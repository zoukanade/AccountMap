package zou.AccountMap.database;

import dev.morphia.query.experimental.filters.Filters;
import zou.AccountMap.map.AppAccount;
import zou.AccountMap.map.Application;
import zou.AccountMap.users.Account;
import org.bson.types.ObjectId;

import javax.xml.crypto.Data;

public final class DatabaseHelper {
    public static Account createAccount(String username, String password) {
        // Unique names only
        Account exists = DatabaseHelper.getAccountByName(username);
        if (exists != null) {
            return null;
        }

        // Account
        Account account = new Account();
        account.setId(Integer.toString(DatabaseManager.getNextId(account)));
        account.setUsername(username);
        account.setPassword(password);
        DatabaseHelper.saveAccount(account);
        return account;
    }
    public static void saveAccount(Account account) {
        DatabaseManager.getDatastore().save(account);
    }
    public static Account getAccountByName(String username) {
        return DatabaseManager.getDatastore().find(Account.class).filter(Filters.eq("username", username)).first();
    }

    public static Application createApplication(String appName, String notes){
        Application exists = getApplicationByAppName(appName);
        if (exists != null) {
            return null;
        }
        Application app = new Application();
        app.setId(Integer.toString(DatabaseManager.getNextId(app)));
        app.setAppName(appName);
        app.setNotes(notes);
        saveApplication(app);
        return app;
    }
    public static void saveApplication(Application application){
        DatabaseManager.getDatastore().save(application);
    }
    public static Application getApplicationByAppName(String AppName){
        return DatabaseManager.getDatastore().find(Application.class).filter(Filters.eq("appName", AppName)).first();
    }

    public static AppAccount createAppAccount(Account ownerAccount, Application ownerApp, String account, String password){
        if((ownerAccount == null) || (ownerApp == null))
            return null;

        AppAccount appAccount = new AppAccount();
        appAccount.setOwner(ownerAccount);
        appAccount.setAppId(ownerApp);
        appAccount.setAccount(account);
        appAccount.setPassword(password);
        return appAccount;
    }
    public static void saveAppAccount(AppAccount appAccount){
        DatabaseManager.getDatastore().save(appAccount);
    }
    public static AppAccount getAppAccountById(ObjectId id){
        return DatabaseManager.getDatastore().find(AppAccount.class).filter(Filters.eq("_id", id)).first();
    }
}
