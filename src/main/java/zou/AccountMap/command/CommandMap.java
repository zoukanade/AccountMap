package zou.AccountMap.command;

import org.reflections8.Reflections;
import zou.AccountMap.AccountMap;
import zou.AccountMap.database.DatabaseHelper;
import zou.AccountMap.users.Account;
import zou.AccountMap.utils.ConfigContainer;

import java.util.*;

public class CommandMap {
    private final Map<String, CommandHandler> commands = new HashMap<>();
    private final Map<String, CommandHandler> aliases = new HashMap<>();
    private final Map<String, Command> annotations = new HashMap<>();

    public CommandMap(){
        scan();
    }
    public static CommandMap getInstance() {
        return AccountMap.getCommandMap();
    }

    public CommandMap registerCommand(String label, CommandHandler command) {
        AccountMap.getLogger().debug("Registered command: " + label);

        // Get command data.
        Command annotation = command.getClass().getAnnotation(Command.class);
        this.annotations.put(label, annotation);
        this.commands.put(label, command);

        // Register aliases.//别名
        if (annotation.aliases().length > 0) {
            for (String alias : annotation.aliases()) {
                this.aliases.put(alias, command);
                this.annotations.put(alias, annotation);
            }
        }
        return this;
    }
    public CommandMap unregisterCommand(String label) {
        AccountMap.getLogger().debug("Unregistered command: " + label);

        CommandHandler handler = this.commands.get(label);
        if (handler == null) return this;

        Command annotation = handler.getClass().getAnnotation(Command.class);
        this.annotations.remove(label);
        this.commands.remove(label);

        // Unregister aliases.
        if (annotation.aliases().length > 0) {
            for (String alias : annotation.aliases()) {
                this.aliases.remove(alias);
                this.annotations.remove(alias);
            }
        }

        return this;
    }

    public List<Command> getAnnotationsAsList() {
        return new LinkedList<>(this.annotations.values());
    }
    public HashMap<String, Command> getAnnotations() {
        return new LinkedHashMap<>(this.annotations);
    }
    public List<CommandHandler> getHandlersAsList() {
        return new LinkedList<>(this.commands.values());
    }
    public HashMap<String, CommandHandler> getHandlers() {
        return new LinkedHashMap<>(this.commands);
    }
    public CommandHandler getHandler(String label) {
        return this.commands.get(label);
    }

    public Account getTargetAccount(Account account, Account targetAccount, List<String> args){
        for (int i = 0; i < args.size(); i++) {
            String uid = args.get(i);
            if(uid.startsWith("@")){
                uid = args.remove(i).substring(1);
                if (uid.equals(""))
                    return null;

                targetAccount = DatabaseHelper.getAccountById(uid);
                if(targetAccount != null)
                    return targetAccount;
                else
                    return null;

            }
        }
        return null;
    }

    public void invoke(Account account, Account targetAccount, String rawMessage) {
        rawMessage = rawMessage.trim();
        if (rawMessage.length() == 0) {
            AccountMap.getLogger().info("commands.generic.not_specified");
            return;
        }
        String[] split = rawMessage.split(" ");
        List<String> args = new LinkedList<>(Arrays.asList(split));
        String label = args.remove(0);



        //Get command handler
        CommandHandler handler = this.commands.get(label);
        if(handler == null)
            handler = this.aliases.get(label);
        if(handler == null){
            AccountMap.getLogger().info("commands.generic.unknown_command");
            return;
        }
        Command annotation = this.annotations.get(label);

        //检查是否有权限@其他用户
        if(targetAccount == null){
            if(account != null){
                if(!account.hasPermission("server.usingUser")){
                    AccountMap.getLogger().info("commands.generic.permission_error");
                    return;
                }
            }
            //Resolve targetAccount
            targetAccount = this.getTargetAccount(account, targetAccount, args);
            if(targetAccount == null){
                AccountMap.getLogger().error("commands.execution.need_target");
                return;
            }

        }

        // Check for permissions.
        if (!AccountMap.getPermissionHandler().checkPermission(account, targetAccount, annotation.permission(), "")) {
            return;
        }
        //Run
        final Account targetAccountF = targetAccount;
        final CommandHandler handlerF = handler;

        Runnable runnable = () -> handlerF.execute(account, targetAccountF, args);
        if (annotation.threading()) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }
    private void scan() {
        Reflections reflector = AccountMap.reflector;
        Set<Class<?>> classes = reflector.getTypesAnnotatedWith(Command.class);

        classes.forEach(annotated -> {
            try {
                Command cmdData = annotated.getAnnotation(Command.class);
                Object object = annotated.getDeclaredConstructor().newInstance();
                if (object instanceof CommandHandler)
                    this.registerCommand(cmdData.label(), (CommandHandler) object);
                else AccountMap.getLogger().error("Class " + annotated.getName() + " is not a CommandHandler!");
            } catch (Exception exception) {
                AccountMap.getLogger().error("Failed to register command handler for " + annotated.getSimpleName(), exception);
            }
        });
    }
}
