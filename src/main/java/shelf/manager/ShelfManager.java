package shelf.manager;

import callbacks.OrderReceiveCallback;
import callbacks.OrdersExhaustedCallback;
import models.Order;
import shelf.model.ShelfSystem;
import shelf.model.ShelfType;
import shelf.ordercleaner.ExpiredOrderCleaner;
import subscribers.OrderSubscriber;
import util.RandomNumberGen;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager for the {@link ShelfSystem}.
 */
public class ShelfManager {

    private final ExpiredOrderCleaner expiredOrderCleaner;
    private final OrderSubscriber orderSubscriber;
    private final ShelfSystem shelfSystem;

    private final List<String> currentOrderIds;

    /**
     * Singleton getter.
     * @return
     */
    public static ShelfManager getOrCreate() {

        return ShelfManagerSingleton.instance;
    }

    private ShelfManager() {

        currentOrderIds = new ArrayList<>();

        shelfSystem = new ShelfSystem(this);
        shelfSystem.init();

        OrderReceiverCallbackImplForShelf ordersReceivedCallback = new OrderReceiverCallbackImplForShelf();
        OrdersExhaustedCallback ordersExhaustedCallback = new OrdersExhaustedCallbackImplForShelf();
        orderSubscriber = new OrderSubscriber("shelfSubscriber", ordersReceivedCallback, ordersExhaustedCallback, 10);

        expiredOrderCleaner = new ExpiredOrderCleaner(this);

        initThreads();
    }

    private static class ShelfManagerSingleton {

        private static final ShelfManager instance = new ShelfManager();
    }


    private void initThreads() {

        Thread subscriberThread = new Thread(orderSubscriber, "shelfSubscriber");
        subscriberThread.start();

        Thread expiredOrderCleanerThread = new Thread(expiredOrderCleaner, "shelfExpiredOrderCleaner");
        expiredOrderCleanerThread.start();
        try {
            subscriberThread.join();
            expiredOrderCleanerThread.join();
        } catch (InterruptedException e) {

            //  Re-interrupt the current thread: restores the interrupt status of the thread.
            System.out.println("ShelfManager thread join interrupted, cause: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {

        expiredOrderCleaner.shutdown();
    }

    public Order retrieveOrderForDelivery(String orderId, String orderTemperature) {

        return isOrderInShelf(orderId) ? shelfSystem.retrieveOrderForDelivery(orderId, orderTemperature) : null;
    }

    public ShelfSystem getShelfSystem() {

        return shelfSystem;
    }

    public boolean isOrderInShelf(String orderId) {

        return currentOrderIds.contains(orderId);
    }

    public void removeOrder(String orderId) {

        currentOrderIds.remove(orderId);
    }

    public void printShelf() {

        shelfSystem.getShelfs().entrySet().stream().forEach(
                entry  -> {
                    System.out.println("*** Shelf Type "  + entry.getKey() + " Contents *** ");
                    entry.getValue().values().stream().forEach(
                            order -> {
                                System.out.println("Shelf content: Order name: " + order.getName() + ", Value: " + order.getOrderValue());
                            }
                    );
                }
        );
    }

    /**
     * Implements the handling of receiving {@link Order}s into the {@link ShelfSystem}.
     */
    private class OrderReceiverCallbackImplForShelf implements OrderReceiveCallback {

        @Override
        public void onCall(Order order) {

            if (order.expired()) {

                // Early check, do not accept expired orders to shelf.
                System.out.println("Discarding expired order: " + order.getName()
                        + " Value: " + order.printOrderValue());

                ShelfManager.getOrCreate().printShelf();
                return;
            }

            // Record that we received this order.
            currentOrderIds.add(order.getId());
            ShelfType shelfType = ShelfType.valueOf(order.getTemperature().toUpperCase());

            if (!shelfSystem.isFull(shelfType)) {

                // If we have space on the ShelfType for this Order, store it there.
                shelfSystem.storeOrder(shelfType, order);
            } else {

                handleOrderSpecificShelfIsFull(order);
            }
        }

        private void handleOrderSpecificShelfIsFull(Order order) {

            if (shelfSystem.isFull(ShelfType.OVERFLOW)) {

                // Overflow shelf is full, try to move something from overflow shelf into its own designated shelf.
                if (!shelfSystem.checkAndMoveFromOverflowShelf()) {

                    // We could not move an item from overflow shelf.
                    // Discard (sigh) a random item from the overflow shelf to make space for the new order.
                    discardRandomOrder();
                }
            }
            // Overflow shelf has space now, place order there.
            shelfSystem.storeOrder(ShelfType.OVERFLOW, order);
        }


        private void discardRandomOrder() {
            String randomOrderId = currentOrderIds.get(RandomNumberGen.getRandomNumber(
                    0, currentOrderIds.size()
            ));
            shelfSystem.discardOrderFromOverflowShelf(randomOrderId);

            // Record that we discarded this order
            currentOrderIds.remove(randomOrderId);
        }
    }

    private class OrdersExhaustedCallbackImplForShelf implements OrdersExhaustedCallback {

        @Override
        public void onCall() {
            shutdown();
        }
    }
}
