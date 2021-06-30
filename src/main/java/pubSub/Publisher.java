package pubSub;


import static com.google.inject.internal.util.Preconditions.checkNotNull;

/**
 * Class for publishing a record into the PubSub system.
 * @param <K> The key
 * @param <V> The value
 */
public class Publisher<K,V> {

    // Reference to the singleton PubSubManager
    private final PubSubManager pubSubManager = PubSubManager.getInstance();

    // The topic that this Publisher corresponds to
    private final String topic;

    /**
     * Constructor.
     * The flow for publishing messages into the PubSub system is to instantiate a {@link Publisher} with the
     * desired topic, and then invoke {@link Publisher#publish(Record)} with the {@link Record} to publish.
     *
     * @param topic The topic for this {@link Publisher}
     */
    public Publisher(String topic) {
        checkNotNull(topic, "Publisher cannot be instantiated with a null topic");

        this.topic = topic;
        pubSubManager.initTopic(topic);
    }

    /**
     * Publishes the {@link Record} into the PubSub system.
     *
     * @param publisherRecord The record to publish
     * @throws PublisherException If there was an error while publishing the record.
     */
    public void publish(Record<K,V> publisherRecord) throws PublisherException {
        checkNotNull(publisherRecord, "Cannot publish null message");

        pubSubManager.send(topic, publisherRecord);
    }
}
