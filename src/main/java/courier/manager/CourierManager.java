package courier.manager;

import callbacks.OrderReceiveCallback;
import courier.model.CourierOrder;
import models.Order;
import subscribers.OrderSubscriber;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Represents a manager for handling couriers.
 * Is implemented as a singleton.
 */
public class CourierManager {

    private final OrderSubscriber orderSubscriber;
    private final Timer orderTimer;

    // The list for maintaining orders that have not yet been handled.
    private final List<String> pendingOrders;

    /**
     * Singleton getter/creator.
     * @return The singleton {@link CourierManager} instance.
     */
    public static CourierManager getOrCreate() {

        return CourierManagerSingleton.instance;
    }

    private static class CourierManagerSingleton {

        private static final CourierManager instance = new CourierManager();
    }

    /**
     * Constructor
     */
    private CourierManager() {

        this.orderTimer = new Timer();
        this.pendingOrders = new ArrayList<>();

        orderSubscriber = new OrderSubscriber("courierSubscriber", new OrderReceiverCallbackImplForCourier(),
                null, 10);

        initThreads();
    }

    /**
     * Records that the order was delivered.
     * @param orderId
     */
    public void onOrderDelivered(String orderId) {

        pendingOrders.remove(orderId);
        if (pendingOrders.size() == 0) {
            shutdown();
        }
    }

    /**
     * Instantiates {@link CourierOrder} subscribers for handling {@link Order}.
     */
    private void initThreads() {

        Thread thread = new Thread(orderSubscriber, "courierSubscriber");
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {

            //  Re-interrupt the current thread: restores the interrupt status of the thread.
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stops the timer since all orders have been handled.
     */
    private void shutdown() {

        orderTimer.cancel();
    }

    /**
     * Handler for receiving an {@link Order} to be delivered.
     * Creates an {@link CourierOrder} for waiting and delivering the order, and adds the {@link Order}
     * to the pending-delivery queue.
     */
    private class OrderReceiverCallbackImplForCourier implements OrderReceiveCallback {

        @Override
        public void onCall(Order order) {

            new CourierOrder(order.getId(), order.getTemperature(), orderTimer);
            pendingOrders.add(order.getId());
        }
    }
}
