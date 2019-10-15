package neto.lc.geraldo.com.communicatorlib;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;


public class CommunicatorServiceStarter {
    private static final String TAG = "CommunicatorStarter";
    private final int servicePort;
    private String deviceName;
    private Context context;
    private Class activityClass;


    public CommunicatorServiceStarter(Context context, Class activityClass,String deviceName,int servicePort){
        this.context = context;
        this.activityClass = activityClass;
        this.deviceName = deviceName;
        this.servicePort = servicePort;
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Class getActivityClass() {
        return activityClass;
    }

    public void setActivityClass(Class activityClass) {
        this.activityClass = activityClass;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel =  new NotificationChannel(CommunicatorService.CHANNEL_ID,
                    "Waiter Caller Service Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            serviceChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    private void startService(){
        Communicator.getInstance(context).setDeviceName(deviceName);
        Communicator.getInstance(context).setServicePort(servicePort);
        Communicator.getInstance(context).start();
        Intent serviceIntent = new Intent(context, CommunicatorService.class);

        serviceIntent.putExtra(CommunicatorService.EXTRA_ACTIVITY_CLASS,activityClass.getName());

        ContextCompat.startForegroundService(context,serviceIntent);

    }

    public void start(){
        if(activityClass!=null && context !=null){
            createNotificationChannel();
            startService();
        }
    }

    public void setNotificationMessage(String title, String content) {
        Communicator.getInstance(context).setNotificationMessage(title,content);

    }
}
