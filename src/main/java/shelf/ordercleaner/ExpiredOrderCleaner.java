package shelf.ordercleaner;

import models.Order;
import shelf.manager.ShelfManager;
import util.ThreadUtil;
import java.util.Iterator;

/**
 * Cleaner for "expired" orders. Runs as a background thread,
 * and removes any expired {@link Order}s from the {@link shelf.model.ShelfSystem}
 */
public class ExpiredOrderCleaner implements Runnable {

    private static final int SLEEP_TIME_MS = 500;
    private volatile boolean exit;

    // Reference to the singleton instance.
    private final ShelfManager shelfManager;

    public ExpiredOrderCleaner(ShelfManager shelfManager) {

        this.shelfManager = shelfManager;
    }

    public void shutdown() {
        this.exit = true;
    }

    @Override
    public void run() {

        while(!exit) {

            // Goes over all shelfs, all Orders, checks if any Order has expired and takes it off if so.
            shelfManager.getShelfSystem().getShelfs().values().stream().forEach(
                    shelf -> {
                        Iterator<Order> iterator = shelf.values().iterator();
                        while (iterator.hasNext()) {
                            Order order = iterator.next();
                            if (order.expired()) {
                                System.out.println("Discarding expired order: " + order.getName()
                                        + " Value: " + order.printOrderValue());

                                shelfManager.removeOrder(order.getId());
                                shelfManager.printShelf();
                                iterator.remove();
                            }
                        }
                    }
            );

            ThreadUtil.sleep(SLEEP_TIME_MS);
        }
        System.out.println("ExpiredOrderCleaner exiting");
    }
}
