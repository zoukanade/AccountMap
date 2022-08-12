package zou.AccountMap.utils;

import zou.AccountMap.AccountMap.ServerDebugMode;
public class ConfigContainer {

    public Database databaseInfo = new Database();
    public Server server = new Server();

    public static class Database{
        public DataStore database = new DataStore();
        public static class DataStore{
            public String connectionUri = "mongodb://localhost:27017";
            public String collection = "AccountMap";
        }
    }
    public static class Server{
        public ServerDebugMode debugLevel = ServerDebugMode.NONE;
        public HTTP http = new HTTP();
    }
    public static class HTTP{
        public String bindAddress = "0.0.0.0";
        public String accessAddress = "127.0.0.1";
        public int bindPort = 443;
        public int accessPort = 0;
        public Encryption encryption = new Encryption();
        public Policies policies = new Policies();
        public Files files = new Files();
    }
    public static class Encryption {
        public boolean useEncryption = true;
        /* Should 'https' be appended to URLs? */
        public boolean useInRouting = true;
        public String keystore = "./keystore.p12";
        public String keystorePassword = "123456";
    }
    public static class Policies {
        public Policies.CORS cors = new Policies.CORS();

        public static class CORS {
            public boolean enabled = false;
            public String[] allowedOrigins = new String[]{"*"};
        }
    }
    public static class Files{
        public String indexFile = "./index.html";
        public String errorFile = "./404.html";
    }
}
