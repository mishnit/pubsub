package models.Serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Order;
import java.io.IOException;

/**
 * Class for de-serializing an {@link Order}.
 * Uses the {@link com.fasterxml.jackson.databind.deser.std.JacksonDeserializers} library internally.
 */
public class OrderDeserializer {

    /**
     * De-serializes the byte stream of a single {@link Order}
     * @param orderByteStream The {@link Order} to be de-serialized.
     * @return The new {@link Order} object.
     * @throws IOException
     */
    public static Order deserialize(byte[] orderByteStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Order order = mapper.readValue(orderByteStream, Order.class);

        // This is when the order's age begins (simulates that the Order is cooked).
        order.constructOrderValue();

        return order;
    }
}
