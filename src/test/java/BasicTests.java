import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BasicTests {
    @BeforeAll
    public static void init() {
        System.out.println("init!");
    }

    @Test
    public void printMommoo() {
        System.out.println("Mommoo");
        //Assertions.assertEquals("mommoo", "m");
    }

    @Test
    public void printHamzzi() {
        System.out.println("Hamzzi");
    }
}
