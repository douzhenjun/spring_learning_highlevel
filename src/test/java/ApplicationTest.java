import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootTest
public class ApplicationTest {
    
    @Test
    public void testStream(){
        List<Integer> numList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

        List<String> strList = numList.stream()
                .map(it -> Integer.toString(it))
                .collect(Collectors.toList());
        strList.stream().forEach(System.out::println);
    }
    
}
