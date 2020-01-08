package neto.lc.geraldo.com.communicator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import neto.lc.geraldo.com.communicator.escpospi.EscPos;
import neto.lc.geraldo.com.communicator.escpospi.EscPosMessage;
import neto.lc.geraldo.com.communicator.escpospi.PrinterUtils;
import neto.lc.geraldo.com.communicatorlib.Communicator;
import neto.lc.geraldo.com.communicatorlib.CommunicatorServiceStarter;
import neto.lc.geraldo.com.communicatorlib.Device;
import neto.lc.geraldo.com.communicatorlib.DeviceDiscoveryListener;
import neto.lc.geraldo.com.communicatorlib.DeviceMessage;
import neto.lc.geraldo.com.communicatorlib.OnConnectionChangedListener;
import neto.lc.geraldo.com.communicatorlib.OnDeviceMessageListener;
import neto.lc.geraldo.com.communicatorlib.Utils;


public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    private boolean connected = true;
    private Button buttonPrint;
    private Device printerDevice;

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
                /*device.addOnConnectionChangedListener(new OnConnectionChangedListener() {
                    @Override
                    public void onConnectionChanged(boolean connected) {
                        if(connected)
                            device.sendMessage("Reconnected");

                    }
                });

                if(device.getName().contains("prt")){
                    printerDevice = device;
                }

                if(!device.getName().contains("pos")){
                    return;
                }

                //testDeviceWifi(device);
*/

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

        communicator.addOnDeviceMessageListener(new OnDeviceMessageListener() {
            @Override
            public void onDeviceMessage(DeviceMessage deviceMessage) {
                Log.e(TAG, "onDeviceMessage: " + deviceMessage.getMessage() + " : " + deviceMessage.getDevice());
                Log.e(TAG, "onDeviceMessage: List Size: " + communicator.deviceList.size() );
            }
        });

        communicator.startListening();

        buttonPrint = findViewById(R.id.buttonPrint);
        buttonPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick:" + communicator.deviceList.size() );
                /*for(Device device:communicator.deviceList){
                    if(device.getName().contains("prt"))
                        printMessage(device);
                }*/
                if(communicator.isRunning()){
                    communicator.stop();
                } else {
                    CommunicatorServiceStarter communicatorServiceStarter =
                            new CommunicatorServiceStarter(getApplicationContext(),
                                    MainActivity.class,
                                    "pos" + Utils.getDeviceMAC().replace(":",""),
                                    41156);
                    communicatorServiceStarter.setNotificationMessage("ThingsPOS","Escutando por chamadas!");
                    communicatorServiceStarter.setDeviceTimeout(6000);
                    communicatorServiceStarter.start();
                }

            }
        });
    }

    private void printMessage(final Device device) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                EscPos printer = new EscPos(getApplicationContext(),new EscPosMessage());

                PrinterUtils.printProduct(printer,"Teste2",9.99,1,1);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("commands",printer.getEscPosMessage().getMessages());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e(TAG,  jsonObject.toString());

                if(device!=null)
                    device.sendMessage(jsonObject.toString());
            }
        }).start();

    }

    private void testDeviceWifi(final Device device) {
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

}
