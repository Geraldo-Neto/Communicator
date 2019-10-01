package neto.lc.geraldo.com.communicatorlib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class EchoThread extends Thread {
    protected Socket socket;
    OnClientMessageListener messageListener;

    public EchoThread(Socket clientSocket, OnClientMessageListener messageListener) {
        this.socket = clientSocket;
        this.messageListener = messageListener;
    }

    public void startPinging(final DataOutputStream outputStream){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean running = true;
                while(running){
                    try {
                        outputStream.writeBytes("teus gay\n");
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        running = false;
                    }
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    public void run() {
        InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
            //startPinging(out);
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                line = brinp.readLine();
                this.messageListener.onClientMessage(line,socket);
                //out.writeBytes(line + "\n\r");
                //out.writeBytes("teus gay\n");
                //out.flush();


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
}