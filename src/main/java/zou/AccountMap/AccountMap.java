package zou.AccountMap;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.reflections8.Reflections;
import zou.AccountMap.command.CommandMap;
import zou.AccountMap.command.DefaultPermissionHandler;
import zou.AccountMap.command.PermissionHandler;
import zou.AccountMap.database.DatabaseHelper;
import zou.AccountMap.database.DatabaseManager;
import zou.AccountMap.server.http.HttpServer;
import zou.AccountMap.server.http.handlers.GenericHandler;
import zou.AccountMap.server.http.handlers.LoginHandler;
import zou.AccountMap.server.http.handlers.User;
import zou.AccountMap.users.Account;
import zou.AccountMap.utils.ConfigContainer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;

import static zou.AccountMap.Configuration.SERVER;


public class AccountMap {
     private static final Logger log = (Logger) LoggerFactory.getLogger(AccountMap.class);
     private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
     private static LineReader consoleLineReader = null;
     private static final File configFile = new File("./config.json");
     public static final Reflections reflector = new Reflections("zou.AccountMap");
     private static final CommandMap commandMap;
     private static PermissionHandler permissionHandler;
     public static ConfigContainer config;
     static{
          AccountMap.loadConfig();

          var mongoLogger = (Logger) LoggerFactory.getLogger("org.mongodb.driver");
          mongoLogger.setLevel(Level.OFF);

          commandMap = new CommandMap();
     }
     public static void main(String[] args){
          DatabaseManager.initialize();

          permissionHandler = new DefaultPermissionHandler();

          HttpServer httpServer = new HttpServer();

          httpServer.addRouter(HttpServer.UnhandledRequestRouter.class);
          httpServer.addRouter(HttpServer.DefaultRequestRouter.class);
          httpServer.addRouter(GenericHandler.class);
          httpServer.addRouter(LoginHandler.class);
          httpServer.addRouter(User.class);
          httpServer.start();

          startConsole();
     }
     
     public static void loadConfig() {
          // Check if config.json exists. If not, we generate a new config.
          if (!configFile.exists()) {
               getLogger().info("config.json could not be found. Generating a default configuration ...");
               config = new ConfigContainer();
               AccountMap.saveConfig(config);
               return;
          }

          // If the file already exists, we attempt to load it.
          try (FileReader file = new FileReader(configFile)) {
               config = gson.fromJson(file, ConfigContainer.class);
          } catch (Exception exception) {
               getLogger().error("There was an error while trying to load the configuration from config.json. Please make sure that there are no syntax errors. If you want to start with a default configuration, delete your existing config.json.");
               System.exit(1);
          }
     }
     public static void saveConfig(@Nullable ConfigContainer config) {
          if (config == null) config = new ConfigContainer();

          try (FileWriter file = new FileWriter(configFile)) {
               file.write(gson.toJson(config));
          } catch (IOException ignored) {
               AccountMap.getLogger().error("Unable to write to config file.");
          } catch (Exception e) {
               AccountMap.getLogger().error("Unable to save config file.", e);
          }
     }

     public static Gson getGsonFactory() {
          return gson;
     }
     public static Logger getLogger(){return log;}
     public static LineReader getConsole() {
          if (consoleLineReader == null) {
               Terminal terminal = null;
               try {
                    terminal = TerminalBuilder.builder().jna(true).build();
               } catch (Exception e) {
                    try {
                         // Fallback to a dumb jline terminal.
                         terminal = TerminalBuilder.builder().dumb(true).build();
                    } catch (Exception ignored) {
                         // When dumb is true, build() never throws.
                    }
               }
               consoleLineReader = LineReaderBuilder.builder()
                       .terminal(terminal)
                       .build();
          }
          return consoleLineReader;
     }
     public static CommandMap getCommandMap(){
          return commandMap;
     }
     public static PermissionHandler getPermissionHandler() {
          return permissionHandler;
     }
     public static void startConsole() {
          getLogger().info("messages.status.done");
          String input = null;
          boolean isLastInterrupted = false;
          while (true) {
               try {
                    input = consoleLineReader.readLine("> ");
               } catch (UserInterruptException e) {
                    if (!isLastInterrupted) {
                         isLastInterrupted = true;
                         AccountMap.getLogger().info("Press Ctrl-C again to shutdown.");
                         continue;
                    } else {
                         Runtime.getRuntime().exit(0);
                    }
               } catch (EndOfFileException e) {
                    AccountMap.getLogger().info("EOF detected.");
                    continue;
               } catch (IOError e) {
                    AccountMap.getLogger().error("An IO error occurred.", e);
                    continue;
               }

               isLastInterrupted = false;
               try {
                    CommandMap.getInstance().invoke(null, null, input);
               } catch (Exception e) {
                    AccountMap.getLogger().error("messages.game.command_error", e);
               }
          }
     }
     public enum ServerDebugMode {
          ALL, MISSING, WHITELIST, BLACKLIST, NONE
     }
}
