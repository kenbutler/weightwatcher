package weightwatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


/**
 * Database manager for WeightWatcher application
 * <p>
 * The weightwatcher database must be manually created first.
 * CREATE DATABASE weightwatcher
 * Ideally this would be created under the 'postgres' user.
 * To access the postgres database from the command line, type:
 * psql weightwatcher postgres
 */
class DatabaseManager {

    // JDBC driver URL and table definition
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    private static final String TBL_CLIENT = "Client";
    private static final String TBL_WEIGHT = "Weight";
    private static final String SEQ_ANIMAL = "animal_sequence";
    private static final String SEQ_RECORD = "record_sequence";

    // Connection
    private String dbName;
    private Connection conn;
    private Statement stmt;
    private Logger logger;

    public static enum AnimalSpecies {
        DOG(0),
        CAT(1);

        private final int code;

        AnimalSpecies(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }
    }

    /**
     * Ctor
     *
     * @param databaseName Name of existing postgres database
     */
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

    /**
     * Connect to postgres database.
     * Requires that postgres credentials file exists within resources directory.
     */
    boolean connect() throws FileNotFoundException, URISyntaxException, SQLException {

        if (conn != null) {  // Only connect once per instance
            logger.info("Connection already exists");
            return true;
        }

        // Read Postgres user credentials from file in resources directory
        URL resource = this.getClass().getClassLoader().getResource("postgres");
        if (resource == null) {
            throw new FileNotFoundException("Failed to find postgres credentials");
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

        return conn.isValid(3);
    }

    /**
     * Create SQL table
     *
     * @param tableName Name of SQL table
     * @param sqlCreate SQL code used for creation
     */
    private void createTable(String tableName, String sqlCreate) throws SQLException {
        // Create table
        stmt.executeUpdate(sqlCreate);
        logger.info("Created '" + tableName + " table in " + dbName + " database");
    }

    /**
     * Reset database by dropping all tables
     */
    void reset() throws SQLException {
        String sql;
        // TODO: Loop through a list of tables to drop/delete in the future

        // Drop client table
        logger.info("Dropping '" + TBL_CLIENT + "' SQL table");
        sql = "DROP TABLE IF EXISTS " + TBL_CLIENT + " CASCADE;";
        stmt.executeUpdate(sql);

        // Drop weight table
        logger.info("Dropping '" + TBL_WEIGHT + "' SQL table");
        sql = "DROP TABLE IF EXISTS " + TBL_WEIGHT + " CASCADE;";
        stmt.executeUpdate(sql);

        // Drop client sequence
        logger.info("Dropping " + SEQ_ANIMAL + " SQL sequence");
        sql = "DROP SEQUENCE IF EXISTS " + SEQ_ANIMAL + ";";
        stmt.executeUpdate(sql);

        // Drop record sequence
        logger.info("Dropping " + SEQ_RECORD + " SQL sequence");
        sql = "DROP SEQUENCE IF EXISTS " + SEQ_RECORD + ";";
        stmt.executeUpdate(sql);
    }

    /**
     * Initialize database properties
     */
    void initializeTables() throws SQLException {

        // Create tables
        String tableName;
        String sql;

        // Create sequences for client and record IDs
        // The sequence start cannot be lower than 1
        sql = "CREATE SEQUENCE IF NOT EXISTS " + SEQ_ANIMAL + " start 1 increment 1";
        stmt.executeUpdate(sql);
        logger.info("Created " + SEQ_ANIMAL + " sequence");
        sql = "CREATE SEQUENCE IF NOT EXISTS " + SEQ_RECORD + " start 1 increment 1";
        stmt.executeUpdate(sql);
        logger.info("Created " + SEQ_RECORD + " sequence");

        // Create client table
        // TODO: Add 'owner' as unique key
        tableName = TBL_CLIENT;
        sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "  animal_id SERIAL PRIMARY KEY UNIQUE NOT NULL, " +
                "  name VARCHAR(30) NOT NULL, " +
                "  species INTEGER NOT NULL, " +
                "  breed VARCHAR(30) NOT NULL " +
                ");";
        createTable(tableName, sql);
        logger.info("Created '" + tableName + "' table");

        // Create weight table
        tableName = TBL_WEIGHT;
        sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "  record_id SERIAL PRIMARY KEY UNIQUE NOT NULL, " +
                "  animal_id SERIAL REFERENCES " + TBL_CLIENT + " (animal_id), " +
                "  date DATE NOT NULL, " +
                "  weight FLOAT NOT NULL " +
                ");";
        createTable(tableName, sql);
        logger.info("Created '" + tableName + "' table");
    }

    /**
     * Add client to database
     *
     * @param client Client instance
     */
    void addClient(Client client) throws SQLException {
        // I do not expect any conflict to occur here, as the unique key is auto-incremented
        String sql = "INSERT INTO " + TBL_CLIENT + " (animal_id, name, species, breed) VALUES (nextval(?), ?, ?, ?);";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, SEQ_ANIMAL);
        pstmt.setString(2, client.getName());
        pstmt.setInt(3, client.getSpecies().getCode());
        pstmt.setString(4, client.getBreed());
        pstmt.executeUpdate();
    }

    /**
     * Get details on current client list
     * @return List of clients
     */
    List<Client> getClients() throws SQLException {
        logger.info("Retrieving clients...");
        String sql = "SELECT * FROM " + TBL_CLIENT + ";";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        // Populate client list from query
        List<Client> clients = new ArrayList<Client>();
        while (rs.next()) {
            Client client = new Client(rs.getString(2), AnimalSpecies.values()[rs.getInt(3)], rs.getString(4));
            clients.add(client);
            logger.info("Retrieved client " + client.getName() + " (" + client.getBreed() + " " + client.getSpecies());
        }
        return clients;
    }

    /**
     * Close database connection
     */
    boolean close() throws SQLException {

        if (conn == null) {
            logger.info("No connection exists to close");
            return true;
        }

        logger.info("Closing connection to database...");

        // Close SQL statement
        if (stmt != null)
            stmt.close();

        // Close connection
        if (conn != null)
            conn.close();

        logger.info("Database successfully closed");
        return conn.isClosed();
    }

}
