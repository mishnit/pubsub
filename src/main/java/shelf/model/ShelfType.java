package shelf.model;


/**
 * Enum for the shelf type.
 */
public enum ShelfType {

    HOT("hot", 1),
    COLD("cold", 1),
    FROZEN("frozen", 1),
    OVERFLOW("overflow", 2);

    public String getType() {
        return type;
    }

    public int getShelfDecayModifier() {
        return shelfDecayModifier;
    }

    private final String type;
    private final int shelfDecayModifier;

    ShelfType(String type, int shelfDecayModifier) {
        this.type = type;
        this.shelfDecayModifier = shelfDecayModifier;
    }
}
