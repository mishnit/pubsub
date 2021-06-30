import courier.manager.CourierManager;
import orderdispatcher.OrderDispatcher;
import shelf.manager.ShelfManager;


public class MainApp {

    /**
     * The main class for running the application.
     *
     * Instantiates the dispatcher, subscriber for shelf and the courier manager/delivery.
     * @param args
     */
    public static void main(String[] args) {

        OrderDispatcher.getOrCreate();

        ShelfManager.getOrCreate();

        CourierManager.getOrCreate();
    }
}
