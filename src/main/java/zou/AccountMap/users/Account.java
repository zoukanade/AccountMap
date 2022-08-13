package zou.AccountMap.users;
import dev.morphia.annotations.*;
import zou.AccountMap.AccountMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static zou.AccountMap.Configuration.*;
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

    public List<String> getPermissions() {
        return this.permissions;
    }
    public boolean addPermission(String permission) {
        if (this.permissions.contains(permission)) return false;
        this.permissions.add(permission);
        return true;
    }
    public boolean removePermission(String permission) {
        return this.permissions.remove(permission);
    }

    public String getBanReason(){return this.banReason;}
    public void setBanReason(String banReason){this.banReason = banReason;}

    public boolean getIsBanned(){return this.isBanned;}
    public void setIsBanned(boolean isBanned){this.isBanned = isBanned;}

    public static boolean permissionMatchesWildcard(String wildcard, String[] permissionParts) {
        String[] wildcardParts = wildcard.split("\\.");
        if (permissionParts.length < wildcardParts.length) {  // A longer wildcard can never match a shorter permission
            return false;
        }
        for (int i = 0; i < wildcardParts.length; i++) {
            switch (wildcardParts[i]) {
                case "**":  // Recursing match
                    return true;
                case "*":  // Match only one layer
                    if (i >= (permissionParts.length - 1)) {
                        return true;
                    }
                    break;
                default:  // This layer isn't a wildcard, it needs to match exactly
                    if (!wildcardParts[i].equals(permissionParts[i])) {
                        return false;
                    }
            }
        }
        // At this point the wildcard will have matched every layer, but if it is shorter then the permission then this is not a match at this point (no **).
        return (wildcardParts.length == permissionParts.length);
    }
    public boolean hasPermission(String permission) {
        if (this.permissions.contains("*") && this.permissions.size() == 1) return true;

        // Add default permissions if it doesn't exist
        List<String> permissions = Stream.of(this.permissions, Arrays.asList(ACCOUNT.defaultPermissions))
                .flatMap(Collection::stream)
                .distinct().toList();

        if (permissions.contains(permission)) return true;

        String[] permissionParts = permission.split("\\.");
        for (String p : permissions) {
            if (p.startsWith("-") && permissionMatchesWildcard(p.substring(1), permissionParts)) return false;
            if (permissionMatchesWildcard(p, permissionParts)) return true;
        }

        return permissions.contains("*");
    }
}
