package neto.lc.geraldo.com.communicatorlib;

public interface DeviceDiscoveryListener {
    void onDeviceFound(Device device);

    void onDeviceRemoved(Device device);

    void onDeviceReconnected(Device device);
}
