package callbacks;


/**
 * Interface for invoking a callback upon exhausting all orders.
 */
public interface OrdersExhaustedCallback {

    void onCall();
}
