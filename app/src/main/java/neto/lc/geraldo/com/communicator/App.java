package neto.lc.geraldo.com.communicator;

import android.app.Application;

import neto.lc.geraldo.com.communicatorlib.CommunicatorServiceStarter;
import neto.lc.geraldo.com.communicatorlib.Utils;


public class App extends Application {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        CommunicatorServiceStarter communicatorServiceStarter =
                new CommunicatorServiceStarter(getApplicationContext(),
                        MainActivity.class,
                        "pos" + Utils.getDeviceMAC().replace(":",""),
                        41156);
        communicatorServiceStarter.setNotificationMessage("ThingsPOS","Escutando por chamadas!");
        communicatorServiceStarter.setDeviceTimeout(30000);

        communicatorServiceStarter.start();
    }
}
