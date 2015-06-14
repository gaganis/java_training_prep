import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Java8FirstTouch {
	public static void main(String[] args) {
		List<String> list = Arrays.asList("523", "234", "778");

		Consumer<String> myConsumer = (a) -> System.out.println("gaganis" + a);
		list.stream()
				.sorted((a, b) -> a.compareTo(b))
				.forEach(myConsumer);

	}
}
