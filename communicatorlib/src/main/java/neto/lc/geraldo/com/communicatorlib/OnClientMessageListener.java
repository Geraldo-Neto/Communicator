package neto.lc.geraldo.com.communicatorlib;

import java.net.Socket;

public interface OnClientMessageListener {
    void onClientMessage(String message, Socket socket);
}
