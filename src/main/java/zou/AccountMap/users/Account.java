package zou.AccountMap.users;
import dev.morphia.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Entity(value = "accounts", useDiscriminator = false)
public class Account {
    @Id
    private String id;

    @Indexed(options = @IndexOptions(unique = true))
    @Collation(locale = "simple", caseLevel = true)
    private String username;
    private String password;
    private String token;
    private String sessionKey;
    private final List<String> permissions;
    private String banReason;
    private boolean isBanned;

    @Deprecated
    public Account() {
        this.permissions = new ArrayList<>();
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return this.token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionKey() {
        return this.sessionKey;
    }
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void addPermission(String permission){

    }
    public boolean removePermission(String permission){
        return false;
    }

    public String getBanReason(){return this.banReason;}
    public void setBanReason(String banReason){this.banReason = banReason;}

    public boolean getIsBanned(){return this.isBanned;}
    public void setIsBanned(boolean isBanned){this.isBanned = isBanned;}
}
