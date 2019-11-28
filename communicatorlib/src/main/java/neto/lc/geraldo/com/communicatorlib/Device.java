package neto.lc.geraldo.com.communicatorlib;

import android.util.Log;

import java.util.ArrayList;

public class Device {
    private String name;
    private DeviceType deviceType;
    private TCPClient tcpClient;
    private String UUID;
    public ArrayList<OnMessageListener> onMessageListeners =  new ArrayList<>();
    private ArrayList<OnConnectionChangedListener> onConnectionChangedListeners = new ArrayList<>();

    public Device(String deviceName, DeviceType deviceType, TCPClient tcpClient) {
        this.deviceType = deviceType;
        this.tcpClient = tcpClient;
        this.name = deviceName;
    }

    public TCPClient getTcpClient() {
        return tcpClient;
    }

    public void setTcpClient(TCPClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    @Override
    public boolean equals(Object obj){
        return name.equals(((Device)obj).getName());
    }

    @Override
    public String toString(){
        return "Name: " + name + " DeviceType: " + getDeviceType() + " UUID: " + getUUID() + " IP: " + tcpClient.getServerIp();
    }

    public void start() {
        tcpClient.start(new TCPClient.ConnectionStartListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                tcpClient.start(this);
            }
        });
        tcpClient.setOnConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void onConnectionChanged(boolean connected) {
                for(OnConnectionChangedListener listener:onConnectionChangedListeners){
                    listener.onConnectionChanged(connected);
                }
            }
        });
        tcpClient.setMessageListener(new OnMessageListener() {
            @Override
            public void onMessageReceived(String message) {
                Log.e("Device", "onMessageReceived: " + onMessageListeners.size() );
                for(OnMessageListener listener:onMessageListeners){
                    listener.onMessageReceived(message);
                }
            }
        });
    }

    public void stop(){
        tcpClient.stop();
    }

    public void start(OnConnectionChangedListener onConnectionChangedListener,
                      OnMessageListener onMessageListener) {
        start();
        addOnConnectionChangedListener(onConnectionChangedListener);
        addOnMessageListener(onMessageListener);
    }

    public void addOnMessageListener(OnMessageListener onMessageListener) {
        if(!onMessageListeners.contains(onMessageListener))
            onMessageListeners.add(onMessageListener);
    }

    public void addOnConnectionChangedListener(OnConnectionChangedListener onConnectionChangedListener) {
        onConnectionChangedListeners.add(onConnectionChangedListener);
    }


    public void sendMessage(String message) {
        tcpClient.sendMessage(message);
    }

    public String getIp() {
        return tcpClient.getServerIp();
    }
}
