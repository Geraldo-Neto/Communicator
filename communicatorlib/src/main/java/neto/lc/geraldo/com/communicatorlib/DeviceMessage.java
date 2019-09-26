package neto.lc.geraldo.com.communicatorlib;

public class DeviceMessage {
    private Device device;
    private String message;

    public DeviceMessage(Device device, String message) {
        this.device = device;
        this.message = message;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
