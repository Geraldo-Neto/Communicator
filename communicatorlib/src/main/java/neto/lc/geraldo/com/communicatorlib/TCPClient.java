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

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    private boolean connected;

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
        if (bufferOut != null) {
            try{
            bufferOut.flush();
            bufferOut.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        bufferIn = null;
        bufferOut = null;
        serverMessage = null;
    }

    public void startConnectionStatusChecker(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(200);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        if(socket==null)
                            continue;
                        boolean connectedNow = socket.getInetAddress().isReachable(timeout);
                        if(connectedNow != connected){
                            connected = connectedNow;
                            if(onConnectionChangedListener!=null)
                                onConnectionChangedListener.onConnectionChanged(connected);
                            if(connected)
                                start();
                            else
                                stop();
                            Log.d(TAG, "run: " + "KEPP_ALIVE" + connected);
                        }
                        connected = connectedNow;


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    public void start() {
        if(running)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                running = true;
                try{
                    
                    socket = new Socket(serverIp,serverPort);
                    socket.setReuseAddress(true);
                    startConnectionStatusChecker();
                    try{
                        bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                        Log.d(TAG, "run: starting client: " + socket.getInetAddress().getHostAddress());
                        while (running){
                            String message = bufferIn.readLine();
                            Log.d(TAG, "run: " + message);
                            if(message==null){
                                //Log.d(TAG, "run: " + "disconnected!!!");
                                stop();
                                //start();
                            }
                            if(message!= null && messageListener!=null){
                                messageListener.onMessageReceived(message);
                                //Log.d(TAG, "run: " + message);
                            }
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }catch (Exception e){
                        e.printStackTrace();

                    }finally {
                      socket.close();
                    }
                }catch (Exception e ){
                    e.printStackTrace();
                }
            }
        }).start();
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