package com.speedometer;
import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.webkit.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
public class MainActivity extends AppCompatActivity {
  private WebView webView;
  private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      double speed=intent.getDoubleExtra("speed",0);
      double lat=intent.getDoubleExtra("lat",0);
      double lon=intent.getDoubleExtra("lon",0);
      double acc=intent.getDoubleExtra("accuracy",0);
      String js=String.format("javascript:onNativeGPS(%f,%f,%f,%f);",speed,lat,lon,acc);
      runOnUiThread(()->webView.evaluateJavascript(js,null));
    }
  };
  protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_main);
    webView=findViewById(R.id.webView);
    WebSettings s=webView.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setGeolocationEnabled(true);
    s.setAllowFileAccessFromFileURLs(true);
    s.setAllowUniversalAccessFromFileURLs(true);
    webView.addJavascriptInterface(new Bridge(),"AndroidBridge");
    webView.setWebViewClient(new WebViewClient());
    webView.setWebChromeClient(new WebChromeClient(){
      public void onGeolocationPermissionsShowPrompt(String o,GeolocationPermissions.Callback cb){cb.invoke(o,true,false);}
    });
    webView.loadUrl("file:///android_asset/speedometer.html");
    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
      ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
  }
  public class Bridge {
    @android.webkit.JavascriptInterface
    public void startGpsService(){
      Intent i=new Intent(MainActivity.this,GpsService.class);
      i.setAction("START");
      if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) startForegroundService(i);
      else startService(i);
    }
    @android.webkit.JavascriptInterface
    public void stopGpsService(){
      Intent i=new Intent(MainActivity.this,GpsService.class);
      i.setAction("STOP");
      startService(i);
    }
    @android.webkit.JavascriptInterface
    public void vibrate(){
      android.os.Vibrator v=(android.os.Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
      if(v!=null) v.vibrate(new long[]{0,500,200,500},-1);
    }
  }
  protected void onResume(){super.onResume();LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceiver,new IntentFilter("GPS_UPDATE"));}
  protected void onPause(){super.onPause();LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsReceiver);}
}
