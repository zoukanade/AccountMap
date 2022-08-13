package zou.AccountMap.command;


import zou.AccountMap.users.Account;

public interface PermissionHandler {
    boolean EnablePermissionCommand();

    boolean checkPermission(Account account, Account targetAccount, String permissionNode, String permissionNodeTargeted);
}
