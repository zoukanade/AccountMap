package zou.AccountMap.server.http.handlers;

import express.Express;
import express.http.Request;
import express.http.Response;
import io.javalin.Javalin;
import zou.AccountMap.AccountMap;
import zou.AccountMap.server.http.Router;

public final class GenericHandler implements Router {
    @Override
    public void applyRoutes(Express express, Javalin handle) {
        express.all("/test", GenericHandler::test);
    }

    private static void test(Request request, Response response){
        String str = request.query("str");
        if(str != null){
            request.session("str", str);
        }


        response.send(request.session("str").toString());
    }
}
