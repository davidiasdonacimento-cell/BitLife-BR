package com.bitlifebr.game.network;

import android.os.Handler;
import android.os.Looper;
import com.bitlifebr.game.BuildConfig;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Cliente HTTP assíncrono. O servidor valida escolhas e controla a revisão do save. */
public final class OnlineApi {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final int TIMEOUT_MS = 12000;
    public interface Callback { void onSuccess(JSONObject result); void onError(String message); }
    private OnlineApi() { }
    public static boolean isConfigured() {
        return BuildConfig.API_BASE_URL != null && !BuildConfig.API_BASE_URL.trim().isEmpty();
    }
    public static void register(String email,String password,Callback cb) { request("POST","/api/auth/register",null,credentials(email,password),cb); }
    public static void login(String email,String password,Callback cb) { request("POST","/api/auth/login",null,credentials(email,password),cb); }
    public static void loadSave(String token,Callback cb) { request("GET","/api/save",token,null,cb); }
    public static void createSession(String token,Callback cb) { request("POST","/api/sessions",token,new JSONObject(),cb); }
    public static void save(String token,long revision,JSONObject state,Callback cb) {
        JSONObject body=new JSONObject();
        try { body.put("expectedRevision",revision); body.put("state",state); }
        catch(Exception e) { cb.onError("Não foi possível preparar o salvamento."); return; }
        request("PUT","/api/save",token,body,cb);
    }
    public static void submitChoice(String token,String choiceId,long revision,Callback cb) {
        JSONObject body=new JSONObject();
        try { body.put("choiceId",choiceId); body.put("expectedRevision",revision); }
        catch(Exception e) { cb.onError("Escolha inválida."); return; }
        request("POST","/api/choice",token,body,cb);
    }
    private static JSONObject credentials(String email,String password) {
        JSONObject body=new JSONObject();
        try { body.put("email",email==null?"":email.trim()); body.put("password",password==null?"":password); }
        catch(Exception ignored) { }
        return body;
    }
    private static void request(String method,String path,String token,JSONObject body,Callback cb) {
        if(!isConfigured()) { cb.onError("Servidor online ainda não configurado."); return; }
        EXECUTOR.execute(() -> {
            HttpURLConnection c=null;
            try {
                String base=BuildConfig.API_BASE_URL.replaceAll("/+$","");
                c=(HttpURLConnection)new URL(base+path).openConnection();
                c.setRequestMethod(method); c.setConnectTimeout(TIMEOUT_MS); c.setReadTimeout(TIMEOUT_MS);
                c.setRequestProperty("Accept","application/json");
                if(body!=null) { c.setDoOutput(true); c.setRequestProperty("Content-Type","application/json; charset=utf-8"); }
                if(token!=null&&!token.isEmpty()) c.setRequestProperty("Authorization","Bearer "+token);
                if(body!=null) {
                    byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);
                    try(OutputStream out=c.getOutputStream()) { out.write(bytes); }
                }
                int code=c.getResponseCode();
                InputStream stream=code>=200&&code<300?c.getInputStream():c.getErrorStream();
                StringBuilder data=new StringBuilder();
                if(stream!=null) try(BufferedReader reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
                    String line; while((line=reader.readLine())!=null) data.append(line);
                }
                JSONObject result=data.length()==0?new JSONObject():new JSONObject(data.toString());
                if(code>=200&&code<300) MAIN.post(() -> cb.onSuccess(result));
                else { String msg=result.optString("error","Falha na comunicação ("+code+")."); MAIN.post(() -> cb.onError(msg)); }
            } catch(Exception e) {
                MAIN.post(() -> cb.onError("Sem conexão com o servidor. Seu save local não foi apagado."));
            } finally { if(c!=null)c.disconnect(); }
        });
    }
}
