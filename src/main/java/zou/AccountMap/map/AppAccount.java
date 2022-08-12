package zou.AccountMap.map;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import zou.AccountMap.database.DatabaseHelper;
import zou.AccountMap.users.Account;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Entity(value = "appAccount", useDiscriminator = false)
public class AppAccount {
    @Id
    private ObjectId id;
    @Indexed
    private String ownerId;
    private String appId;

    private String account;
    private String password;


    private List<ObjectId> bindAppAccount;

    public AppAccount(){
        bindAppAccount = new ArrayList<>();
    }

    public ObjectId getId() {
        return this.id;
    }

    public String getOwnerId() {
        return ownerId;
    }
    public boolean setOwner(Account account) {
        if(account == null)
            return false;

        this.ownerId = account.getId();
        return true;
    }

    public String getAppId() {
        return appId;
    }
    public boolean setAppId(Application app) {
        if(app == null)
            return false;

        this.appId = app.getId();
        return true;
    }

    public String getAccount(){
        return this.account;
    }
    public void setAccount(String account){
        this.account = account;
    }

    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password){
        this.password = password;
    }

    public List<ObjectId> getBindAppAccount(){
        return bindAppAccount;
    }
    public boolean addBindAppAccount(AppAccount appAccount){
        AppAccount exists = DatabaseHelper.getAppAccountById(appAccount.getId());
        if (exists == null) {
            return false;
        }
        bindAppAccount.add(appAccount.getId());
        return true;
    }
    public boolean delBindAppAccount(AppAccount appAccount){
        return bindAppAccount.remove(appAccount.getId());
    }



    public static boolean createAppAccount(Account ownerAccount, Application ownerApp, String account, String password)
    {
        return DatabaseHelper.createAppAccount(ownerAccount, ownerApp, account, password) != null;
    }




}
