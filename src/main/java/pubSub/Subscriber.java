package pubSub;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Objects;
import static com.google.inject.internal.util.Preconditions.checkArgument;
import static com.google.inject.internal.util.Preconditions.checkNotNull;

/**
 * Class for reading {@link Record} from the PubSub system.
 * @param <K> The key
 * @param <V> The value
 */
public class Subscriber<K,V>  {

    // Represents a unique hash for identifying this Subscriber.
    private final int HASH;

    // The topic corresponding to this Subscriber.
    private final String topic;

    // Reference to the Singleton instance.
    private final PubSubManager pubSubManager = PubSubManager.getInstance();

    /**
     * Instantiates a new {@link Subscriber}
     *
     * The workflow for subscribing to messages from the PubSub is to instantiate a new {@link Subscriber} and
     * invoke {@link Subscriber#register()}. Then the consumer can invoke {@link Subscriber#poll(int)} with the
     * limit on the number of {@link Record}s to be returned back.
     *
     * @param topic The topic corresponding to this Subscriber.
     */
    public Subscriber(String topic) {
        checkNotNull(topic, "Subscriber cannot be instantiated with null topic");

        String generatedString = RandomStringUtils.randomAlphanumeric(32);
        HASH = Objects.hash(generatedString);
        this.topic = topic;
    }

    /**
     * Register the subscriber with the PubSub system.
     */
    public void register() {

        pubSubManager.registerSubscriber(HASH, topic);
    }

    /**
     * Retrieves {@link Record}s from the PubSub system.
     *
     * @param maxRecords The maximum number of records to return.
     * @return The List of records.
     * @throws SubscriberException If there was any error while retrieving the records.
     */
    public List<Record<K,V>> poll(int maxRecords) throws SubscriberException {
        checkArgument(maxRecords > 0, "maxRecords should be greater than 0");

        return pubSubManager.poll(topic, HASH, maxRecords);
    }

    /**
     * Rewinds the current subscriber's offset count by rewindCount.
     *
     * @param rewindCount The number of {@link Record}s to rewind
     */
    public void rewind(int rewindCount) {
        checkArgument(rewindCount > 0, "Illegal rewindCount, should be > 0");

        pubSubManager.rewind(HASH, rewindCount);
    }
}
