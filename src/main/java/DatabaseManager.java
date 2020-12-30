import javax.annotation.Resources;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by kenbutler on 12/27/20.
 */
public class DatabaseManager {

    // JDBC driver URL and table definition
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    private static final String TBL_CLIENT = "Client";
    private static final String TBL_WEIGHT = "Weight";

    // Connection
    private String dbName;
    private Connection conn;
    private Statement stmt;
    private Logger logger;

    DatabaseManager(String databaseName) throws IOException {

        dbName = databaseName.toLowerCase();

        // Initialize logger through handler and formatter
        logger = Logger.getLogger(dbName.substring(0, 1).toUpperCase() + dbName.substring(1) + "Log");

        // Configure handler and formatter
        FileHandler fh = new FileHandler("/tmp/" + dbName + ".log");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        logger.info("Logger initialized");
    }

    int connect() throws FileNotFoundException, URISyntaxException, SQLException {

        if (conn != null) {  // Only connect once per instance
            logger.info("Connection already exists");
            return 1;
        }

        // Read Postgres user credentials from file in resources directory
        URL resource = this.getClass().getClassLoader().getResource("postgres");
        if (resource == null) {
            logger.info("Failed to find postgres credentials");
            return -1;
        }
        logger.info("Reading credentials from " + resource.toString());
        Scanner sc;
        File file = new File(resource.toURI());
        sc = new Scanner(file);
        List<String> lines = new ArrayList<String>();
        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }
        String[] info = lines.toArray(new String[0]);

        // Set Postgres user properties
        Properties props = new Properties();
        props.setProperty("user", info[0]);
        props.setProperty("password", info[1]);
        props.setProperty("sslmode", "disable");

        // Open a connection
        conn = DriverManager.getConnection("jdbc:postgresql://localhost/" + dbName, props);
        stmt = conn.createStatement();  // Use to execute queries

        return 0;
    }

    /**
     * Create SQL table
     *
     * @param tableName Name of SQL table
     * @param sqlCreate SQL code used for creation
     */
    private void createTable(String tableName, String sqlCreate) {

        try {
            Statement stmt = conn.createStatement();

            // Drop table if it exists
            String sql = "DROP TABLE IF EXISTS " + tableName + " CASCADE;";
            stmt.executeUpdate(sql);

            // Create table
            System.out.print("Creating " + tableName + " table...");
            stmt.executeUpdate(sqlCreate);
            System.out.println("SUCCESS");

        } catch (SQLException e) {
            System.out.println("!! FAILURE !!");
            e.printStackTrace();
        }
    }

    /**
     * Initialize database properties
     *
     * @return Integer representing success
     */
    public int initializeTables() {

        try {

            // Create tables
            String tableName;
            String sql;

            // Create weight table
            tableName = "Weight";
            sql = "CREATE TABLE " + tableName + " (" +
                    "  uid UUID PRIMARY KEY UNIQUE NOT NULL, " +
                    "  name CHARACTER(30) NOT NULL, " +
                    "  species UNSIGNED NOT NULL " +
                    ");";
            createTable(tableName, sql);

            // Create health records table
            tableName = "Record";
            sql = "CREATE TABLE " + tableName + " (" +
                    "  uid UUID PRIMARY KEY UNIQUE NOT NULL, " +
                    "  date DATE NOT NULL " +
                    "  weight FLOAT NOT NULL " +
                    ");";
            createTable(tableName, sql);

        } catch (Exception e) {

            // Handle errors for Class.forName
            e.printStackTrace();
            return -1;

        }

        return 0;

    }

    /**
     * Close database connection
     */
    void close() throws SQLException {

        if (conn == null) {
            logger.info("No connection exists to close");
            return;
        }

        logger.info("Closing connection to database...");

        // Close SQL statement
        if (stmt != null)
            stmt.close();

        // Close connection
        if (conn != null)
            conn.close();

        logger.info("Database successfully closed");
    }

}
