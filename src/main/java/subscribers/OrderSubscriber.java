package subscribers;

import callbacks.OrdersExhaustedCallback;
import models.Order;
import models.Serdes.OrderDeserializer;
import pubSub.Record;
import pubSub.Subscriber;
import pubSub.SubscriberException;
import callbacks.OrderReceiveCallback;
import util.AppProperties;
import util.ThreadUtil;
import java.io.IOException;
import java.util.List;

/**
 * {@link Subscriber} for {@link Order}s.
 * Runs as a thread, de-queues {@link Order} from the PubSub system and invokes the {@link OrderReceiveCallback}.
 */
public class OrderSubscriber implements Runnable {

    private final OrderReceiveCallback orderReceiveCallback;
    private final OrdersExhaustedCallback ordersExhaustedCallback;
    private final String subscriberName;

    private static final String ORDERS_TOPIC = AppProperties.getInstance().getProperty("order.topic");
    private static final int NUM_RECORDS_TO_READ = 10; // How many records to read in an invocation of the Subscriber thread.
    private static final int SLEEP_TIME_MS = 1000;

    private volatile int noOrderCount;
    private final int noOrderCountLimit;

    /**
     * Constructor
     * @param name The name of the {@link Subscriber}
     * @param orderReceiveCallback The {@link OrderReceiveCallback} to invoke when an {@link Order} is received.
     * @param ordersExhaustedCallback The {@link OrdersExhaustedCallback} to invoke when there are no more {@link Order}s
     * @param noOrderCountLimit The retry count for invoking the {@link OrdersExhaustedCallback} to invoke when there are no more {@link Order}s.
     */
    public OrderSubscriber(String name, OrderReceiveCallback orderReceiveCallback, OrdersExhaustedCallback ordersExhaustedCallback,
                           int noOrderCountLimit) {

        this.subscriberName = name;
        this.orderReceiveCallback = orderReceiveCallback;
        this.ordersExhaustedCallback = ordersExhaustedCallback;
        this.noOrderCountLimit =  noOrderCountLimit;
    }

    @Override
    public void run() {
        Subscriber subscriber = new Subscriber(ORDERS_TOPIC);
        subscriber.register();

        // While we have not exhausted the retry count for reading orders.
        int maxRetries = 5;
        int retryCount = 0;
        List<Record> records = null;
        while (noOrderCount < noOrderCountLimit) {
            try {

                records = subscriber.poll(NUM_RECORDS_TO_READ);
                if (records.size() == 0) {
                    noOrderCount++;
                    ThreadUtil.sleep(SLEEP_TIME_MS);
                }
                for (Record record : records) {

                    Order order = OrderDeserializer.deserialize((byte[]) record.getValue());
                    System.out.println(subscriberName + " Received order: " + order.getName()
                            + " Value: " + order.printOrderValue());
                    orderReceiveCallback.onCall(order);
                }
            } catch (SubscriberException | IOException se) {

                if (++retryCount > maxRetries) {
                    break;
                } else {

                    // Log and retry.
                    System.out.println("Subscriber Exception: " + se.getMessage());
                    if (records != null && records.size() > 0) {

                        // Rewind if we could successfully read from the PubSub but failed while de-serializing
                        subscriber.rewind(records.size());
                    }
                }
            }
        }
        if (ordersExhaustedCallback != null) {

            // We conclude that there are no more Orders to process.
            ordersExhaustedCallback.onCall();
        }
        System.out.println(subscriberName + " Exiting");
    }
}
