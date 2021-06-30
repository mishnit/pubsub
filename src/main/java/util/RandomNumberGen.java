package util;


import java.util.Random;

public class RandomNumberGen {

    public static int getRandomNumber(int start, int endExclude) {
        Random random = new Random();
        return random.ints(start, endExclude)
                .findFirst()
                .getAsInt();
    }
}
