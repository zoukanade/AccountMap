package zou.AccountMap.command;


import zou.AccountMap.AccountMap;
import zou.AccountMap.users.Account;

public class DefaultPermissionHandler implements PermissionHandler {
    @Override
    public boolean EnablePermissionCommand() {
        return true;
    }

    @Override
    public boolean checkPermission(Account account, Account targetAccount, String permissionNode, String permissionNodeTargeted) {
        if(account == null)
            return true;

        if((account.hasPermission(permissionNode)))
            return true;

        AccountMap.getLogger().info("commands.generic.permission_error");
        return false;
    }
}
