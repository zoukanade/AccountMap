package zou.AccountMap.server.http.handlers;

import express.Express;
import express.http.Request;
import express.http.Response;
import io.javalin.Javalin;
import zou.AccountMap.database.DatabaseHelper;
import zou.AccountMap.server.http.Router;
import zou.AccountMap.server.http.objects.JsonResponse;
import zou.AccountMap.users.Account;

public final class User implements Router {
    @Override
    public void applyRoutes(Express express, Javalin handle) {
        express.all("register", User::register);
    }

    private static void register(Request request, Response response) {
        //Get username and password
        String username = request.query("username");
        String password = request.query("password");

        if(username == null || password == null){
            JsonResponse res = new JsonResponse(400, "username or password is empty", null);
            response.status(400);
            response.json(res);
            return;
        }

        if(DatabaseHelper.getAccountByUserName(username) != null){
            JsonResponse res = new JsonResponse(403, "username exists", null);
            response.status(403);
            response.json(res);
            return;
        }
        Account account = DatabaseHelper.createAccount(username, password);

        JsonResponse res = new JsonResponse(account);
        response.json(res);
    }
}
