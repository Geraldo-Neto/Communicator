package neto.lc.geraldo.com.communicator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import neto.lc.geraldo.com.communicatorlib.Communicator;
import neto.lc.geraldo.com.communicatorlib.Device;
import neto.lc.geraldo.com.communicatorlib.DeviceDiscoveryListener;
import neto.lc.geraldo.com.communicatorlib.DeviceMessage;
import neto.lc.geraldo.com.communicatorlib.OnConnectionChangedListener;
import neto.lc.geraldo.com.communicatorlib.OnDeviceMessageListener;


public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    private boolean connected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                //| WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                //| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        );
        Log.e(TAG, "onCreate: ");

        final Communicator communicator = Communicator.getInstance(getApplicationContext());
        communicator.addDeviceListener(new DeviceDiscoveryListener() {
            @Override
            public void onDeviceFound(final Device device) {
                Log.e(TAG, "onDeviceFound: " + device);
                device.addOnConnectionChangedListener(new OnConnectionChangedListener() {
                    @Override
                    public void onConnectionChanged(boolean connected) {
                        if(connected)
                            device.sendMessage("Reconnected");

                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i=0;
                        while (true){
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if(!connected)
                                continue;
                            device.sendMessage(String.valueOf(i));
                            i++;

                        }
                    }
                }).start();

            }

            @Override
            public void onDeviceRemoved(Device device) {
                Log.e(TAG, "onDeviceRemoved: " );
                connected = false;

            }

            @Override
            public void onDeviceReconnected(Device device) {
                Log.e(TAG, "onDeviceReconnected: " );
                connected = true;

            }
        });

        communicator.addOnDeviceMessageListenerListener(new OnDeviceMessageListener() {
            @Override
            public void onDeviceMessage(DeviceMessage deviceMessage) {
                Log.e(TAG, "onDeviceMessage: " + deviceMessage.getMessage() + " : " + deviceMessage.getDevice());
            }
        });

        communicator.startListening();
    }

}
