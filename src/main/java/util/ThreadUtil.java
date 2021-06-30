package util;


public class ThreadUtil {

    public static void sleep(int sleepTimeMs) {
        try {
            Thread.sleep(sleepTimeMs);
        } catch (InterruptedException e) {

            //  Re-interrupt the current thread: restores the interrupt status of the thread.
            Thread.currentThread().interrupt();
        }
    }
}
