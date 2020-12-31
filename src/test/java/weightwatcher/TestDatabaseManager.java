package weightwatcher;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Test suite for database manager
 */
public class TestDatabaseManager {

    private DatabaseManager db;

    @BeforeSuite
    private void initialize() throws IOException, SQLException {
        // NOTE: This database must be manually created if using unit tests
        db = new DatabaseManager("test_weightwatcher");
    }

    @Test(priority = 1)
    public void testConnect() throws FileNotFoundException, URISyntaxException, SQLException {
        Assert.assertTrue(db.connect());
        db.initializeTables();
    }

    @Test(priority = 2)
    public void testAddClient() throws SQLException {

        // Clients to add
        List<Client> clientsToAdd = new ArrayList<Client>();
        Client client1 = new Client("Smokey", DatabaseManager.AnimalSpecies.DOG, "Cairn Terrier");
        clientsToAdd.add(client1);
        Client client2 = new Client("Maui", DatabaseManager.AnimalSpecies.DOG, "Shih Tzu");
        clientsToAdd.add(client2);
        Client client3 = new Client("Ether", DatabaseManager.AnimalSpecies.CAT, "Tuxedo Cat");
        clientsToAdd.add(client3);
        Client client4 = new Client("Zena", DatabaseManager.AnimalSpecies.DOG, "Blue Heeler");
        clientsToAdd.add(client4);
        Client client5 = new Client("Spookers", DatabaseManager.AnimalSpecies.CAT, "Japanese Bob Tail");
        clientsToAdd.add(client5);
        Client client6 = new Client("Tuna", DatabaseManager.AnimalSpecies.CAT, "Norwegian Forest Cat");
        clientsToAdd.add(client6);

        // Add clients
        for (Client c: clientsToAdd) {
            db.addClient(c);
        }

        // Query table to check results
        List<Client> res = db.getClients();
        for (int i = 0; i < res.size(); i++) {
            Client c = res.get(i);
            System.out.println("Found client " + c.getName() + " (" + c.getBreed() + " " + c.getSpecies() + ")");
            Assert.assertEquals(c.getName(), clientsToAdd.get(i).getName());
            Assert.assertEquals(c.getSpecies(), clientsToAdd.get(i).getSpecies());
            Assert.assertEquals(c.getBreed(), clientsToAdd.get(i).getBreed());
        }
    }

    @AfterSuite
    private void close() throws SQLException {
        db.reset();
        Assert.assertTrue(db.close());
    }
}
