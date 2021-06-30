package models;


import shelf.model.ShelfType;

/**
 * Class representing the Value of an {@link Order}.
 * Can be queried any time to get the current value of the Order.
 */
final class OrderValue {

    // Stores the order creation time in epoch seconds.
    private final long createTimeInMs;
    private final int shelfLife;
    private final double decayRate;
    private ShelfType currentShelfType;

    void setCurrentShelfType(ShelfType currentShelfType) {
        this.currentShelfType = currentShelfType;
    }

    /**
     * Constructor.
     * @param order The {@link Order} to construct the Value from.
     */
    OrderValue(Order order) {

        createTimeInMs = System.currentTimeMillis();
        shelfLife = order.getShelfLife();
        decayRate = order.getDecayRate();

        currentShelfType = ShelfType.valueOf(order.getTemperature().toUpperCase());
    }

    double getOrderValue() {

        double orderAge = getOrderAgeInSeconds();
        return (shelfLife - orderAge -
                (decayRate * orderAge * currentShelfType.getShelfDecayModifier())) /
                shelfLife;
    }

    String printOrderValue() {

        return "OrderValue: " + getOrderValue();
    }

    private double getOrderAgeInSeconds() {

        return (System.currentTimeMillis() - createTimeInMs)/1000;
    }
}
