package neto.lc.geraldo.com.communicatorlib;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {

    private static final String TAG = TCPClient.class.getSimpleName();
    private String serverIp;
    private int serverPort;
    // message to send to the server
    private String serverMessage;
    // sends message received notifications
    private OnMessageListener messageListener = null;
    private OnConnectionChangedListener onConnectionChangedListener = null;
    // while this is true, the server will continue running
    private boolean running = false;
    // used to send messages
    private PrintWriter bufferOut;
    // used to read messages from the server
    private BufferedReader bufferIn;
    private Socket socket;
    private int timeout = 3000;
    private Thread connectionCheckThread;
    private boolean alive = true;
    private boolean reconnecting = false;
    private long lastHeartBeatTime;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    private boolean connected = true;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(String serverIp, int serverPort, OnMessageListener messageListener) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.messageListener = messageListener;
    }

    public TCPClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public TCPClient(String serverIp, int serverPort, OnConnectionChangedListener onConnectionChangedListener) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.onConnectionChangedListener = onConnectionChangedListener;
    }

    public void kill() {
        alive = false;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public OnMessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(OnMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (bufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    bufferOut.println(message);
                    bufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stop() {

        Log.d(TAG, "stop: ");
        running = false;
        //connected = false;
        if (bufferOut != null) {
            try {
                bufferOut.flush();
                bufferOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bufferIn = null;
        bufferOut = null;
        serverMessage = null;
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startConnectionStatusChecker() {
        connectionCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (alive) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    if (socket == null) {
                        resetConnection();
                        continue;
                    }
                    boolean connectedNow;
                    boolean isReachable;
                    connectedNow = checkHeartBeat(timeout);

                    //Log.e(TAG, "ConnectionCheck heartbeat:" + connectedNow);

                    if (!connectedNow) {

                        isReachable = checkReachable();
                        //Log.e(TAG, "ConnectionCheck reacheable:" + connectedNow);

                        connectedNow = checkHeartBeat(timeout);
                        if (isReachable && !connectedNow) {
                            resetConnection();
                        }
                    }

                    //Log.e(TAG, "ConnectionCheck: connectedNow: " + connectedNow);
                    //Log.e(TAG, "ConnectionCheck: connected: " + connectedNow);

                    if (connectedNow != connected) {

                        Log.e(TAG, "ConnectionCheck: state changed: "  + connectedNow );

                        if (onConnectionChangedListener != null)
                            onConnectionChangedListener.onConnectionChanged(connectedNow);
                    }

                    connected = connectedNow;

                }
            }
        });
        connectionCheckThread.start();
    }

    private boolean checkReachable() {
        boolean reachable = false;

        for(int i=4;i>=1;i--){
            try {
                reachable = socket.getInetAddress().isReachable(timeout/i);
            } catch (Exception e) {
                e.printStackTrace();
                reachable = false;
            }
            Log.e(TAG, "checkReachable: tryout: " + i + " -- " + timeout/i );

            if(reachable || checkHeartBeat(timeout)){
                break;
            }
        }

        return reachable;
    }

    private void resetConnection() {
        stop();
        start(new ConnectionStartListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private boolean checkHeartBeat(int timeout) {
        return (System.currentTimeMillis() - lastHeartBeatTime) < timeout;
    }

    public interface ConnectionStartListener {
        void onSuccess();

        void onError();
    }

    public void start(final ConnectionStartListener connectionStartListener) {
        if (running || !alive)
            return;
        updateHeartBeat();
        new Thread(new Runnable() {
            @Override
            public void run() {
                running = true;
                try {
                    if (connectionCheckThread == null || !connectionCheckThread.isAlive()) {
                        startConnectionStatusChecker();
                    }
                    socket = new Socket(serverIp, serverPort);
                    socket.setReuseAddress(true);
                    try {
                        bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                        Log.d(TAG, "run: starting client: " + socket.getInetAddress().getHostAddress());
                        connectionStartListener.onSuccess();
                        while (running && alive) {
                            String message = bufferIn.readLine();
                            //Log.d(TAG, "run: " + message);
                            if (message == null) {
                                //Log.d(TAG, "run: " + "disconnected!!!");
                                //stop();
                                //start();
                            }
                            if (message != null && message.equals("#H")) {
                                updateHeartBeat();
                            } else if (message != null && messageListener != null) {
                                messageListener.onMessageReceived(message);
                                //Log.d(TAG, "run: " + message);
                                updateHeartBeat();
                            }
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        connectionStartListener.onError();
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateHeartBeat() {
        lastHeartBeatTime = System.currentTimeMillis();
    }

    public void setOnConnectionChangedListener(OnConnectionChangedListener onConnectionChangedListener) {
        this.onConnectionChangedListener = onConnectionChangedListener;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}