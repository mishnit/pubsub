package models.Serdes;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Order;

/**
 * Class for serializing an {@link Order}. Uses the {@link com.fasterxml.jackson.databind.deser.std.JacksonDeserializers}
 * library internally.
 */
public class OrderSerializer {

    /**
     * Serializes the {@link Order}
     * @param order
     * @return The serialized byte stream for the Order.
     */
    public static byte[] serialize(Order order) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            retVal = objectMapper.writeValueAsString(order).getBytes();
        } catch (Exception e) {

            //  Re-interrupt the current thread: restores the interrupt status of the thread.
            Thread.currentThread().interrupt();
        }
        return retVal;
    }
}
