package neto.lc.geraldo.com.communicatorlib;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class EchoThread extends Thread {
    private static final String TAG = "EchoThread";
    protected Socket socket;
    OnClientMessageListener messageListener;
    private Thread heartBeatThread;
    private DataOutputStream out;

    public EchoThread(Socket clientSocket, OnClientMessageListener messageListener) {
        this.socket = clientSocket;
        this.messageListener = messageListener;
        Log.e(TAG, "EchoThread: created" );
    }

    public void run() {
        InputStream inp = null;
        BufferedReader brinp = null;
        out = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
            startHeartBeating();
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                line = brinp.readLine();
                if(line==null){
                    Log.e(TAG, "run: EchoThread client closed connection" );
                    //TODO provide better solution!!
                    this.socket.close();
                    return;
                }

                Log.e(TAG, "run: " + line );
                this.messageListener.onClientMessage(line,socket);
            } catch (IOException e) {
                //e.printStackTrace();
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startHeartBeating() {
        heartBeatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(socket!=null && socket.isClosed())
                        break;
                    try {
                        out.write("#H\n".getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        heartBeatThread.start();
    }
}