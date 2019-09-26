package neto.lc.geraldo.com.communicatorlib;


public interface CommunicatorMessageListener {
    void onMessage(long callId, CallAction callAction);
}
