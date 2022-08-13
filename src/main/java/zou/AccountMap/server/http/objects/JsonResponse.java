package zou.AccountMap.server.http.objects;

public class JsonResponse {
    public int code;
    public String message;
    public Object data;

    public JsonResponse(){
        this.code = 200;
        this.message = "OK";
    }
    public JsonResponse(Object data){
        this.code = 200;
        this.message = "OK";
        this.data = data;
    }
    public JsonResponse(String message, Object data){
        this.code = 200;
        this.message = message;
        this.data = data;
    }
    public JsonResponse(int code, String message, Object data){
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
