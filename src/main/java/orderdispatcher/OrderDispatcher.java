package orderdispatcher;

import pubSub.Publisher;
import pubSub.PublisherException;
import pubSub.Record;
import models.Order;
import models.Serdes.OrderSerializer;
import util.AppProperties;
import util.ThreadUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for dispatching orders.
 */
public class OrderDispatcher {

    private List<Order> ordersList;
    private Timer dispatcher;
    private volatile int currentIndex;

    // The topic to dispatch Orders into.
    private final String ORDERS_TOPIC;

    // How many Orders to dispatch per invocation of the dispatcher task.
    private final int DISPATCH_RATE_PER_RUN;
    private final int CHECKER_THREAD_SLEEP_IN_MS = 1000;

    // The interval per invocation of the dispatcher task.
    private static final int DISPATCHER_PERIODICITY_IN_MS = 1000;

    private final int ONE_SEC_IN_MS = 1000;

    public static OrderDispatcher getOrCreate() {

        return OrderDispatcherSingleton.instance;
    }

    private static class OrderDispatcherSingleton {

        private static final OrderDispatcher instance = new OrderDispatcher();
    }

    private OrderDispatcher() {
        AppProperties properties = AppProperties.getInstance();

        // Calculate how many Orders we want to dispatch per invocation of the Dispather thread.
        DISPATCH_RATE_PER_RUN = Integer.parseInt(properties.getProperty("order.dispatch.rate.per.sec")) * DISPATCHER_PERIODICITY_IN_MS / ONE_SEC_IN_MS;

        ORDERS_TOPIC = properties.getProperty("order.topic");
        readOrdersAndDispatch();
    }

    private void readOrdersAndDispatch() {

        ordersList = Arrays.asList(new OrderFileReaderUtil().readOrders());
        dispatcher = new Timer();
        dispatcher.schedule(new Dispatcher(), 0, DISPATCHER_PERIODICITY_IN_MS);

        while (currentIndex < ordersList.size()) {
            // Poll, sleep and wait until we have dispatched all orders.
            ThreadUtil.sleep(CHECKER_THREAD_SLEEP_IN_MS);
        }
        dispatcher.cancel();

        System.out.println("Shutting down dispatcher");
    }

    /**
     * Dispatcher task. Reads orders as per desired rate and dispatches them into the PubSub system.
     */
    class Dispatcher extends TimerTask {

        Publisher publisher = new Publisher(ORDERS_TOPIC);

        @Override
        public void run() {

            int numRecordsToRead;
            if ((currentIndex + DISPATCH_RATE_PER_RUN) <= ordersList.size()) {
                // If we have enough orders to dispatch.
                numRecordsToRead = DISPATCH_RATE_PER_RUN;
            } else {
                numRecordsToRead = ordersList.size() - currentIndex;
            }
            for (int i = 0; i < numRecordsToRead; i++) {
                Order order = ordersList.get(currentIndex++);
                Record record = new Record(
                        order.getId(), OrderSerializer.serialize(order));

                try {
                    publisher.publish(record);
                } catch (PublisherException pe) {
                    // Log and continue to dispatch.
                    System.out.println("Publisher Exception: " + pe.getMessage());
                }
                System.out.println("Dispatched order: " + order.getName());
            }
        }
    }
}
