package zou.AccountMap.server.http;

import express.http.Request;
import express.http.Response;
import org.eclipse.jetty.websocket.api.Session;
import zou.AccountMap.database.DatabaseHelper;
import zou.AccountMap.users.Account;

public final class Helper {
    public static boolean isLogin(Request request){
        String c_token = request.cookie("token");
        if(c_token == null)
            return false;

        Account s_account= request.session("account");
        Account account = DatabaseHelper.getAccountByToken(c_token);
        if(s_account == null){
            if(account == null)
                return false;
            request.session("account", account);
            return true;
        }

        if(!s_account.getToken().equals(account.getToken())){
            request.session("account", account);
            return true;
        }
        return true;

    }
}
