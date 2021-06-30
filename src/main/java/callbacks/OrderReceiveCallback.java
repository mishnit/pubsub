package callbacks;


import models.Order;

/**
 * Interface for invoking a callback upon receiving an order.
 */
public interface OrderReceiveCallback {

    void onCall(Order order);
}
