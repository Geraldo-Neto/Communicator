package neto.lc.geraldo.com.communicatorlib;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Communicator {
    private static final String TAG = "Communicator";

    private static Communicator instance;

    private Context context;
    private Queue<DeviceMessage> incomingMessageQueue;
    private ArrayList<OnDeviceMessageListener> onDeviceMessageListeners;
    private ArrayList<DeviceDiscoveryListener> deviceDiscoveryListeners;
    private static boolean listening = false;
    public ArrayList<Device> deviceList;
    private NsdHelper nsdHelper;

    public Communicator(Context context) {
        this.context = context;
        incomingMessageQueue = new ConcurrentLinkedQueue<>();
        onDeviceMessageListeners = new ArrayList<>();
        deviceList = new ArrayList<>();
        deviceDiscoveryListeners = new ArrayList<>();
    }

    public static synchronized Communicator getInstance(Context context){
        if(instance==null){
            instance = new Communicator(context);
        }
        return instance;
    }

    public void addDeviceListener(DeviceDiscoveryListener listener){
        deviceDiscoveryListeners.add(listener);
        Log.e(TAG, "addDeviceListener: " + deviceDiscoveryListeners.size() );
    }

    public void addOnDeviceMessageListenerListener(OnDeviceMessageListener listener){
        onDeviceMessageListeners.add(listener);
    }

    public void addDeviceMessage(DeviceMessage deviceMessage){
        Log.d(TAG, "addDeviceMessage: " + deviceMessage.getDevice());
        if(!listening) {
            this.incomingMessageQueue.offer(deviceMessage);
            Log.e(TAG, "addDeviceMessage: queue" + deviceMessage.getDevice() );
        }
        else {
            /*CallAction callAction = resolveAction(deviceMessage.getMessage());
            if(callAction==null)
                return;*/
            //long callId = resolveId(deviceMessage.getMessage());
            Log.e(TAG, "addDeviceMessage: " + onDeviceMessageListeners.size() );
            for (OnDeviceMessageListener listener : onDeviceMessageListeners) {
                listener.onDeviceMessage(deviceMessage);
                Log.e(TAG, "addDeviceMessage: " + deviceMessage.getDevice() );
            }
        }

    }

    public void startDiscovery(){
        if(nsdHelper == null)
            nsdHelper = new NsdHelper(context);

        nsdHelper.initDeviceDiscovery(new NsdHelper.OnDeviceFoundListener() {
            @Override
            public void onDeviceFound(final Device device) {
                if (deviceList.contains(device)) {
                    Log.e(TAG, "onDeviceFound: RECONNECTED " + device);
                    deviceReconnected(device);
                    return;
                }

                Log.e(TAG, "onDeviceFound: NEW " + device);
                addDevice(device);
                device.addOnConnectionChangedListener(new OnConnectionChangedListener() {
                    @Override
                    public void onConnectionChanged(boolean connected) {
                        for(DeviceDiscoveryListener listener:deviceDiscoveryListeners){
                            if(connected)
                                listener.onDeviceReconnected(device);
                            else
                                listener.onDeviceRemoved(device);
                        }
                    }
                });
            }
        });
    }

    public void startServiceRegister(){
        if(nsdHelper == null)
            nsdHelper = new NsdHelper(context);

        MultiClientTCPServer multiClientTCPServer = new MultiClientTCPServer(5050, new OnClientMessageListener() {
            @Override
            public void onClientMessage(String message, Socket socket) {
                Log.e(TAG, "onClientMessage: " + message);
                DeviceMessage deviceMessage = new DeviceMessage(getDeviceFromSocket(socket), message);
                addDeviceMessage(deviceMessage);
            }
        }, new MultiClientTCPServer.OnServerStartedListener() {
            @Override
            public void onServerStarted(ServerSocket serverSocket) {
                nsdHelper.setServerIp(Utils.getIpAddress(context));
                nsdHelper.initializeRegistrationListener();
                nsdHelper.registerService(5050);
            }
        });
        multiClientTCPServer.start();
    }

    private Device getDeviceFromSocket(Socket socket) {
        for (Device device:deviceList){
            if(device.getIp().equals(socket.getInetAddress().getHostAddress()))
                return device;
        }
        return null;
    }


    public void startListening(){
        listening = true;
        while(incomingMessageQueue.size()>0){
            DeviceMessage deviceMessage = incomingMessageQueue.poll();
            /*CallAction callAction = resolveAction(deviceMessage.getMessage());
            if(callAction==null)
                return;
            long callId = resolveId(deviceMessage.getMessage());*/
            for (OnDeviceMessageListener listener : onDeviceMessageListeners) {
                listener.onDeviceMessage(deviceMessage);
            }
        }
    }

    public static long resolveId(String message) {
        String callerId = message.substring(message.indexOf("#")+3);
        return Long.parseLong(callerId);
    }

    public static CallAction resolveAction(String message) {
        if(message.contains("#WC")){
            return CallAction.RECEIVED;
        }else if(message.contains("#WA")){
            return CallAction.ANSWERED;
        }else
            return null;
    }

    public void notifyCallAnswered(long callId) throws IOException {
        String callIdString = String.valueOf(callId);
        for(Device device:deviceList){
            device.sendMessage("#WA" + callIdString);
        }
    }


    public void addDevice(Device device) {
        deviceList.add(device);
        for(DeviceDiscoveryListener listener:deviceDiscoveryListeners){
            listener.onDeviceFound(device);
        }
    }

    public void removeDevice(Device device) {
        deviceList.remove(device);
        for(DeviceDiscoveryListener listener:deviceDiscoveryListeners){
            listener.onDeviceRemoved(device);
        }
    }

    public void deviceReconnected(Device device) {
        for(DeviceDiscoveryListener listener:deviceDiscoveryListeners){
            listener.onDeviceReconnected(device);
        }
    }
}
