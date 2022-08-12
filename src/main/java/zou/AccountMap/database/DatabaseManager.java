package zou.AccountMap.database;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.experimental.filters.Filters;
import zou.AccountMap.AccountMap;
import zou.AccountMap.map.AppAccount;
import zou.AccountMap.map.Application;
import zou.AccountMap.users.Account;

import static zou.AccountMap.Configuration.DATABASE;
import static zou.AccountMap.Configuration.SERVER;

public final class DatabaseManager {
    private static Datastore datastore;

    private static final Class<?>[] mappedClasses = new Class<?>[] {
            Account.class, Application.class, AppAccount.class
    };

    public static Datastore getDatastore() {
        return datastore;
    }

    public static MongoDatabase getDatabase() {
        return getDatastore().getDatabase();
    }
    public static void test(){
        AccountMap.getLogger().info("test");
    }
    public static void initialize() {
        // Initialize
        MongoClient gameMongoClient = MongoClients.create(DATABASE.database.connectionUri);
        // Set mapper options.
        MapperOptions mapperOptions = MapperOptions.builder()
                .storeEmpties(true).storeNulls(false).build();
        // Create data store.
        datastore = Morphia.createDatastore(gameMongoClient, DATABASE.database.collection, mapperOptions);
        // Map classes.
        datastore.getMapper().map(mappedClasses);
        // Ensure indexes
        try {
            datastore.ensureIndexes();
        } catch (MongoCommandException exception) {
            AccountMap.getLogger().info("Mongo index error: ", exception);
            // Duplicate index error
            if (exception.getCode() == 85) {
                // Drop all indexes and re add them
                MongoIterable<String> collections = datastore.getDatabase().listCollectionNames();
                for (String name : collections) {
                    datastore.getDatabase().getCollection(name).dropIndexes();
                }
                // Add back indexes
                datastore.ensureIndexes();
            }
        }
    }

    public static synchronized int getNextId(Class<?> c) {
        DatabaseCounter counter = getDatastore().find(DatabaseCounter.class).filter(Filters.eq("_id", c.getSimpleName())).first();
        if (counter == null) {
            counter = new DatabaseCounter(c.getSimpleName());
        }
        try {
            return counter.getNextId();
        } finally {
            getDatastore().save(counter);
        }
    }

    public static synchronized int getNextId(Object o) {
        return getNextId(o.getClass());
    }
}
