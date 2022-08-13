package zou.AccountMap.server.http.handlers;

import express.Express;
import express.http.Request;
import express.http.Response;
import io.javalin.Javalin;
import zou.AccountMap.database.DatabaseHelper;
import zou.AccountMap.server.http.Router;
import zou.AccountMap.server.http.objects.JsonResponse;
import zou.AccountMap.users.Account;

import javax.servlet.http.Cookie;
import java.util.Objects;

public final class LoginHandler implements Router {
    @Override
    public void applyRoutes(Express express, Javalin handle) {
        express.all("/login", LoginHandler::login);
        express.all("/outLogin", LoginHandler::outLogin);
    }

    private static void login(Request request, Response response) {
        if(isLogin(request.cookie("token"))){
            JsonResponse res = new JsonResponse(403, "you are already logged in", null);
            response.status(403);
            response.json(res);
            return;
        }
        //Get username and password
        String username = request.query("username");
        String password = request.query("password");
        //username is empty
        if(username == null){
            JsonResponse res = new JsonResponse(400, "username is empty", null);
            response.status(400);
            response.json(res);
            return;
        }
        //username is existed
        Account account = DatabaseHelper.getAccountByUserName(username);
        if(account == null){
            JsonResponse res = new JsonResponse(403, "username not exist", null);
            response.status(403);
            response.json(res);
            return;
        }

        if(!account.getPassword().equals(password)){
            JsonResponse res = new JsonResponse(403, "username or password error", null);
            response.status(403);
            response.json(res);
            return;
        }

        String token = account.generateLoginToken();
        JsonResponse res = new JsonResponse(token);

        response.cookie("token", token);
        response.json(res);
    }
    private static void outLogin(Request request, Response response){
        if(!isLogin(request.cookie("token"))){
            JsonResponse res = new JsonResponse(403, "Not logged in", null);
            response.status(403);
            response.json(res);
            return;
        }
        response.clearCookie("token", "/");
        JsonResponse res = new JsonResponse(null);
        response.json(res);
    }

    public static boolean isLogin(String token){
        if(token == null)
            return false;

        if(DatabaseHelper.getAccountByToken(token) == null)
            return false;

        return true;
    }
}
