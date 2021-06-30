import pubSub.Subscriber
import spock.lang.Specification


class SubscriberSpec extends Specification {

    def "Test subscriber instantiation"() {

        when:
            Subscriber<String, String> subscriber = new Subscriber<>("topic1")

        then:
            noExceptionThrown()

        when:
            subscriber = new Subscriber<>(null)

        then:
            NullPointerException nullPointerException = thrown()
            nullPointerException.getMessage().equals("Subscriber cannot be instantiated with null topic")
    }

    def "Test subscriber subscribe"() {

        when:
            Subscriber<String, String> subscriber = new Subscriber<>("topic1")

        then:
            noExceptionThrown()

        when:
            subscriber.register()

        then:
            noExceptionThrown()

        when:
            subscriber.poll(-10)

        then:
            IllegalArgumentException illegalArgumentException = thrown()
            illegalArgumentException.getMessage().equals("maxRecords should be greater than 0")
    }
}
