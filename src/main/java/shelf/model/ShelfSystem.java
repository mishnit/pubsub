package shelf.model;

import models.Order;
import shelf.manager.ShelfManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Shelf for storing {@link Order}s
 */
public final class ShelfSystem {

    private final int REGULAR_SHELF_SIZE = 10;
    private final int OVERFLOW_SHELF_SIZE = 15;

    // Is a map of shelfType to the actual Shelf.
    private Map<String, Map<String, Order>> shelfs = new ConcurrentHashMap<>();

    // Has the count of number of free slots (value) on a given shelfType (key).
    private Map<String, Integer> currentFreeSlotsOnShelf = new ConcurrentHashMap<>();

    private final ShelfManager manager;

    public ShelfSystem(ShelfManager manager) {

        this.manager = manager;
    }

    /**
     * Instantiates the Shelf system.
     */
    public void init() {

        Stream.of(ShelfType.values())
                .forEach(value -> {
                    Map shelf = new ConcurrentHashMap();
                    shelfs.putIfAbsent(value.name(), shelf);
                    currentFreeSlotsOnShelf.putIfAbsent(value.name(), REGULAR_SHELF_SIZE);
                });

        // Replace the overflow shelf's capacity.
        currentFreeSlotsOnShelf.putIfAbsent(ShelfType.OVERFLOW.name(), OVERFLOW_SHELF_SIZE);
    }

    public Map<String, Map<String, Order>> getShelfs() {
        return shelfs;
    }

    public boolean isFull(ShelfType shelfType) {

        return currentFreeSlotsOnShelf.get(shelfType.name()) == 0;
    }

    public boolean checkAndMoveFromOverflowShelf() {

        boolean moved = false;

        for (Map.Entry<String, Order> entry : shelfs.get(ShelfType.OVERFLOW.name()).entrySet()) {

            Order order = entry.getValue();
            ShelfType orderShelfType = ShelfType.valueOf(order.getTemperature().toUpperCase());
            if (!isFull(orderShelfType)) {
                storeOrder(orderShelfType, order);
                moved = true;
             }
        }
        return moved;
    }

    public void discardOrderFromOverflowShelf(String orderId) {

        shelfs.get(ShelfType.OVERFLOW.name()).remove(orderId);
    }

    public Order retrieveOrderForDelivery(String orderId, String orderTemperature) {

        Order order;
        ShelfType orderShelfType = ShelfType.valueOf(orderTemperature.toUpperCase());
        if (shelfs.get(orderShelfType.name()).containsKey(orderId)) {
            // Check the shelf the order is supposed to be on.
            order = shelfs.get(orderShelfType.name()).get(orderId);
            shelfs.get(orderShelfType.name()).remove(orderId);
        } else {
            // Else check the overflow shelf.
            order = shelfs.get(ShelfType.OVERFLOW).get(orderId);
            shelfs.get(ShelfType.OVERFLOW).remove(orderId);
        }
        return order;
    }

    /**
     * Places/stores the Order into the Shelf.
     *
     * @param shelfType The shelf type to store the Order into
     * @param order The Order to store
     */
    public void storeOrder(ShelfType shelfType, Order order) {

        if (order.expired()) {

            System.out.println("Discarding expired order: " + order.getName()
                    + " Value: " + order.printOrderValue());

            manager.printShelf();
            manager.removeOrder(order.getId());
            return;
        }

        Map shelf = shelfs.get(shelfType.name());
        if (shelf == null) {

            // We don't have a shelf with this type yet.. create a new one.
            shelf = new ConcurrentHashMap();
            shelfs.putIfAbsent(shelfType.name(), shelf);
        }
        // Place order onto the shelf.
        shelf.putIfAbsent(order.getId(), order);

        order.setShelfType(shelfType);

        // Decrease capacity
        currentFreeSlotsOnShelf.putIfAbsent(shelfType.name(), currentFreeSlotsOnShelf.get(shelfType.name()) - 1);
    }
}
