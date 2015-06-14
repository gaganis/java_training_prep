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
	}
	
	public void run()
	{
		for(position = 10 * direction; position != 10 * direction * -1 ;)
		{
			int walking_into = position + direction * -1;
			while(checkPosition(walking_into))
			{
				if(random.nextBoolean())
				{
					try {
			            Thread.sleep(random.nextInt(500));
			        } catch (InterruptedException e) {}
					changeLane();
				}
			}
			checkAndSetPosition(walking_into);
			
//			System.out.println(name + " Walking " + position + " Lane " + lane);
			try {
                Thread.sleep(random.nextInt(500));
            } catch (InterruptedException e) {}
		}
	}

	private void checkAndSetPosition(int walking_into) {
		lock.lock();
		opponent.lock.lock();
		if(!(opponent.getPosition() == walking_into && getLane().equals(opponent.getLane())))
			setPosition(walking_into);
		else
			System.out.println("failed to set. aborting");
		lock.unlock();
		opponent.lock.unlock();
	}

	private boolean checkPosition(int walking_into) {
		lock.lock();
		opponent.lock.lock();
		boolean result = opponent.getPosition() == walking_into && getLane().equals(opponent.getLane());
		lock.unlock();
		opponent.lock.unlock();
		return result;
	}
	
	private void changeLane() {
//		System.out.format("# %s Begins to change lane", name);
		
		lock.lock();
		if(lane.equals(Lane.LANE_A))
			lane = Lane.LANE_B;
		else
			lane = Lane.LANE_A;
		lock.unlock();
//		System.out.println(name + "Changed lane");
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
