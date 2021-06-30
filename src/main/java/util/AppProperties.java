package util;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class for reading/storing application properties.
 * Reads the "app.properties" from the classpath and internally stores the
 * parsed <key, value> pairs.
 */
public class AppProperties {

    private static final AppProperties INSTANCE = new AppProperties();

    private final Properties properties;

    // Assumes this file is packaged in the Jar.
    private final static String PROPERTIES_FILE = "app.properties";

    // Returns the singleton instance.
    public static AppProperties getInstance() {
        return INSTANCE;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Constructor.
     */
    private AppProperties() {

        properties = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);

        try {
            properties.load(input);
        } catch (IOException ioe) {

            // Exit the application since we cannot proceed if we fail here.
            System.out.println("*** Error loading app.properties..Exiting *** " + ioe.getMessage());
            System.exit(0);
        }
    }
}
