package pubSub;


/**
 * Class representing an entity for storing data in the PubSub system.
 * @param <K> The key
 * @param <V> The value
 */
public class Record<K,V> extends Object {

    protected K key;
    protected V value;

    /**
     * Constructor for the Record.
     * @param key
     * @param value
     */
    public Record(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Retrieves the Record's key.
     * @return
     */
    public K getKey() {
        return key;
    }

    /**
     * Retrieves the Record's value.
     * @return
     */
    public V getValue() {
        return value;
    }
}
