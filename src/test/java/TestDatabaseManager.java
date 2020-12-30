import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import weight_watcher.DatabaseManager;

import java.io.IOException;
import java.sql.Connection;

/**
 * Created by kenbutler on 12/28/20.
 */
public class TestDatabaseManager {

    DatabaseManager db;

    @BeforeTest
    private void initialize() throws IOException {
        db = new DatabaseManager("weightwatcher");
    }

    @Test
    public void testConnection() {
        Assert.assertEquals(db.connect(), 0);
    }
}
