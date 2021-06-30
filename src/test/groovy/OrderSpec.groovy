import models.Order
import orderdispatcher.OrderDispatcher
import pubSub.Publisher
import spock.lang.Specification
import util.AppProperties;


class OrderSpec extends Specification {

    def "Test reading and dispatching orders"() {

        when:
            Order order = new Order("randomId", "testOrder", "hot", 100, 0.25)
            order.constructOrderValue()

        then:
            // This should execute within a second, hence order value should be > 0.5
            order.getOrderValue() > 0.5
            order.getShelfLife() == 100
            order.getId().equals("randomId")
            order.getName().equals("testOrder")
            order.getTemperature().equals("hot")
            order.getDecayRate() == 0.25
            order.expired() == false

    }
}
