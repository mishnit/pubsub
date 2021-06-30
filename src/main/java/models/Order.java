package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import shelf.model.ShelfType;

/**
 * Class representing an Order to be processed by the system.
 */
public final class Order {

    private String id;
    private String name;

    @JsonProperty("temp")
    private String temperature;

    private int shelfLife;
    private double decayRate;

    @JsonIgnore
    private OrderValue orderValue;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTemperature() {
        return temperature;
    }

    public int getShelfLife() {
        return shelfLife;
    }

    public double getDecayRate() {
        return decayRate;
    }


    // TODO: Can use builder pattern as the number of params increase and become "optional" for constructing the Order.
    public Order(String id, String name, String temperature, int shelfLife, double decayRate) {

        this.id = id;
        this.name = name;
        this.temperature = temperature;
        this.shelfLife = shelfLife;
        this.decayRate = decayRate;
    }

    public Order() {

    }

    /**
     * Instantiates the value for the order by starting the order's age.
     */
    public void constructOrderValue() {

        orderValue = new OrderValue(this);
    }

    /**
     * Getter for the value.
     * @return
     */
    public double getOrderValue() {

        return orderValue.getOrderValue();
    }

    /**
     * Checks to see if this order has expired.
     * @return True if expired.
     */
    public boolean expired() {

        return getOrderValue() <= 0.0f;
    }

    /**
     * Setter for shelftype for this order.
     * @param shelfType
     */
    public void setShelfType(ShelfType shelfType) {

        orderValue.setCurrentShelfType(shelfType);
    }

    /**
     * Prints the string for the current value of the order.
     * @return
     */
    public String printOrderValue() {

        return orderValue.printOrderValue();
    }
}
