package zou.AccountMap.server.http.handlers;

import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import express.Express;
import express.http.Request;
import express.http.Response;
import io.javalin.Javalin;
import org.bson.types.ObjectId;
import zou.AccountMap.database.DatabaseHelper;
import zou.AccountMap.database.DatabaseManager;
import zou.AccountMap.map.AppAccount;
import zou.AccountMap.map.Application;
import zou.AccountMap.server.http.Router;
import zou.AccountMap.server.http.objects.JsonResponse;
import zou.AccountMap.users.Account;

import java.util.ArrayList;
import java.util.List;

import static zou.AccountMap.map.AppAccount.createAppAccount;
import static zou.AccountMap.server.http.Helper.isLogin;

public final class AccountMapHandler implements Router {
    @Override
    public void applyRoutes(Express express, Javalin handle) {
        express.all("/api/accountmap/add", AccountMapHandler::add);
        express.all("/api/accountmap/getAppAccount", AccountMapHandler::getAppAccount);
    }


    //account password ownerApp bindAppAccountId[]
    private static void add(Request request, Response response) {
        JsonResponse res = new JsonResponse();
        if(!isLogin(request)){
            res = new JsonResponse(403, "Not logged in", null);
            response.status(403);
            response.json(res);
            return;
        }

        String account = request.query("account");
        String password = request.query("password");
        String ownerApp = request.query("ownerApp");
        String bindAppAccountId = request.query("bindAppAccountId");

        if(account == null || password == null){
            res = new JsonResponse(400, "username or password is null", null);
            response.status(400);
            response.json(res);
            return;
        }
        if(ownerApp == null){
            res = new JsonResponse(400, "ownerApp is null", null);
            response.status(400);
            response.json(res);
            return;
        }
        Application application = DatabaseHelper.getApplicationByAppName(ownerApp);
        if(application == null){
            res = new JsonResponse(403, "application not exist", null);
            response.status(403);
            response.json(res);
            return;
        }
        //create AppAccount
        AppAccount createAppAccount = createAppAccount(request.session("account"), application, account, password);
        //add bindAppAccount
        if(bindAppAccountId != null && bindAppAccountId.length() >= 1){
            String[] bindAppAccountIds = bindAppAccountId.trim().split(",");
            try {
                for (String appAccountId : bindAppAccountIds) {
                    AppAccount bindAppAccount = DatabaseHelper.getAppAccountById(new ObjectId(appAccountId));
                    if (bindAppAccount != null){
                        createAppAccount.addBindAppAccount(bindAppAccount);
                    }
                }
                createAppAccount.save();
            }catch (Exception e){
                res.data = e.toString();
                response.json(res);
                return;
            }
        }
        //send to client
        response.json(res);
    }
    private static void getAppAccount(Request request, Response response) {
        if(!isLogin(request)){
            response.status(403);
            response.json(new JsonResponse(403, "Not logged in", null));
            return;
        }
        Account req_account = request.session("account");
        String type = (request.query("type") == null ? "all" : request.query("type"));
        int res_index = Integer.parseInt(request.query("res_index") == null ? "0" : request.query("res_index"));
        String id = request.query("id");
        String AppId = request.query("AppId");
        String account = request.query("account");
        Query<AppAccount> queue = null;
        boolean allAccount = false;
        try {
            if (id != null)
                if (!DatabaseHelper.getAppAccountById(new ObjectId(id)).getOwnerId().equals(req_account.getId()))
                    allAccount = true;
        }catch (Exception e){
            response.status(400);
            response.json(new JsonResponse(400, "error", e));
            return;
        }

        if(allAccount) //req_account or allAccount
            queue = DatabaseManager.getDatastore().find(AppAccount.class);
        else
            queue = DatabaseManager.getDatastore().find(AppAccount.class).filter(Filters.eq("ownerId", req_account.getId()));


        if (id != null)
            queue = queue.filter(Filters.eq("_id", new ObjectId(id)));
        if (AppId != null)
            queue = queue.filter(Filters.eq("appId", AppId));
        if(account != null)
            queue = queue.filter(Filters.eq("account", account));

        List<AppAccount> resAppAccounts = new ArrayList<>(queue.stream().toList());
        //clear not reqAccount to password
        if(allAccount){
            for (AppAccount resAppAccount : resAppAccounts) {
                if (!resAppAccount.getOwnerId().equals(req_account.getId()))
                    resAppAccount.setPassword("");
            }
        }
        switch (type) {
            case "all":
                response.json(new JsonResponse(resAppAccounts));
                return;
            case "index":
                if(res_index >= resAppAccounts.size()){
                    response.status(403);
                    response.json(new JsonResponse(403, "res_index out of range", null));
                    return;
                }
                response.json(new JsonResponse(resAppAccounts.get(res_index)));
                return;
        }
    }
}
