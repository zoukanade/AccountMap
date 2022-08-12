package zou.AccountMap;

import ch.qos.logback.classic.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import zou.AccountMap.database.DatabaseManager;
import zou.AccountMap.map.AppAccount;
import zou.AccountMap.map.Application;
import zou.AccountMap.server.http.HttpServer;
import zou.AccountMap.users.Account;
import zou.AccountMap.utils.ConfigContainer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import zou.AccountMap.database.DatabaseHelper;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class AccountMap {
     private static final Logger log = (Logger) LoggerFactory.getLogger(AccountMap.class);
     private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
     private static LineReader consoleLineReader = null;
     private static final File configFile = new File("./config.json");
     public static ConfigContainer config;
     static{
          AccountMap.loadConfig();
     }
     public static void main(String[] args){

          DatabaseManager.initialize();

          HttpServer httpServer = new HttpServer();

          httpServer.addRouter(HttpServer.UnhandledRequestRouter.class);
          httpServer.addRouter(HttpServer.DefaultRequestRouter.class);
          httpServer.start();

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

     public enum ServerDebugMode {
          ALL, MISSING, WHITELIST, BLACKLIST, NONE
     }
}
