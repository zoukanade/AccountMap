package zou.AccountMap.command;

import zou.AccountMap.users.Account;

import java.util.List;

public interface CommandHandler {
    default void execute(Account sender, Account targetAccount, List<String> args) {
    }

}
