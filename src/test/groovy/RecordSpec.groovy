import pubSub.Record
import spock.lang.Specification


class RecordSpec extends Specification {

    def "Test record"() {

        when:
            Record<String, String> record = new Record("key1", "value1")

        then:
            noExceptionThrown()
            record.getKey().equals("key1")
            record.getValue().equals("value1")
    }
}
