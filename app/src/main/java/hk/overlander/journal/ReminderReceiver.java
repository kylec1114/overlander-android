package hk.overlander.journal;
import android.app.*;import android.content.*;import org.json.*;
public class ReminderReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){String id=i.getStringExtra("id");String title=null;
 try{JSONArray a=new JSONArray(c.getSharedPreferences("journey",0).getString("tasks","[]"));for(int j=0;j<a.length();j++){JSONObject o=a.getJSONObject(j);if(o.optString("id").equals(id)&&!o.optBoolean("done"))title=o.optString("title");}}catch(Exception e){return;}
 if(title==null)return;
 NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
 nm.createNotificationChannel(new NotificationChannel("journey","旅程提醒",NotificationManager.IMPORTANCE_DEFAULT));
 PendingIntent p=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
 Notification n=new Notification.Builder(c,"journey").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("下一程，準備出發").setContentText(title).setContentIntent(p).setAutoCancel(true).build();
 try{nm.notify(id.hashCode(),n);}catch(SecurityException ignored){}
 }
}
