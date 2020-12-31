package weightwatcher;

/**
 * Created by kenbutler on 12/31/20.
 */
public class Client {

    private String name;  // Name of pet
    private DatabaseManager.AnimalSpecies species;  // Species of pet (e.g. dog, cat, bird, etc.)
    private String breed;  // Breed of pet (e.g. if species is 'dog' then breed may be 'labrador'

    public Client (String name, DatabaseManager.AnimalSpecies species, String breed) {
        this.name = name;
        this.species = species;
        this.breed = breed;
    }

    public String getName() {
        return name;
    }

    public DatabaseManager.AnimalSpecies getSpecies() {
        return species;
    }

    public String getBreed() {
        return breed;
    }
}
