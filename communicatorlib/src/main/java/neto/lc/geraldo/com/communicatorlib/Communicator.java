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
    private String deviceName;
    private int servicePort;
    private String notificationTitle;
    private String notificationContent;
    private boolean running = false;
    private int deviceTimeout = 3000;

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

    public void restart(){
        this.context = context;
        incomingMessageQueue = new ConcurrentLinkedQueue<>();
        onDeviceMessageListeners = new ArrayList<>();
        deviceList = new ArrayList<>();
        deviceDiscoveryListeners = new ArrayList<>();
    }

    public void setDeviceName(String deviceName){
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void addDeviceListener(DeviceDiscoveryListener listener){
        deviceDiscoveryListeners.add(listener);
        Log.e(TAG, "addDeviceListener: " + deviceDiscoveryListeners.size() );
    }

    public void addOnDeviceMessageListener(OnDeviceMessageListener listener){
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

    public void clearDeviceListener(){
        deviceDiscoveryListeners.clear();
    }

    public void clearOnDeviceMessageListener(){
        onDeviceMessageListeners.clear();
    }

    public void removeDeviceListener(DeviceDiscoveryListener deviceDiscoveryListener){
        deviceDiscoveryListeners.remove(deviceDiscoveryListener);
    }

    public void removeOnDeviceMessageListener(OnDeviceMessageListener onDeviceMessageListener){
        onDeviceMessageListeners.remove(onDeviceMessageListener);
    }

    public void startDiscovery(){
        if(nsdHelper == null){
            nsdHelper = new NsdHelper(context);
            nsdHelper.setDeviceTimeout(deviceTimeout);
        }

        nsdHelper.initDeviceDiscovery(new NsdHelper.OnDeviceFoundListener() {
            @Override
            public void onDeviceFound(final Device device) {
                if(device.getName().equals(deviceName))
                    return;
                if (deviceList.contains(device)) {
                    Device prevDevice  = deviceList.get(deviceList.indexOf(device));
                    prevDevice.stop();
                    prevDevice.getTcpClient().kill();
                    prevDevice.setTcpClient(device.getTcpClient());
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
        if(nsdHelper == null){
            nsdHelper = new NsdHelper(context);
            nsdHelper.setDeviceTimeout(deviceTimeout);
        }


        MultiClientTCPServer multiClientTCPServer = new MultiClientTCPServer(servicePort, new OnClientMessageListener() {
            @Override
            public void onClientMessage(String message, Socket socket) {
                Log.e(TAG, "onClientMessage: " + message);
                if(message==null)
                    return;
                DeviceMessage deviceMessage = new DeviceMessage(getDeviceFromSocket(socket), message);
                addDeviceMessage(deviceMessage);
            }
        }, new MultiClientTCPServer.OnServerStartedListener() {
            @Override
            public void onServerStarted(ServerSocket serverSocket) {
                nsdHelper.setServerIp(Utils.getIpAddress(context));
                nsdHelper.initializeRegistrationListener();
                nsdHelper.registerService(serverSocket.getLocalPort(),deviceName);
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
        device.start();
        for(DeviceDiscoveryListener listener:deviceDiscoveryListeners){
            listener.onDeviceFound(device);
        }
    }

    public void removeDevice(Device device) {
        deviceList.remove(device);
        device.stop();
        for(DeviceDiscoveryListener listener:deviceDiscoveryListeners){
            listener.onDeviceRemoved(device);
        }
    }

    public void deviceReconnected(Device device) {

        device.start();
        for(DeviceDiscoveryListener listener:deviceDiscoveryListeners){
            listener.onDeviceReconnected(device);
        }
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public void setNotificationMessage(String title, String content) {
        this.notificationTitle = title;
        this.notificationContent = content;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        this.running = false;
    }

    public void start() {
        this.running = true;
    }

    public int getDeviceTimeout() {
        return deviceTimeout;
    }

    public void setDeviceTimeout(int deviceTimeout) {
        this.deviceTimeout = deviceTimeout;
    }
}
