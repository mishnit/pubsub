package orderdispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Order;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for reading {@link Order}s from the file.
 */
final class OrderFileReaderUtil {

    // The name of the file having the Orders. Can be moved to the AppProperties if required.
    // This file is assumed to be present in the Jar.
    private static String FILENAME = "orders.json";

    /**
     * Reads Orders from the file and returns a {@link java.util.List} of Orders.
     * Exits if reading orders threw an exception.
     * As of now this method reads all orders in the file and returns the array, expecting the caller
     * to do the buffering and dispatch rate control. Otoh we can enhance this class to take the number
     * of orders to read/dispatch as well. Also, we can implement an internal cache in this class so that
     * we read the file-system beforehand and cache a pre-determined numnber of orders (since file-read is an expensive operation).
     *
     * @return The array containing the {@link Order}s.
     */
    Order[] readOrders() {

        ObjectMapper objectMapper = new ObjectMapper();
        InputStream input = getClass().getClassLoader().getResourceAsStream(FILENAME);
        Order [] orders = null;
        try {
            orders = objectMapper.readValue(input, Order[].class);
        } catch (IOException ioException) {

            // Exit the application since we cannot proceed if we fail here.
            System.out.println("*** Error loading app.properties..Exiting *** " + ioException.getMessage());
            System.exit(0);
        }
        return (orders == null) ? new Order[0] : orders;
    }
}
