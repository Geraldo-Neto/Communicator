package neto.lc.geraldo.com.communicatorlib;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import neto.lc.geraldo.com.communicator.MainActivity;

public class CommunicatorServiceStarter {
    private static final String TAG = "CommunicatorStarter";
    private Context context;
    private Class activityClass;


    public CommunicatorServiceStarter(Context context, Class activityClass){
        this.context = context;
        this.activityClass = activityClass;
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

    private void startWaiterCallerService(){
        Intent serviceIntent = new Intent(context, CommunicatorService.class);

        Log.e(TAG, "startWaiterCallerService: " + MainActivity.class.getName());
        serviceIntent.putExtra(CommunicatorService.EXTRA_ACTIVITY_CLASS,activityClass.getName());

        ContextCompat.startForegroundService(context,serviceIntent);
    }

    public void start(){
        if(activityClass!=null && context !=null){
            startWaiterCallerService();
            createNotificationChannel();
        }

    }
}
