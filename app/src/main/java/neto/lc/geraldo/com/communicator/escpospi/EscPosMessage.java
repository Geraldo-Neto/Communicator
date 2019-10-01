package neto.lc.geraldo.com.communicator.escpospi;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EscPosMessage {
    private String messages;

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public void write(byte message){
        byte[] bytes = new byte[]{message};
        write(bytes);
    }


    public void write(byte[] bytes)  {
        String str = new String(bytes, StandardCharsets.ISO_8859_1);
        messages = messages + "\n" + str;
    }

    public void write(int i) {
        byte[] b = {(byte)i};
        write(b);
    }
}
