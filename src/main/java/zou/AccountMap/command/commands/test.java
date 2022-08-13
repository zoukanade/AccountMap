package zou.AccountMap.command.commands;

import zou.AccountMap.AccountMap;
import zou.AccountMap.command.Command;
import zou.AccountMap.command.CommandHandler;
import zou.AccountMap.users.Account;

import java.util.List;

@Command(label = "test", usage = "usage.test", permission = "server.test", description = "description.test")
public class test implements CommandHandler {
    public void execute(Account sender, Account targetAccount, List<String> args){
        AccountMap.getLogger().info("Hello World! Command.test");
    }
}
