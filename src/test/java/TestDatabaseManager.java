import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Test suite for database manager
 */
public class TestDatabaseManager {

    private DatabaseManager db;

    @BeforeTest
    private void initialize() throws IOException {
        db = new DatabaseManager("weightwatcher");
    }

    @Test
    public void testConnect() throws FileNotFoundException, URISyntaxException, SQLException {
        Assert.assertEquals(db.connect(), 0);
        db.initializeTables();
    }

    @AfterTest
    private void close() throws SQLException {
        db.close();
    }
}
