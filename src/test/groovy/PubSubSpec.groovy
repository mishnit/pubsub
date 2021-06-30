import pubSub.Publisher
import pubSub.Record
import pubSub.Subscriber
import pubSub.SubscriberException
import spock.lang.Specification


class PubSubSpec extends Specification {

    // Positive testcase
    def "Test Publish and Subscribe basic test"() {

        setup:
            String topic = "testTopic"

        when:
            Publisher publisher = new Publisher(topic)
            Record<String, String> testRecord = new Record<>("key", "value")
            publisher.publish(testRecord)

        then:
            noExceptionThrown()

        when:
            Subscriber subscriber = new Subscriber(topic)
            subscriber.register()
            List<Record<String, String>> records = subscriber.poll(10)

        then:
            noExceptionThrown()
            records.get(0).getKey().equals("key")
            records.get(0).getValue().equals("value")
    }

    // Positive testcase
    def "Test Publish and Subscribe with no records in the topic"() {

        setup:
            String topic = "testTopic1"

        when:
            Publisher publisher = new Publisher(topic)

        then:
            noExceptionThrown()

        when:
            Subscriber subscriber = new Subscriber(topic)
            subscriber.register()
            List<Record<String, String>> records = subscriber.poll(10)

        then:
            noExceptionThrown()
            records.isEmpty() == true
    }

    // Positive testcase
    def "Test Publish and Subscribe loop test"() {

        setup:
            String topic = "testTopic2"

        when:
            Publisher publisher = new Publisher(topic)
            for (int i = 0; i < 100; i++) {
                Record<String, String> testRecord = new Record<>("key" + i, "value" + i)
                publisher.publish(testRecord)
            }

        then:
            noExceptionThrown()

        when:
            Subscriber subscriber = new Subscriber(topic)
            subscriber.register()
            List<Record<String, String>> records
            for (int i = 0; i < 10; i++) {
                records = subscriber.poll(10)
            }

        then:
            // We successfully retrieve the last record in the subscriber
            noExceptionThrown()
            records.get(records.size() - 1).getKey().equals("key99")
            records.get(records.size() - 1).getValue().equals("value99")

        when:
            // Now retrieving any more records gives empty results
            records = subscriber.poll(10)

        then:
            noExceptionThrown()
            records.size() ==  0

        when:
            // Publishing more records now
            for (int i = 100; i < 500; i++) {
                Record<String, String> testRecord = new Record<>("key" + i, "value" + i)
                publisher.publish(testRecord)
            }

        then:
            noExceptionThrown()

        when:
            records = subscriber.poll(10)

        then:
            // We can read records as expected
            noExceptionThrown()
            records.get(0).getKey().equals("key100")
            records.get(0).getValue().equals("value100")
    }

    def "Test Publish and Subscribe rewind"() {

        setup:
            String topic = "testTopic3"

        when:
            Publisher publisher = new Publisher(topic)
            for (int i = 0; i < 100; i++) {
                Record<String, String> testRecord = new Record<>("key" + i, "value" + i)
                publisher.publish(testRecord)
            }

        then:
            noExceptionThrown()

        when:
            Subscriber subscriber = new Subscriber(topic)
            subscriber.register()
            List<Record<String, String>> records
            records = subscriber.poll(10)

        then:
            // We successfully retrieve the last record in the subscriber
            noExceptionThrown()
            records.get(records.size() - 1).getKey().equals("key9")
            records.get(records.size() - 1).getValue().equals("value9")

        when:
            subscriber.rewind(10)
            records = subscriber.poll(10)

        then:
            // Since we rewind, same records are returned once again.
            noExceptionThrown()
            records.get(records.size() - 1).getKey().equals("key9")
            records.get(records.size() - 1).getValue().equals("value9")
    }

    // Negative testcase
    def "Test Subscriber subscribes from topic which does not have a produced - exception is thrown"() {

        when:
            Subscriber subscriber = new Subscriber("NotRegisteredTopic")
            List<Record<String, String>> records = subscriber.poll(10)

        then:
            SubscriberException subscriberException = thrown()
            subscriberException.getMessage().equals("Topic not found")
    }

    // Negative testcase
    def "Test Subscriber is not registered - exception is thrown"() {

        setup:
            String topic = "testTopic"

        when:
            Publisher publisher = new Publisher(topic)
            Record<String, String> testRecord = new Record<>("key", "value")
            publisher.publish(testRecord)

            Subscriber subscriber = new Subscriber(topic)
            List<Record<String, String>> records = subscriber.poll(10)

        then:
            SubscriberException subscriberException = thrown()
            subscriberException.getMessage().equals("Subscriber not registered")
    }
}
