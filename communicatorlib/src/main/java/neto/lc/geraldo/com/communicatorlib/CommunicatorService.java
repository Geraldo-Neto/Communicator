package neto.lc.geraldo.com.communicatorlib;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CommunicatorService extends Service {

    public static final String EXTRA_ACTIVITY_CLASS = "activityClass";
    private static final String TAG = "CommunicatorPerService";
    public static final String CHANNEL_ID = "waiterCallerServiceChannel";
    private PowerManager.WakeLock wakeLock;
    private Handler handler;
    public static boolean started = false;
    private WifiManager.WifiLock wifiLock;
    private WifiManager.MulticastLock multicastLock;
    private NsdHelper nsdHelper;
    private Class activityClass;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand:" + "SERVICE STARTED" );

        /*if(!Communicator.getInstance(getApplicationContext()).isRunning()){
            stopSelf();
            stopForeground(true);
            Log.e(TAG, "onStartCommand:" + "SERVICE STOPPED" );
            stopService(new Intent(getApplicationContext(), this.getClass()));
            return super.onStartCommand(intent,flags,startId);
        }*/

        startNotification(intent);
        /*Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 3000,
                restartServicePendingIntent);
*/
        if (started)
            return START_NOT_STICKY;

        Log.e(TAG, "onStartCommand: STARTED STICKY" );
        handler = new Handler();
        initDeviceLookup();
        acquireWakeLock();
        started = true;

        return START_STICKY;
    }

    private void startNotification(Intent intent) {
        if(intent==null){
            return;
        }

        Communicator communicator = Communicator.getInstance(getApplicationContext());

        if (intent.getExtras().getString(EXTRA_ACTIVITY_CLASS) == null)
            return;
        try {
            activityClass = Class.forName(intent.getExtras().getString(EXTRA_ACTIVITY_CLASS));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Intent notificationIntent = new Intent(getApplicationContext(), activityClass);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(communicator.getNotificationTitle())
                .setContentText(communicator.getNotificationContent())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVibrate(new long[0])
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();

        startForeground(1, notification);
        Log.d(TAG, "startNotification: ");
    }

    private void initDeviceLookup() {
        configureTCPDevices();
    }

    private void configureTCPDevices() {
        Communicator.getInstance(getApplicationContext()).startDiscovery();
        Communicator.getInstance(getApplicationContext()).addDeviceListener(new DeviceDiscoveryListener() {
            @Override
            public void onDeviceFound(final Device device) {
                Log.e(TAG, "onDeviceFound: " + device);
                //device.start();
                device.addOnMessageListener(new OnMessageListener() {
                    @Override
                    public void onMessageReceived(String message) {
                        Log.e(TAG, "onMessageReceived: " + message);
                        Log.e(TAG, "onMessageReceived: " +
                                Communicator.getInstance(getApplicationContext()).deviceList.size());
                        if (message.contains("#WC")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showScreen();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onDeviceRemoved(Device device) {
                Log.e(TAG, "onDeviceRemoved: " + device.getName());
                //device.stop();
            }

            @Override
            public void onDeviceReconnected(final Device device) {
                Log.e(TAG, "onDeviceReconnected: " + device.getName());
            }
        });

        Communicator.getInstance(getApplicationContext()).startServiceRegister();
    }

    private void acquireWakeLock() {

        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ExampleApp:Wakelock");

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "ExampleApp:WifiLock");

        multicastLock = wm.createMulticastLock("ExampleApp:multicastLock");
        multicastLock.setReferenceCounted(false);

        if ((wakeLock != null) &&           // we have a WakeLock
                (!wakeLock.isHeld())) {  // but we don't hold it
            wakeLock.acquire();
        }
        if ((wifiLock != null) &&           // we have a WakeLock
                (!wifiLock.isHeld())) {  // but we don't hold it
            wifiLock.acquire();
        }

        if ((multicastLock != null) &&           // we have a WakeLock
                (!multicastLock.isHeld())) {  // but we don't hold it
            multicastLock.acquire();
        }
    }

    private void showScreen() {
        Intent it = new Intent(getApplicationContext(), activityClass);
        it.addFlags(FLAG_ACTIVITY_NEW_TASK);
        it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(it);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
