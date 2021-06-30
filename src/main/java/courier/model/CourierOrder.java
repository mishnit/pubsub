package courier.model;

import courier.manager.CourierManager;
import models.Order;
import shelf.manager.ShelfManager;
import shelf.model.ShelfSystem;
import util.RandomNumberGen;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Represents the order that the courier will process.
 * The {@link orderdispatcher.OrderDispatcher} dispatches {@link Order} to the {@link CourierManager} to
 * be picked up. The {@link CourierManager} will take each {@link Order}  to be delivered and
 * subject it to a delay ranging from 2 to 6 seconds, which is implemented by this class.
 */
public class CourierOrder {

    private final String orderId;
    private final String orderTemperature;

    /**
     * Constructor
     * @param orderId The ID of the {@link Order} to process (deliver).
     * @param orderTemperature The temperature of the {@link Order}.
     * @param orderTimer The {@link Timer} associated with the Order to simulate the delay.
     */
    public CourierOrder(String orderId, String orderTemperature, Timer orderTimer) {

        this.orderId = orderId;
        this.orderTemperature = orderTemperature;

        long millisecondsToWait = RandomNumberGen.getRandomNumber(2, 7) * 1000;
        orderTimer.schedule(new CourierTimerTask(), millisecondsToWait);
    }

    /**
     * Task for handling the delivery of an {@link Order}
     */
    class CourierTimerTask extends TimerTask {

        @Override
        public void run() {
            Order order = ShelfManager.getOrCreate().retrieveOrderForDelivery(orderId, orderTemperature);
            if (order == null) {

                // We could not find the order on the shelf, could have been discarded due to overflow.
                System.out.println("Order not found on shelf : " + orderId);
                ShelfManager.getOrCreate().printShelf();
                return;
            }

            if (order.expired()) {
                System.out.println("Discarding expired order: " + order.getName() + " Value: " + order.printOrderValue());
            } else {
                System.out.println("Order delivered : " + order.getName() + " Value: " + order.printOrderValue());
            }
            // Let the manager know that the order has been handled..
            ShelfManager.getOrCreate().printShelf();
            CourierManager.getOrCreate().onOrderDelivered(orderId);
        }
    }
}
