package hk.overlander.retro;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RetroActivity extends Activity {
  private static final String HOME="https://appassets.androidplatform.net/assets/index.html";
  private WebView web;
  private ValueCallback<Uri[]> chooser;
  private String exportText;
  private void message(String s){runOnUiThread(()->Toast.makeText(this,s,Toast.LENGTH_LONG).show());}
  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    getWindow().setStatusBarColor(0xff293e32);
    getWindow().setNavigationBarColor(0xff293e32);
    web=new WebView(this);web.setBackgroundColor(0xff293e32);setContentView(web);
    WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
    settings.setAllowFileAccess(false);settings.setAllowContentAccess(true);
    settings.setAllowFileAccessFromFileURLs(false);settings.setAllowUniversalAccessFromFileURLs(false);
    settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    settings.setMediaPlaybackRequiresUserGesture(true);
    WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
    web.setWebViewClient(new WebViewClientCompat(){
      @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){
        Uri u=r.getUrl();
        if("https".equals(u.getScheme())&&"appassets.androidplatform.net".equals(u.getHost())){
          WebResourceResponse local=loader.shouldInterceptRequest(u);if(local!=null)return local;
        }
        return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
      }
      @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return !HOME.equals(r.getUrl().toString());}
    });
    web.setWebChromeClient(new WebChromeClient(){
      @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams p){
        if(chooser!=null)chooser.onReceiveValue(null);chooser=cb;
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*");
        ArrayList<String> types=new ArrayList<>();for(String t:p.getAcceptTypes())if(t!=null&&t.contains("/"))types.add(t);
        if(types.size()==1)i.setType(types.get(0));else if(types.size()>1)i.putExtra(Intent.EXTRA_MIME_TYPES,types.toArray(new String[0]));
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,p.getMode()==FileChooserParams.MODE_OPEN_MULTIPLE);
        try{startActivityForResult(i,10);}catch(Exception e){chooser.onReceiveValue(null);chooser=null;message("未找到檔案選擇器");}return true;
      }
    });
    web.addJavascriptInterface(new ExportBridge(),"AndroidFiles");web.loadUrl(HOME);
  }
  public class ExportBridge {
    @JavascriptInterface public void saveText(String name,String mime,String text){
      if(text==null||text.length()>32*1024*1024){message("匯出檔案過大");return;}
      runOnUiThread(()->{
        if(exportText!=null){message("請先完成上一個匯出");return;}exportText=text;
        String safe=(name==null?"overlander.json":name).replaceAll("[^a-zA-Z0-9._-]","_");
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
          .setType("text/calendar".equals(mime)?"text/calendar":"application/json").putExtra(Intent.EXTRA_TITLE,safe);
        try{startActivityForResult(i,11);}catch(Exception e){exportText=null;message("未找到儲存工具");}
      });
    }
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(request==10&&chooser!=null){
      ArrayList<Uri> list=new ArrayList<>();
      if(result==RESULT_OK&&data!=null){
        if(data.getClipData()!=null){for(int n=0;n<data.getClipData().getItemCount();n++){Uri u=data.getClipData().getItemAt(n).getUri();if("content".equals(u.getScheme()))list.add(u);}}
        else if(data.getData()!=null&&"content".equals(data.getData().getScheme()))list.add(data.getData());
      }
      chooser.onReceiveValue(list.isEmpty()?null:list.toArray(new Uri[0]));chooser=null;
    }
    if(request==11){
      final String text=exportText;exportText=null;
      if(result==RESULT_OK&&data!=null&&data.getData()!=null&&text!=null){
        final Uri destination=data.getData();
        new Thread(()->{try(OutputStream out=getContentResolver().openOutputStream(destination,"wt")){
          if(out==null)throw new IOException();out.write(text.getBytes(StandardCharsets.UTF_8));message("檔案已儲存");
        }catch(Exception e){message("儲存失敗，請重新匯出");}}).start();
      }
    }
  }
  @Override protected void onPause(){super.onPause();if(web!=null){web.evaluateJavascript("if(typeof player!=='undefined')player.pause();if(typeof custom!=='undefined')custom.pause();if(typeof audioCtx!=='undefined'&&audioCtx)audioCtx.suspend();",null);web.onPause();}}
  @Override protected void onResume(){super.onResume();if(web!=null){web.onResume();web.evaluateJavascript("if(typeof checkSleep==='function')checkSleep();",null);}}
  @Override public void onBackPressed(){web.evaluateJavascript("(function(){const d=document.querySelector('dialog[open]');if(d){d.close();return false;}if(typeof tab!=='undefined'&&tab!=='tickets'){tab='tickets';render();return false;}return true;})()",r->{if("true".equals(r))moveTaskToBack(true);});}
  @Override protected void onDestroy(){if(chooser!=null)chooser.onReceiveValue(null);if(web!=null){web.removeJavascriptInterface("AndroidFiles");web.destroy();}super.onDestroy();}
}
