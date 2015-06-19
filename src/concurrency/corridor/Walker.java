package concurrency.corridor;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Walker extends Thread{
	private static final int DIRECTION_BACK = 1;
	private static final int DIRECTION_FORWARD = -1;
	
	Lock lock = new ReentrantLock();
	
	Random random = new Random();

	public enum Lane{
		LANE_A,
		LANE_B
	}
	
	
	int direction;
	private Lane lane;
	int position;
	
	public synchronized void setPosition(int position) {
		this.position = position;
	}

	Walker opponent;
	private String name;
	
	public void setOpponent(Walker opponent) {
		this.opponent = opponent;
	}

	public Walker(String name, int direction)
	{
		this.direction = direction;
		this.lane = Lane.LANE_A;
		this.name = name;
		this.position = 10 * direction;
	}
	
	public void run()
	{
		for(; position != 10 * direction * -1 ;)
		{
			
			if(!moveForward())
			{
				if(random.nextBoolean())
				{
					changeLane();
				}
			}
			
//			System.out.println(name + " Walking " + position + " Lane " + lane);
			try {
                Thread.sleep(random.nextInt(500));
            } catch (InterruptedException e) {}
		}
	}

	private boolean moveForward() {
		boolean moved = false;
		boolean myLock = lock.tryLock();
		boolean otherLock = opponent.lock.tryLock();
		try{
			if(myLock && otherLock){
				int walking_into = position + direction * -1;
				if(!(opponent.getPosition() == walking_into && getLane().equals(opponent.getLane()))){
					setPosition(walking_into);
					moved = true;
					System.out.format("%s walked into %d \n", name, walking_into);
				}else
					System.out.format("%s failed to move forward. \n", name);
			}else
				System.out.println("unable to lock both locks");
		}finally{
			if(otherLock)
				opponent.lock.unlock();
			if(myLock)
				lock.unlock();
		}
		return moved;
	}

	private void changeLane() {
//		System.out.format("# %s Begins to change lane", name);
		
		boolean myLock = lock.tryLock();
		boolean otherLock = opponent.lock.tryLock();
		try{
			if(myLock && otherLock){
				Lane changeToLane;
				if(lane.equals(Lane.LANE_A))
					changeToLane = Lane.LANE_B;
				else
					changeToLane = Lane.LANE_A;

				if(!(opponent.getPosition() == position && changeToLane.equals(opponent.getLane()))){
					lane = changeToLane;
					System.out.format("%s Changed lane \n", name);
				} else
					System.out.println("failed to set. aborting");
			}else
				System.out.println("unable to lock both locks");
		}finally{
			if(otherLock)
				opponent.lock.unlock();
			if(myLock)
				lock.unlock();
		}
		
		
	}

	private  int getPosition() {
		return position;
	}

	public static void main(String[] args) {
		for(int i =0; i < 100; i++)
		{
			new Thread(){
				public void run()
				{
					walkerPair();
				}
			}.start();
		}
		
	}

	private static void walkerPair() {
		Walker walker1 = new Walker("---Mitsos", DIRECTION_FORWARD);
		Walker walker2 = new Walker("Kwstas", DIRECTION_BACK);
		
		walker1.setOpponent(walker2);
		walker2.setOpponent(walker1);
		
		walker1.start();
		walker2.start();
		while(walker1.isAlive() || walker2.isAlive())
		{
			if(walker1.position == walker2.position 
				&& walker1.getLane().equals(walker2.getLane()))
				System.out.println("walkers in same position and lane");
		}
	}

	public synchronized Lane getLane() {
		return lane;
	}

	
}
