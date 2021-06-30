import models.Order
import models.Serdes.OrderDeserializer
import models.Serdes.OrderSerializer
import spock.lang.Shared
import spock.lang.Specification


class OrderSerDesSpec extends Specification {

    @Shared
    Order order

    // Called once per test suite.
    def setupSpec() {
        order = new Order("randomId", "testOrder", "hot", 100, 0.25)
    }

    def "Test Order serialization"() {

        when:
            byte [] serializedOrder = OrderSerializer.serialize(order)

        then:
            noExceptionThrown()
            serializedOrder != null
    }

    def "Test Order de-serialization"() {

        when:
            byte [] serializedOrder = OrderSerializer.serialize(order)

        then:
            noExceptionThrown()
            serializedOrder != null

        when:
            Order deserializedOrder = OrderDeserializer.deserialize(serializedOrder)

        then:
            noExceptionThrown()
            deserializedOrder != null
            deserializedOrder.getId().equals("randomId")
    }
}
