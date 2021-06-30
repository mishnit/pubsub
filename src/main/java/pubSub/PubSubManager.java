package pubSub;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class representing the manager for the PubSub system.
 * This is used only internally by the {@link Publisher} and the {@link Subscriber}.
 *
 * @param <K> The key for the {@link Record}
 * @param <V> The value for the {@link Record}
 */
final class PubSubManager<K,V> {

    // Some potential enhancements that can be done:
    // Add an expiry for the queue
    // The queue can be periodically backed-up to a disk to prevent data loss during a machine failure
    // The queue can be replicated into more than one machine for higher availability

    // We can add auto-handling of serializers and de-serializers using properties
    // This will prevent producers and consumers from having to track the appropriate objects and do the serdes.

    // Stores the actual data (value) per topic (key).
    private final Map<String, List<Record<K,V>>> topicToQueueMap = new ConcurrentHashMap<>();

    // Stores the list of subscribers (value) that are registered per topic (key).
    private final Map<String, List<Integer>> topicToSubscriberMap = new ConcurrentHashMap<>();

    // Stores the offset (value) per subscriber (key). This way we can support multiple subscriber offsets for the same topic.
    private final Map<Integer, Integer> subscriberToOffsetMap = new ConcurrentHashMap<>();

    private final static PubSubManager INSTANCE = new PubSubManager();

    private PubSubManager() {

    }

    /**
     * Returns the singleton instance.
     * @return
     */
    static PubSubManager getInstance() {
        return INSTANCE;
    }

    /**
     * Called by the {@link Publisher}
     * Readies things for accepting {@link Record}s into the system on the topic.
     *
     * @param topic
     */
    synchronized void initTopic(String topic) {

        if (topicToQueueMap.containsKey(topic)) {

            // If this topic has already been registered.
            return;
        }
        topicToQueueMap.put(topic, Collections.synchronizedList(new ArrayList<>()));
    }

    /**
     * Called by the {@link Publisher}
     *
     * @param topic
     * @param publisherRecord
     * @throws PublisherException
     */
    synchronized void send(String topic, Record<K,V> publisherRecord) throws PublisherException {

        try {
            topicToQueueMap.get(topic).add(publisherRecord);
        } catch (Exception e) {

            // Log actual cause and throw exception back.
            System.out.println("Publishing failed with exception " + e.getMessage());
            throw new PublisherException("Publishing failed");
        }
    }

    /**
     * Called by the {@link Subscriber}.
     *
     * @param subscriberHash
     * @param topic
     */
    synchronized void registerSubscriber(int subscriberHash, String topic) {

        List<Integer> consumerList;
        if (topicToSubscriberMap.containsKey(topic)) {
            consumerList = topicToSubscriberMap.get(topic);
        } else {
            consumerList = Collections.synchronizedList(new ArrayList<>());
            topicToSubscriberMap.put(topic, consumerList);
        }
        consumerList.add(subscriberHash);
        subscriberToOffsetMap.put(subscriberHash, new Integer(0));
    }

    /**
     * Sanity checks for the {@link Subscriber}
     *
     * @param topic
     * @param subscriberHash
     * @throws SubscriberException
     */
    private void checkTopicAndSubscriberHash(String topic, int subscriberHash) throws SubscriberException {

        if (!topicToQueueMap.containsKey(topic)) {

            // This means that there is no Producer producing data on this topic, yet.
            // We want to be strict to allow subscribers to registers only on topics which have
            // data from the Producers.
            throw new SubscriberException("Topic not found");
        }
        long found = (topicToSubscriberMap.get(topic) != null) ?
                topicToSubscriberMap.get(topic)
                        .stream()
                        .filter(hash -> (hash == subscriberHash))
                        .count() : 0;

        if (found == 0) {
            throw new SubscriberException("Subscriber not registered");
        }
    }

    /**
     * Called by the {@link Subscriber} to retrieve upto maxRecords number of {@link Record}s.
     *
     * @param topic
     * @param subscriberHash
     * @param maxRecords
     * @return The List of records containing upto maxRecord number of entries.
     * @throws SubscriberException
     */
    synchronized List<Record<K,V>> poll(String topic, int subscriberHash, int maxRecords) throws SubscriberException {

        checkTopicAndSubscriberHash(topic, subscriberHash);
        Integer subscriberOffset = subscriberToOffsetMap.getOrDefault(subscriberHash, -1);

        if (subscriberOffset == -1) {
            throw new SubscriberException("Subscriber not registered");
        }

        if (!topicToQueueMap.containsKey(topic) || (topicToQueueMap.get(topic).size() == subscriberOffset)) {

            // No producer has produced data into this topic OR the consumer is already at the end offset of the topic.
            return Collections.emptyList();
        }

        List<Record<K, V>> topicQueue = topicToQueueMap.get(topic);
        List<Record<K,V>> returnedRecords = new ArrayList<>();
        int endOffset;
        try {
            if ((subscriberOffset + maxRecords) <= topicQueue.size()) {
                // We have enough items in the queue to read as requested
                endOffset = subscriberOffset + maxRecords;
            } else {
                // We don't have as many items as requested - Read and give back what we have
                endOffset = topicQueue.size();
            }
            for (int i = subscriberOffset; i < endOffset; i++) {
                returnedRecords.add(topicQueue.get(i));
            }
            subscriberToOffsetMap.put(subscriberHash, endOffset);
        } catch (Exception e) {

            // Log error and throw exception back.
            System.out.println("Subscriber Exception: " + e.getMessage());
            throw new SubscriberException("Polling failed");
        }
        return returnedRecords;
    }

    /**
     * Rewinds the subscriber's offset by rewindCount
     * @param subscriberHash The unique ID of the subscriber
     * @param rewindCount
     */
    synchronized void rewind(int subscriberHash, int rewindCount) {

        int currentOffset = subscriberToOffsetMap.get(subscriberHash);
        if (currentOffset - rewindCount >= 0) {
            subscriberToOffsetMap.put(subscriberHash, currentOffset - rewindCount);
        }
    }
}
