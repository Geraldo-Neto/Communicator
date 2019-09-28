package neto.lc.geraldo.com.communicatorlib;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiClientTCPServer extends Thread {

    private OnServerStartedListener onServerStartedListener;
    private int port;
    private OnClientMessageListener onClientMessageListener;

    public MultiClientTCPServer(int port, OnClientMessageListener onClientMessageListener,
                                OnServerStartedListener onServerStartedListener){
        this.port = port;
        this.onClientMessageListener = onClientMessageListener;
        this.onServerStartedListener = onServerStartedListener;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        onServerStartedListener.onServerStarted(serverSocket);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // new thread for a client
            new EchoThread(socket,onClientMessageListener).start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnClientMessageListener(OnClientMessageListener onClientMessageListener) {
        this.onClientMessageListener = onClientMessageListener;
    }

    public interface OnServerStartedListener{
        void onServerStarted(ServerSocket serverSocket);
    }
}