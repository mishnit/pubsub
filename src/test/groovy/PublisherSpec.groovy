import pubSub.Publisher
import pubSub.Record
import spock.lang.Specification


class PublisherSpec extends Specification {

    def "Test publisher instantiation"() {

        when:
            Publisher<String, String> publisher = new Publisher<>("topic1")

        then:
            noExceptionThrown()

        when:
            publisher = new Publisher<>(null)

        then:
            NullPointerException nullPointerException = thrown()
            nullPointerException.getMessage().equals("Publisher cannot be instantiated with a null topic")
    }

    def "Test publisher publish"() {

        when:
            Publisher<String, String> publisher = new Publisher<>("topic1")

        then:
            noExceptionThrown()

        when:
            publisher.publish(null)

        then:
            NullPointerException nullPointerException = thrown()
            nullPointerException.getMessage().equals("Cannot publish null message")

        when:
            Record<String, String> record = new Record<>("key", "value")
            publisher.publish(record)

        then:
            noExceptionThrown()
    }
}
