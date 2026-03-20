package com.speedometer;
import android.app.*;
import android.content.Intent;
import android.location.*;
import android.os.*;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
public class GpsService extends Service implements LocationListener {
  private LocationManager lm;
  private boolean running=false;
  public int onStartCommand(Intent intent,int f,int id){
    if(intent!=null&&"STOP".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
    if(!running){
      running=true;
      if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
        NotificationChannel ch=new NotificationChannel("gps","GPS",NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
      }
      Notification n=new NotificationCompat.Builder(this,"gps")
        .setContentTitle("GPS Speedometer")
        .setContentText("Tracking...")
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setOngoing(true).setSilent(true).build();
      startForeground(1,n);
      lm=(LocationManager)getSystemService(LOCATION_SERVICE);
      try{
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,2,this);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,2000,5,this);
      }catch(SecurityException e){}
    }
    return START_STICKY;
  }
  public void onLocationChanged(Location loc){
    double spd=loc.hasSpeed()?loc.getSpeed()*3.6:0;
    Intent b=new Intent("GPS_UPDATE");
    b.putExtra("speed",spd);
    b.putExtra("lat",loc.getLatitude());
    b.putExtra("lon",loc.getLongitude());
    b.putExtra("accuracy",(double)loc.getAccuracy());
    LocalBroadcastManager.getInstance(this).sendBroadcast(b);
  }
  public void onDestroy(){running=false;if(lm!=null)lm.removeUpdates(this);super.onDestroy();}
  public IBinder onBind(Intent i){return null;}
  public void onStatusChanged(String p,int s,Bundle e){}
  public void onProviderEnabled(String p){}
  public void onProviderDisabled(String p){}
}
