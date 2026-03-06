
package com.TestFlashCard.FlashCard.Utils;
import java.util.concurrent.ThreadLocalRandom;
public class random6DigitNumber {
    public static int randomDigit() {
        return ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
    }
}
