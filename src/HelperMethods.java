import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class HelperMethods {
    // returns a random number between min(inclusive) and max(exclusive)
    public static int mathRandom(int min, int max) {
        Random r = new Random();
        int result = r.nextInt(max - min) + min;
        return result;
    }
}