package com.bitlifebr.game;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Download, integridade SHA-256 e extração segura do pacote oficial do jogo. */
public final class ContentPackActivity extends Activity {
    private static final int BG=Color.rgb(10,16,31), PANEL=Color.rgb(23,33,54);
    private static final int ACCENT=Color.rgb(87,224,197), TEXT=Color.rgb(244,247,252);
    private static final int MUTED=Color.rgb(163,178,200);
    private static final long EXTRA_SPACE=96L*1024L*1024L, MAX_UNPACKED=3L*1024L*1024L*1024L;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private DownloadManager manager;
    private TextView status,detail,percent,size,stage;
    private ProgressBar bar;
    private Button action,cancel;
    private ManifestData pack;
    private long downloadId=-1L;
    private boolean polling,extracting;
    private ObjectAnimator pulse;
    private final Runnable pollTask=this::pollDownload;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        manager=(DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        buildScreen(); restoreDownload(); refreshManifest();
    }
    @Override protected void onResume(){super.onResume();if(downloadId>0)startPolling();}
    @Override protected void onDestroy(){handler.removeCallbacks(pollTask);if(pulse!=null)pulse.cancel();super.onDestroy();}

    private void buildScreen(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(22),dp(28),dp(22),dp(30));
        TextView heading=text("CENTRAL DE CONTEÚDO",24,TEXT);heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(heading,match());
        TextView sub=text("Instale os dados e recursos oficiais do BitLife BR.",14,MUTED);sub.setPadding(0,dp(7),0,dp(19));root.addView(sub,match());
        LinearLayout hero=new LinearLayout(this);hero.setGravity(Gravity.CENTER);hero.setOrientation(1);hero.setPadding(dp(18),dp(24),dp(18),dp(24));hero.setBackground(round(PANEL,18));
        TextView icon=text("⬇",54,ACCENT);icon.setGravity(Gravity.CENTER);hero.addView(icon,match());
        pulse=ObjectAnimator.ofFloat(icon,"alpha",0.55f,1f);pulse.setDuration(950);pulse.setRepeatCount(ObjectAnimator.INFINITE);pulse.setRepeatMode(ObjectAnimator.REVERSE);pulse.start();
        TextView heroTitle=text("PACOTE DE EXPANSÃO",17,TEXT);heroTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);heroTitle.setGravity(Gravity.CENTER);heroTitle.setPadding(0,dp(8),0,dp(4));hero.addView(heroTitle,match());
        TextView heroSub=text("Download seguro • Verificação • Extração",12,MUTED);heroSub.setGravity(Gravity.CENTER);hero.addView(heroSub,match());
        LinearLayout.LayoutParams hp=match();hp.bottomMargin=dp(16);root.addView(hero,hp);
        LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(16),dp(17),dp(16),dp(17));card.setBackground(round(PANEL,18));
        status=text("Conectando ao servidor...",16,TEXT);status.setTypeface(Typeface.DEFAULT,Typeface.BOLD);card.addView(status,match());
        stage=text("ETAPA 1/4 • VERIFICANDO DISPONIBILIDADE",11,ACCENT);stage.setPadding(0,dp(7),0,dp(13));card.addView(stage,match());
        bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(1000);card.addView(bar,match());
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(8),0,dp(4));
        percent=text("0%",21,ACCENT);percent.setTypeface(Typeface.DEFAULT,Typeface.BOLD);row.addView(percent,new LinearLayout.LayoutParams(0,-2,1f));
        size=text("0 B / —",12,MUTED);row.addView(size,wrap());card.addView(row,match());
        detail=text("O progresso será calculado pelos bytes recebidos do servidor.",13,MUTED);detail.setPadding(0,dp(5),0,0);card.addView(detail,match());
        LinearLayout.LayoutParams cp=match();cp.bottomMargin=dp(14);root.addView(card,cp);
        action=button("Verificar pacote no servidor",true,this::refreshManifest);root.addView(action,match());
        cancel=button("Cancelar download",false,this::cancelDownload);cancel.setVisibility(View.GONE);root.addView(cancel,match());
        TextView info=text("O conteúdo só será instalado depois de conferir o tamanho, validar o SHA-256 e extrair os arquivos com proteção contra caminhos maliciosos. O download não avança a idade do personagem.",12,MUTED);
        info.setPadding(0,dp(18),0,0);root.addView(info,match());scroll.addView(root);setContentView(scroll);
    }

    private void restoreDownload(){
        SharedPreferences p=getSharedPreferences("content_pack",MODE_PRIVATE);downloadId=p.getLong("download_id",-1L);
        if(downloadId>0){
            pack=new ManifestData(p.getString("version",""),p.getString("filename","bitlife-br-content.zip"),
                    p.getLong("size_bytes",0L),p.getString("sha256",""),p.getString("download_url",""));
            update("Download encontrado","ETAPA 2/4 • RETOMANDO DOWNLOAD","O Android está continuando ou verificando o download salvo.",true);
            cancel.setVisibility(View.VISIBLE);action.setText("Download em andamento");action.setEnabled(false);
        }else{
            String installed=p.getString("installed_version","");
            if(!installed.isEmpty()){update("Conteúdo instalado: "+installed,"ETAPA 4/4 • CONCLUÍDO",
                    "Arquivos disponíveis no armazenamento privado do jogo.",false);bar.setProgress(1000);percent.setText("100%");}
        }
    }

    private void refreshManifest(){
        if(downloadId>0||extracting)return;
        String base=BuildConfig.API_BASE_URL==null?"":BuildConfig.API_BASE_URL.trim();
        if(base.isEmpty()){
            update("Servidor ainda não conectado","ETAPA 1/4 • CONFIGURAÇÃO PENDENTE",
                    "Compile com BITLIFE_API_BASE_URL apontando para a API publicada. Nenhum download fictício foi iniciado.",false);
            action.setText("Tentar novamente");action.setEnabled(true);action.setOnClickListener(v->refreshManifest());return;
        }
        action.setEnabled(false);action.setText("Consultando...");
        update("Consultando o catálogo...","ETAPA 1/4 • CONECTANDO","Buscando versão, tamanho real e assinatura do pacote.",true);
        new Thread(()->{
            HttpURLConnection c=null;
            try{
                c=(HttpURLConnection)new URL(trimSlash(base)+"/api/content/manifest").openConnection();
                c.setRequestMethod("GET");c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setUseCaches(false);
                int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();
                JSONObject j=new JSONObject(readText(in));
                if(code!=200)throw new IllegalStateException(j.optString("error","Pacote não publicado."));
                ManifestData m=new ManifestData(j.getString("version"),j.optString("fileName","bitlife-br-content.zip"),
                        j.getLong("sizeBytes"),j.getString("sha256"),j.getString("downloadUrl"));
                boolean safe=m.downloadUrl.startsWith("/api/content/download")||m.downloadUrl.startsWith("https://");
                if(m.sizeBytes<=0||m.sizeBytes>2L*1024L*1024L*1024L||!m.sha256.matches("(?i)[0-9a-f]{64}")||!safe)
                    throw new IllegalStateException("Manifesto inválido ou inseguro.");
                pack=m;
                runOnUiThread(()->{size.setText("0 B / "+format(m.sizeBytes));update("Pacote "+m.version+" disponível",
                    "ETAPA 1/4 • PACOTE VALIDADO",format(m.sizeBytes)+" • tamanho real e SHA-256 recebidos do servidor.",false);
                    action.setText("Baixar e instalar conteúdo");action.setEnabled(true);action.setOnClickListener(v->startDownload());});
            }catch(Exception e){
                runOnUiThread(()->{update("Pacote indisponível","ETAPA 1/4 • AGUARDANDO PUBLICAÇÃO",
                    message(e)+" O jogo não inicia download falso.",false);action.setText("Verificar novamente");
                    action.setEnabled(true);action.setOnClickListener(v->refreshManifest());});
            }finally{if(c!=null)c.disconnect();}
        },"bitlife-pack-manifest").start();
    }

    private void startDownload(){
        if(pack==null||downloadId>0||extracting)return;
        try{
            String url=pack.downloadUrl.startsWith("https://")?pack.downloadUrl:trimSlash(BuildConfig.API_BASE_URL)+pack.downloadUrl;
            Uri uri=Uri.parse(url);if(!"https".equalsIgnoreCase(uri.getScheme()))throw new IllegalStateException("O download exige HTTPS.");
            File dir=getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);if(dir==null)throw new IllegalStateException("Armazenamento indisponível.");
            if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("Não foi possível preparar a pasta.");
            if(new StatFs(dir.getAbsolutePath()).getAvailableBytes()<pack.sizeBytes+EXTRA_SPACE)
                throw new IllegalStateException("Espaço insuficiente. São necessários "+format(pack.sizeBytes+EXTRA_SPACE)+" livres.");
            String filename=pack.fileName.replaceAll("[^A-Za-z0-9._-]","_");
            if(!filename.toLowerCase(Locale.ROOT).endsWith(".zip"))throw new IllegalStateException("O servidor não publicou um ZIP.");
            pack=new ManifestData(pack.version,filename,pack.sizeBytes,pack.sha256,pack.downloadUrl);
            DownloadManager.Request req=new DownloadManager.Request(uri);
            req.setTitle("BitLife BR • Conteúdo "+pack.version);req.setDescription("Baixando recursos do jogo");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI|DownloadManager.Request.NETWORK_MOBILE);
            req.setVisibleInDownloadsUi(true);req.setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS,filename);
            downloadId=manager.enqueue(req);
            getSharedPreferences("content_pack",MODE_PRIVATE).edit().putLong("download_id",downloadId)
                .putString("version",pack.version).putString("filename",filename).putLong("size_bytes",pack.sizeBytes)
                .putString("sha256",pack.sha256).putString("download_url",pack.downloadUrl).apply();
            action.setEnabled(false);action.setText("Baixando...");cancel.setVisibility(View.VISIBLE);bar.setIndeterminate(false);bar.setProgress(0);startPolling();
        }catch(Exception e){update("Não foi possível iniciar","ETAPA 2/4 • DOWNLOAD NÃO INICIADO",message(e),false);action.setEnabled(true);}
    }

    private void startPolling(){if(polling||downloadId<=0)return;polling=true;handler.post(pollTask);}
    private void pollDownload(){
        if(downloadId<=0||extracting){polling=false;return;}
        try(Cursor cur=manager.query(new DownloadManager.Query().setFilterById(downloadId))){
            if(cur==null||!cur.moveToFirst()){fail("Registro de download não encontrado.");return;}
            int state=cur.getInt(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            long got=cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long total=cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            long expected=pack==null?total:pack.sizeBytes;
            if(state==DownloadManager.STATUS_SUCCESSFUL){polling=false;handler.removeCallbacks(pollTask);verifyAndExtract();return;}
            if(state==DownloadManager.STATUS_FAILED){int reason=cur.getInt(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));fail("Download recusado pelo Android (código "+reason+").");return;}
            int n=expected>0?(int)Math.min(1000L,got*1000L/expected):0;
            runOnUiThread(()->{bar.setIndeterminate(false);bar.setProgress(n);percent.setText(Math.round(n/10f)+"%");
                size.setText(format(got)+" / "+format(expected));update(state==DownloadManager.STATUS_PAUSED?"Download pausado":"Baixando conteúdo...",
                "ETAPA 2/4 • RECEBENDO ARQUIVOS","A barra acompanha os bytes confirmados pelo Android.",true);cancel.setVisibility(View.VISIBLE);});
        }catch(Exception e){fail(message(e));return;}
        handler.postDelayed(pollTask,650L);
    }

    private void verifyAndExtract(){
        if(extracting)return;extracting=true;
        runOnUiThread(()->{action.setEnabled(false);action.setText("Verificando...");cancel.setVisibility(View.GONE);
            bar.setIndeterminate(true);percent.setText("✓");update("Verificando integridade","ETAPA 3/4 • SHA-256",
            "Conferindo tamanho e assinatura antes de abrir os arquivos.",true);});
        new Thread(()->{
            try{
                File dir=getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),zip=new File(dir,pack.fileName);
                if(!zip.isFile()||zip.length()!=pack.sizeBytes)throw new IllegalStateException("Tamanho recebido diferente do manifesto.");
                String hash=sha256(zip);
                if(!MessageDigest.isEqual(hash.getBytes("UTF-8"),pack.sha256.toLowerCase(Locale.ROOT).getBytes("UTF-8")))
                    throw new IllegalStateException("SHA-256 não confere. Pacote rejeitado.");
                runOnUiThread(()->update("Integridade confirmada","ETAPA 4/4 • EXTRAINDO RECURSOS",
                    "Arquivo autêntico. Preparando dados no armazenamento privado do jogo.",true));
                File installed=extract(zip,pack.version);zip.delete();
                getSharedPreferences("content_pack",MODE_PRIVATE).edit().remove("download_id").remove("download_url")
                    .putString("installed_version",pack.version).putString("installed_path",installed.getAbsolutePath()).apply();
                downloadId=-1;extracting=false;
                runOnUiThread(()->{bar.setIndeterminate(false);bar.setProgress(1000);percent.setText("100%");
                    size.setText(format(pack.sizeBytes)+" • instalado");update("Conteúdo instalado",
                    "ETAPA 4/4 • CONCLUÍDO","Versão "+pack.version+" extraída em "+installed.getAbsolutePath(),false);
                    action.setText("Verificar atualizações");action.setEnabled(true);action.setOnClickListener(v->refreshManifest());});
            }catch(Exception e){extracting=false;fail("Pacote não instalado: "+message(e));}
        },"bitlife-pack-verify-extract").start();
    }

    private File extract(File zipFile,String version)throws Exception{
        File root=new File(getFilesDir(),"bitlife-content");if(!root.exists()&&!root.mkdirs())throw new IllegalStateException("Falha ao criar pasta de conteúdo.");
        String safe=version.replaceAll("[^A-Za-z0-9._-]","_");File temp=new File(root,".staging_"+safe+"_"+System.currentTimeMillis());
        if(!temp.mkdirs())throw new IllegalStateException("Falha ao preparar extração.");
        File canonicalRoot=temp.getCanonicalFile();long bytes=0;int entries=0;
        try(ZipInputStream zip=new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))){
            ZipEntry entry;byte[] buffer=new byte[65536];
            while((entry=zip.getNextEntry())!=null){
                if(++entries>50000)throw new IllegalStateException("Arquivos demais no pacote.");
                File outFile=new File(temp,entry.getName());String path=outFile.getCanonicalPath();
                if(!path.equals(canonicalRoot.getPath())&&!path.startsWith(canonicalRoot.getPath()+File.separator))
                    throw new IllegalStateException("Caminho inseguro no ZIP.");
                if(entry.isDirectory()){if(!outFile.exists()&&!outFile.mkdirs())throw new IllegalStateException("Falha ao criar diretório.");}
                else{File parent=outFile.getParentFile();if(parent!=null&&!parent.exists()&&!parent.mkdirs())throw new IllegalStateException("Falha ao criar pasta.");
                    try(FileOutputStream out=new FileOutputStream(outFile)){int len;while((len=zip.read(buffer))!=-1){bytes+=len;
                        if(bytes>MAX_UNPACKED)throw new IllegalStateException("Pacote excede o limite de extração.");out.write(buffer,0,len);}}}
                zip.closeEntry();
            }
        }catch(Exception e){removeTree(temp);throw e;}
        File meta=new File(temp,"manifest.json");if(!meta.isFile()||bytes<=0){removeTree(temp);throw new IllegalStateException("manifest.json não encontrado no ZIP.");}
        String json;try(InputStream in=new FileInputStream(meta);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>1048576)throw new IllegalStateException("Manifesto interno muito grande.");out.write(b,0,n);}json=out.toString("UTF-8");}
        JSONObject internal=new JSONObject(json);
        if(!version.equals(internal.optString("version"))||internal.optInt("schemaVersion",0)<1){removeTree(temp);throw new IllegalStateException("Versão/schema interno incompatível.");}
        File target=new File(root,"v_"+safe);if(target.exists()){removeTree(temp);return target;}
        if(!temp.renameTo(target)){removeTree(temp);throw new IllegalStateException("Falha ao finalizar a instalação.");}
        return target;
    }

    private void cancelDownload(){
        if(downloadId>0){manager.remove(downloadId);downloadId=-1;polling=false;handler.removeCallbacks(pollTask);
            getSharedPreferences("content_pack",MODE_PRIVATE).edit().remove("download_id").remove("download_url").apply();}
        cancel.setVisibility(View.GONE);action.setText("Verificar pacote no servidor");action.setEnabled(true);
        action.setOnClickListener(v->refreshManifest());bar.setIndeterminate(false);bar.setProgress(0);
        update("Download cancelado","ETAPA 2/4 • CANCELADO","Nenhum arquivo parcial foi instalado.",false);
    }
    private void fail(String reason){
        long old=downloadId;downloadId=-1;polling=false;extracting=false;handler.removeCallbacks(pollTask);
        if(old>0)manager.remove(old);getSharedPreferences("content_pack",MODE_PRIVATE).edit().remove("download_id").remove("download_url").apply();
        runOnUiThread(()->{cancel.setVisibility(View.GONE);bar.setIndeterminate(false);bar.setProgress(0);
            update("Não foi possível instalar","ETAPA 3/4 • ERRO DE VALIDAÇÃO",reason,false);
            action.setText("Tentar novamente");action.setEnabled(true);action.setOnClickListener(v->refreshManifest());});
    }
    private void update(String title,String step,String text,boolean busy){status.setText(title);stage.setText(step);detail.setText(text);if(!busy)bar.setIndeterminate(false);}
    private String sha256(File f)throws Exception{
        MessageDigest d=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(f)){byte[] b=new byte[131072];int n;while((n=in.read(b))!=-1)d.update(b,0,n);}
        StringBuilder out=new StringBuilder(64);for(byte b:d.digest())out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();
    }
    private String readText(InputStream in)throws Exception{if(in==null)return "{}";try(InputStream s=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){
        byte[] b=new byte[8192];int n;while((n=s.read(b))!=-1){if(out.size()+n>1048576)throw new IllegalStateException("Resposta muito grande.");out.write(b,0,n);}return out.toString("UTF-8");}}
    private String message(Exception e){return e.getMessage()==null?"Erro de rede ou arquivo.":e.getMessage();}
    private String trimSlash(String s){return s==null?"":s.replaceAll("/+$","");}
    private String format(long n){if(n<0)return "—";if(n<1024)return n+" B";if(n<1048576)return String.format(Locale.ROOT,"%.1f KB",n/1024.0);
        if(n<1073741824L)return String.format(Locale.ROOT,"%.1f MB",n/1048576.0);return String.format(Locale.ROOT,"%.2f GB",n/1073741824.0);}
    private void removeTree(File f){if(!f.exists())return;if(f.isDirectory()){File[] c=f.listFiles();if(c!=null)for(File x:c)removeTree(x);}f.delete();}
    private Button button(String label,boolean primary,Runnable task){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(15);
        b.setTextColor(primary?Color.rgb(9,31,33):TEXT);b.setBackground(round(primary?ACCENT:PANEL,13));b.setOnClickListener(v->task.run());return b;}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private LinearLayout.LayoutParams match(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(8);return p;}
    private LinearLayout.LayoutParams wrap(){return new LinearLayout.LayoutParams(-2,-2);}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private static final class ManifestData{
        final String version,fileName,sha256,downloadUrl;final long sizeBytes;
        ManifestData(String v,String f,long s,String h,String u){version=v;fileName=f;sizeBytes=s;sha256=h;downloadUrl=u;}
    }
}