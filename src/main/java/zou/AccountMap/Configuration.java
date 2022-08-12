package zou.AccountMap;
import static zou.AccountMap.AccountMap.config;
import zou.AccountMap.utils.ConfigContainer;

public final class Configuration extends ConfigContainer {
    public static final Server SERVER = config.server;
    public static final ConfigContainer.Database DATABASE = config.databaseInfo;
    public static final HTTP HTTP_INFO = config.server.http;
    public static final Encryption HTTP_ENCRYPTION = config.server.http.encryption;
    public static final Policies HTTP_POLICIES = config.server.http.policies;
    public static final Files HTTP_STATIC_FILES = config.server.http.files;
}
