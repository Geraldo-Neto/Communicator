package neto.lc.geraldo.com.communicatorlib;

public class Call {

    private CallableDevice device;

    private CallState state = CallState.WAITING;

    private long startTime;

    public Call(CallableDevice device,long startTime){
        this.device = device;
        this.startTime = startTime;
    }

    public Call(CallableDevice device){
        this.device = device;
        this.startTime = System.currentTimeMillis();
    }

    public CallableDevice getDevice() {
        return device;
    }

    public void setDevice(CallableDevice device) {
        this.device = device;
    }

    public CallState getState() {
        return state;
    }

    public void setState(CallState state) {
        this.state = state;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
