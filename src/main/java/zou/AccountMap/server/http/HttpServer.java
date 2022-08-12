package zou.AccountMap.server.http;
import io.javalin.Javalin;
import zou.AccountMap.AccountMap;
import express.Express;
import express.http.MediaType;
import zou.AccountMap.utils.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import zou.AccountMap.AccountMap.ServerDebugMode;

import java.io.File;

import static zou.AccountMap.Configuration.*;

public class HttpServer {
    private final Express express;
    public HttpServer() {
        this.express = new Express(config -> {
            // Set the Express HTTP server.
            config.server(HttpServer::createServer);

            // Configure encryption/HTTPS/SSL.
            config.enforceSsl = HTTP_ENCRYPTION.useEncryption;

            // Configure HTTP policies.
            if (HTTP_POLICIES.cors.enabled) {
                var allowedOrigins = HTTP_POLICIES.cors.allowedOrigins;
                if (allowedOrigins.length > 0)
                    config.enableCorsForOrigin(allowedOrigins);
                else config.enableCorsForAllOrigins();
            }

            // Configure debug logging.
            if (SERVER.debugLevel == ServerDebugMode.ALL)
                config.enableDevLogging();

            // Disable compression on static files.
            config.precompressStaticFiles = false;
        });
    }

    @SuppressWarnings("resource")
    private static Server createServer() {
        Server server = new Server();
        ServerConnector serverConnector
                = new ServerConnector(server);

        if (HTTP_ENCRYPTION.useEncryption) {
            var sslContextFactory = new SslContextFactory.Server();
            var keystoreFile = new File(HTTP_ENCRYPTION.keystore);

            if (!keystoreFile.exists()) {
                HTTP_ENCRYPTION.useEncryption = false;
                HTTP_ENCRYPTION.useInRouting = false;

                AccountMap.getLogger().warn("messages.dispatch.keystore.no_keystore_error");
            } else try {
                sslContextFactory.setKeyStorePath(keystoreFile.getPath());
                sslContextFactory.setKeyStorePassword(HTTP_ENCRYPTION.keystorePassword);
            } catch (Exception ignored) {
                AccountMap.getLogger().warn("messages.dispatch.keystore.password_error");

                try {
                    sslContextFactory.setKeyStorePath(keystoreFile.getPath());
                    sslContextFactory.setKeyStorePassword("123456");

                    AccountMap.getLogger().warn("messages.dispatch.keystore.default_password");
                } catch (Exception exception) {
                    AccountMap.getLogger().warn("messages.dispatch.keystore.general_error", exception);
                }
            } finally {
                serverConnector = new ServerConnector(server, sslContextFactory);
            }
        }

        serverConnector.setPort(HTTP_INFO.bindPort);
        server.setConnectors(new ServerConnector[]{serverConnector});

        return server;
    }

    public Javalin getHandle() {
        return this.express.raw();
    }

    @SuppressWarnings("UnusedReturnValue")
    public HttpServer addRouter(Class<? extends Router> router, Object... args) {
        // Get all constructor parameters.
        Class<?>[] types = new Class<?>[args.length];
        for (var argument : args)
            types[args.length - 1] = argument.getClass();

        try { // Create a router instance & apply routes.
            var constructor = router.getDeclaredConstructor(types); // Get the constructor.
            var routerInstance = constructor.newInstance(args); // Create instance.
            routerInstance.applyRoutes(this.express, this.getHandle()); // Apply routes.
        } catch (Exception exception) {
            AccountMap.getLogger().warn("messages.dispatch.router_error", exception);
        }
        return this;
    }
    public void start() {
        // Attempt to start the HTTP server.
        if (HTTP_INFO.bindAddress.equals("")) {
            this.express.listen(HTTP_INFO.bindPort);
        } else {
            this.express.listen(HTTP_INFO.bindAddress, HTTP_INFO.bindPort);
        }

        // Log bind information.
        AccountMap.getLogger().info(String.format("messages.dispatch.port_bind%s", Integer.toString(this.express.raw().port())));
    }

    public static class DefaultRequestRouter implements Router {
        @Override
        public void applyRoutes(Express express, Javalin handle) {
            express.get("/", (request, response) -> {
                File file = new File(HTTP_STATIC_FILES.indexFile);
                if (!file.exists())
                    response.send("""
                        <!DOCTYPE html>
                        <html>
                            <head>
                                <meta charset="utf8">
                            </head>
                            <body>%s</body>
                        </html>
                        """.formatted("messages.status.welcome"));
                else {
                    final var filePath = file.getPath();
                    final MediaType fromExtension = MediaType.getByExtension(filePath.substring(filePath.lastIndexOf(".") + 1));
                    response.type((fromExtension != null) ? fromExtension.getMIME() : "text/plain")
                            .send(FileUtils.read(filePath));
                }
            });
        }
    }
    public static class UnhandledRequestRouter implements Router {
        @Override
        public void applyRoutes(Express express, Javalin handle) {
            handle.error(404, context -> {
                if (SERVER.debugLevel == ServerDebugMode.MISSING)
                    AccountMap.getLogger().info("messages.dispatch.unhandled_request_error", context.method(), context.url());
                context.contentType("text/html");

                File file = new File(HTTP_STATIC_FILES.errorFile);
                if (!file.exists())
                    context.result("""
                        <!DOCTYPE html>
                        <html>
                            <head>
                                <meta charset="utf8">
                            </head>

                            <body>
                                <img src="https://http.cat/404" />
                            </body>
                        </html>
                        """);
                else {
                    final var filePath = file.getPath();
                    final MediaType fromExtension = MediaType.getByExtension(filePath.substring(filePath.lastIndexOf(".") + 1));
                    context.contentType((fromExtension != null) ? fromExtension.getMIME() : "text/plain")
                            .result(FileUtils.read(filePath));
                }
            });
        }
    }
}
