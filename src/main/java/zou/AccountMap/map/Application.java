package zou.AccountMap.map;

import com.mongodb.client.result.DeleteResult;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import zou.AccountMap.database.DatabaseHelper;
import zou.AccountMap.database.DatabaseManager;

@Entity(value = "application", useDiscriminator = false)
public class Application {
    @Id
    private String id;
    @Indexed(options = @IndexOptions(unique = true))
    private String appName;
    private String notes;
    public Application(){

    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return this.appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getNotes() {
        return this.notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }



    public static boolean addApplication(String appName, String notes){
        if(DatabaseHelper.getApplicationByAppName(appName) == null)
            return false;

        Application application = new Application();
        application.setAppName(appName);
        application.setNotes(notes);
        DatabaseHelper.saveApplication(application);
        return true;
    }
    public static boolean deleteApplication(Application application){
        DeleteResult result = DatabaseManager.getDatastore().delete(application);
        return result.wasAcknowledged();
    }

}
