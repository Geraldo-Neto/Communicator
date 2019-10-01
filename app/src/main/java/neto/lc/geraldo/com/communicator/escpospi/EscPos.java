package neto.lc.geraldo.com.communicator.escpospi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;



import java.io.IOException;
import java.nio.charset.StandardCharsets;

import neto.lc.geraldo.com.communicator.escpospi.exception.BarcodeSizeError;
import neto.lc.geraldo.com.communicator.escpospi.image.Image;
public class EscPos {

    // Feed control sequences
    private static final byte[] CTL_LF          = {0x0a};          // Print and line feed
    // Line Spacing
    private static final byte[] LINE_SPACE_24   = {0x1b,0x33,24}; // Set the line spacing at 24
    private static final byte[] LINE_SPACE_30   = {0x1b,0x33,30}; // Set the line spacing at 30
    //Image
    private static final byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33};
    // Printer hardware
    private static final byte[] HW_INIT         = {0x1b,0x40};          // Clear data in buffer and reset modes
    // Cash Drawer
    private static final byte[] CD_KICK_2       = {0x1b,0x70,0x00};      // Sends a pulse to pin 2 []
    private static final byte[] CD_KICK_5       = {0x1b,0x70,0x01};      // Sends a pulse to pin 5 []
    // Paper
    private static final byte[]  PAPER_FULL_CUT = {0x1d,0x56,0x00}; // Full cut paper
    private static final byte[]  PAPER_PART_CUT = {0x1d,0x56,0x01}; // Partial cut paper
    // Text format
    private static final byte[] TXT_NORMAL      = {0x1b,0x21,0x00}; // Normal text
    private static final byte[] TXT_2HEIGHT     = {0x1b,0x21,0x10}; // Double height text
    private static final byte[] TXT_2WIDTH      = {0x1b,0x21,0x20}; // Double width text
    private static final byte[] TXT_4SQUARE     = {0x1b,0x21,0x30}; // Quad area text
    private static final byte[] TXT_UNDERL_OFF  = {0x1b,0x2d,0x00}; // Underline font OFF
    private static final byte[] TXT_UNDERL_ON   = {0x1b,0x2d,0x01}; // Underline font 1-dot ON
    private static final byte[] TXT_UNDERL2_ON  = {0x1b,0x2d,0x02}; // Underline font 2-dot ON
    private static final byte[] TXT_BOLD_OFF    = {0x1b,0x45,0x00}; // Bold font OFF
    private static final byte[] TXT_BOLD_ON     = {0x1b,0x45,0x01}; // Bold font ON
    private static final byte[] TXT_FONT_A      = {0x1b,0x4d,0x00}; // Font type A
    private static final byte[] TXT_FONT_B      = {0x1b,0x4d,0x01};// Font type B
    private static final byte[] TXT_ALIGN_LT    = {0x1b,0x61,0x00}; // Left justification
    private static final byte[] TXT_ALIGN_CT    = {0x1b,0x61,0x01}; // Centering
    private static final byte[] TXT_ALIGN_RT    = {0x1b,0x61,0x02}; // Right justification
    // Char code table
    private static final byte[] CHARCODE_PC437  = {0x1b,0x74,0x00}; // USA){ Standard Europe
    private static final byte[] CHARCODE_JIS    = {0x1b,0x74,0x01}; // Japanese Katakana
    private static final byte[] CHARCODE_PC850  = {0x1b,0x74,0x02}; // Multilingual
    private static final byte[] CHARCODE_PC860  = {0x1b,0x74,0x03}; // Portuguese
    private static final byte[] CHARCODE_PC863  = {0x1b,0x74,0x04}; // Canadian-French
    private static final byte[] CHARCODE_PC865  = {0x1b,0x74,0x05}; // Nordic
    private static final byte[] CHARCODE_WEU    = {0x1b,0x74,0x06}; // Simplified Kanji, Hirakana
    private static final byte[] CHARCODE_GREEK  = {0x1b,0x74,0x07}; // Simplified Kanji
    private static final byte[] CHARCODE_HEBREW = {0x1b,0x74,0x08}; // Simplified Kanji
    private static final byte[] CHARCODE_PC1252 = {0x1b,0x74,0x10}; // Western European Windows Code Set
    private static final byte[] CHARCODE_PC866  = {0x1b,0x74,0x12}; // Cirillic //2
    private static final byte[] CHARCODE_PC852  = {0x1b,0x74,0x13}; // Latin 2
    private static final byte[] CHARCODE_PC858  = {0x1b,0x74,0x14}; // Euro
    private static final byte[] CHARCODE_THAI42 = {0x1b,0x74,0x15}; // Thai character code 42
    private static final byte[] CHARCODE_THAI11 = {0x1b,0x74,0x16}; // Thai character code 11
    private static final byte[] CHARCODE_THAI13 = {0x1b,0x74,0x17}; // Thai character code 13
    private static final byte[] CHARCODE_THAI14 = {0x1b,0x74,0x18}; // Thai character code 14
    private static final byte[] CHARCODE_THAI16 = {0x1b,0x74,0x19}; // Thai character code 16
    private static final byte[] CHARCODE_THAI17 = {0x1b,0x74,0x1a}; // Thai character code 17
    private static final byte[] CHARCODE_THAI18 = {0x1b,0x74,0x1b}; // Thai character code 18

    // Barcode format
    private static final byte[] BARCODE_TXT_OFF = {0x1d,0x48,0x00}; // HRI printBarcode chars OFF
    private static final byte[] BARCODE_TXT_ABV = {0x1d,0x48,0x01}; // HRI printBarcode chars above
    private static final byte[] BARCODE_TXT_BLW = {0x1d,0x48,0x02}; // HRI printBarcode chars below
    private static final byte[] BARCODE_TXT_BTH = {0x1d,0x48,0x03}; // HRI printBarcode chars both above and below
    private static final byte[] BARCODE_FONT_A  = {0x1d,0x66,0x00}; // Font type A for HRI printBarcode chars
    private static final byte[] BARCODE_FONT_B  = {0x1d,0x66,0x01}; // Font type B for HRI printBarcode chars
    private static final byte[] BARCODE_HEIGHT  = {0x1d,0x68,0x64}; // Barcode Height [1-255]
    private static final byte[] BARCODE_WIDTH   = {0x1d,0x77,0x03}; // Barcode Width  [2-6]
    private static final byte[] BARCODE_UPC_A   = {0x1d,0x6b,0x00}; // Barcode type UPC-A
    private static final byte[] BARCODE_UPC_E   = {0x1d,0x6b,0x01}; // Barcode type UPC-E
    private static final byte[] BARCODE_EAN13   = {0x1d,0x6b,0x02}; // Barcode type EAN13
    private static final byte[] BARCODE_EAN8    = {0x1d,0x6b,0x03}; // Barcode type EAN8
    private static final byte[] BARCODE_CODE39  = {0x1d,0x6b,0x04}; // Barcode type CODE39
    private static final byte[] BARCODE_ITF     = {0x1d,0x6b,0x05}; // Barcode type ITF
    private static final byte[] BARCODE_NW7     = {0x1d,0x6b,0x06}; // Barcode type NW7
    // Printing Density
    private static final byte[] PD_N50          = {0x1d,0x7c,0x00}; // Printing Density -50%
    private static final byte[] PD_N37          = {0x1d,0x7c,0x01}; // Printing Density -37.5%
    private static final byte[] PD_N25          = {0x1d,0x7c,0x02}; // Printing Density -25%
    private static final byte[] PD_N12          = {0x1d,0x7c,0x03}; // Printing Density -12.5%
    private static final byte[] PD_0            = {0x1d,0x7c,0x04}; // Printing Density  0%
    private static final byte[] PD_P50          = {0x1d,0x7c,0x08}; // Printing Density +50%
    private static final byte[] PD_P37          = {0x1d,0x7c,0x07}; // Printing Density +37.5%
    private static final byte[] PD_P25          = {0x1d,0x7c,0x06}; // Printing Density +25%
    private static final byte[] PD_P12          = {0x1d,0x7c,0x05}; // Printing Density +12.5%

    public static int TOTAL_CHAR=45,DIV1=10,DIV2=5,DIV3=10,DIV4=10,DIV5=10
            ,PRINT_MODE_FONT_A = 0
            ,PRINT_MODE_FONT_B = 1
            ,PRINT_MODE_FONT_C = 2
            ,PRINT_MODE_EMPHASIZED = 8
            ,PRINT_MODE_DOUBLE_HEIGHT = 16
            ,PRINT_MODE_DOUBLE_WIDTH = 32
            ,PRINT_MODE_DOUBLE_ITALIC= 64
            ,PRINT_MODE_UNDERLINE = 128;
    private EscPosMessage escPosMessage;
    private final Context context;

    public EscPos(Context context,EscPosMessage escPosMessage){
        this.context = context;
        this.escPosMessage = escPosMessage;
    }

    public EscPosMessage getEscPosMessage() {
        return escPosMessage;
    }

    public void setEscPosMessage(EscPosMessage escPosMessage) {
        this.escPosMessage = escPosMessage;
    }

    public void clearMessages(){
        escPosMessage.setMessages("");
    }
    public void print(String text) throws IOException {
        escPosMessage.write(text.getBytes(StandardCharsets.ISO_8859_1));
    }

    public void printLn(String text) throws IOException {
        print(text + "\n");
    }

    public void printString(String str) throws IOException {
        Log.i("PRINTER_PRE",str);
        escPosMessage.write(str.getBytes(StandardCharsets.ISO_8859_1));

    }

    public void setPrintMode(int mode) throws IOException {
        escPosMessage.write(0x1B);
        escPosMessage.write("!".getBytes());
        escPosMessage.write(mode);
    }

    public void initialize() throws IOException {
        escPosMessage.write(0x1B);
        escPosMessage.write("@".getBytes());
    }

    public void setCharWidth(int width) throws IOException {
        escPosMessage.write(0x1D);
        escPosMessage.write(0x21);
        escPosMessage.write(16*width);
    }
    public void setCharHeight(int height) throws IOException {
        escPosMessage.write(0x1D);
        escPosMessage.write(0x21);
        escPosMessage.write(height);
    }
    public void setCharSize(int size) throws IOException {
        escPosMessage.write(0x1D);
        escPosMessage.write(0x21);
        escPosMessage.write(size | 16*size);
    }

    public void storeString(String str) throws IOException {
        escPosMessage.write(str.getBytes());


    }
    public void printStorage() throws IOException {
        escPosMessage.write(0xA);


    }
    public void feed(int feed) throws IOException {
        //escInit();
        escPosMessage.write(0x1B);
        escPosMessage.write("d".getBytes());
        escPosMessage.write(feed);


    }

    public void selectCodePage(int codePage) throws IOException {

        escPosMessage.write(0x1B);
        escPosMessage.write(0x74);
        escPosMessage.write(codePage);

    }
    public void printAndFeed(String str, int feed) throws IOException {
        //escInit();
        escPosMessage.write(str.getBytes(StandardCharsets.ISO_8859_1));
        escPosMessage.write(0x1B);
        escPosMessage.write("d".getBytes());
        escPosMessage.write(feed);


    }
    public void setBold(Boolean bool) throws IOException {
        escPosMessage.write(0x1B);
        escPosMessage.write("E".getBytes());
        escPosMessage.write((int)(bool?1:0));
    }
    /**
     * Sets white on black printing
     * */
    public void setInverse(Boolean bool) throws IOException {
        bool=false;
        escPosMessage.write(0x1D);
        escPosMessage.write("B".getBytes());
        escPosMessage.write( (int)(bool ? 1:0) );

    }
    public void resetToDefault() throws IOException {
        setInverse(false);
        setBold(false);
        setUnderline(0);
        setJustification(0);
        setPrintMode(0);
    }
    /**
     * Sets underline and weight
     *
     * @param val
     *      0 = no underline.
     *      1 = single weight underline.
     *      2 = double weight underline.
     * */
    public void setUnderline(int val) throws IOException {
        escPosMessage.write(0x1B);
        escPosMessage.write("-".getBytes());
        escPosMessage.write(val);
    }
    /**
     * Sets left, center, right justification
     *
     * @param val
     *      0 = left justify.
     *      1 = center justify.
     *      2 = right justify.
     * */

    public void setJustification(int val) throws IOException {
        escPosMessage.write(0x1B);
        escPosMessage.write("a".getBytes());
        escPosMessage.write(val);
    }
    public void setLeftRight(String left,String right) throws IOException {
        escPosMessage.write(0x1B);
        escPosMessage.write("a".getBytes());
        escPosMessage.write(0);
        printString(left);

        escPosMessage.write(0x1B);
        escPosMessage.write("a".getBytes());
        escPosMessage.write(2);
        printString(right);

    }
    public void printBarcode(String code, int type, int h, int w, int font, int pos) throws IOException {

        //need to test for errors in length of code
        //also control for input type=0-6

        //GS H = HRI position
        escPosMessage.write(0x1D);
        escPosMessage.write("H".getBytes());
        escPosMessage.write(pos); //0=no print, 1=above, 2=below, 3=above & below

        //GS f = set barcode characters
        escPosMessage.write(0x1D);
        escPosMessage.write("f".getBytes());
        escPosMessage.write(font);

        //GS h = sets barcode height
        escPosMessage.write(0x1D);
        escPosMessage.write("h".getBytes());
        escPosMessage.write(h);

        //GS w = sets barcode width
        escPosMessage.write(0x1D);
        escPosMessage.write("w".getBytes());
        escPosMessage.write(w);//module = 1-6

        //GS k
        escPosMessage.write(0x1D); //GS
        escPosMessage.write("k".getBytes()); //k
        escPosMessage.write(type);//m = barcode type 0-6
        escPosMessage.write(code.length()); //length of encoded string
        escPosMessage.write(code.getBytes());//d1-dk
        escPosMessage.write(0);//print barcode

    }

    public void beep() throws IOException {
        escPosMessage.write(0x1B);
        escPosMessage.write("(A".getBytes());
        escPosMessage.write(4);
        escPosMessage.write(0);
        escPosMessage.write(48);
        escPosMessage.write(55);
        escPosMessage.write(3);
        escPosMessage.write(15);
    }

    public void setLineSpacing(int spacing) throws IOException {
        //function ESC 3
        escPosMessage.write(0x1B);
        escPosMessage.write("3".getBytes());
        escPosMessage.write(spacing);

    }
    public void cut() throws IOException {
        escPosMessage.write(0x1D);
        escPosMessage.write("V".getBytes());
        escPosMessage.write(48);
        escPosMessage.write(0);
    }

    public void setEncoding(int i) throws IOException {
        escPosMessage.write(0x1B);
        escPosMessage.write(0x52);
        escPosMessage.write(i);
    }

    public void lineBreak() throws IOException {
        lineBreak(1);
    }

    public void lineBreak(int nbLine) throws IOException {
        for (int i=0;i<nbLine;i++) {
            escPosMessage.write(0x0A);
        }
    }
    public void cutPart() throws IOException {
        cut("PART");
    }

    public void cutFull() throws IOException {
        cut("FULL");
    }

    private void cut(String mode) throws IOException {
        for (int i=0;i<6;i++){
            escPosMessage.write(CTL_LF);
        }
        if (mode.toUpperCase().equals("PART")){
            escPosMessage.write(PAPER_PART_CUT);
        }else{
            escPosMessage.write(PAPER_FULL_CUT);
        }
    }

    public void printBarcode(String code, String bc, int width, int height, String pos, String font) throws BarcodeSizeError, IOException {
        // Align Bar Code()
        escPosMessage.write(TXT_ALIGN_CT);
        // Height
        if (height >=2 || height <=6) {
            escPosMessage.write(BARCODE_HEIGHT);
        }else {
            throw new BarcodeSizeError("Incorrect Height");
        }
        //Width
        if (width >= 1 || width <=255) {
            escPosMessage.write(BARCODE_WIDTH);
        }else {
            throw new BarcodeSizeError("Incorrect Width");
        }
        //Font
        if (font.equalsIgnoreCase("B")) {
            escPosMessage.write(BARCODE_FONT_B);
        }else {
            escPosMessage.write(BARCODE_FONT_A);
        }
        //Position
        if (pos.equalsIgnoreCase("OFF")) {
            escPosMessage.write(BARCODE_TXT_OFF);
        }else if (pos.equalsIgnoreCase("BOTH")) {
            escPosMessage.write(BARCODE_TXT_BTH);
        }else if (pos.equalsIgnoreCase("ABOVE")) {
            escPosMessage.write(BARCODE_TXT_ABV);
        }else {
            escPosMessage.write(BARCODE_TXT_BLW);
        }
        //Type
        switch(bc.toUpperCase()){
            case "UPC-A":
                escPosMessage.write(BARCODE_UPC_A);
                break;
            case "UPC-E":
                escPosMessage.write(BARCODE_UPC_E);
                break;
            default: case "EAN13":
                escPosMessage.write(BARCODE_EAN13);
                break;
            case "EAN8":
                escPosMessage.write(BARCODE_EAN8);
                break;
            case "CODE39":
                escPosMessage.write(BARCODE_CODE39);
                break;
            case "ITF":
                escPosMessage.write(BARCODE_ITF);
                break;
            case "NW7":
                escPosMessage.write(BARCODE_NW7);
                break;
        }
        //Print Code
        if (!code.equals("")) {
            escPosMessage.write(code.getBytes());
            escPosMessage.write(CTL_LF);
        } else {
            throw new BarcodeSizeError("Incorrect Value");
        }
    }


    public void setTextDensity(int density) throws IOException {
        switch (density){
            case 0:
                escPosMessage.write(PD_N50);
                break;
            case 1:
                escPosMessage.write(PD_N37);
                break;
            case 2:
                escPosMessage.write(PD_N25);
                break;
            case 3:
                escPosMessage.write(PD_N12);
                break;
            case 4:
                escPosMessage.write(PD_0);
                break;
            case 5:
                escPosMessage.write(PD_P12);
                break;
            case 6:
                escPosMessage.write(PD_P25);
                break;
            case 7:
                escPosMessage.write(PD_P37);
                break;
            case 8:
                escPosMessage.write(PD_P50);
                break;
        }
    }

    public void printImage(String filePath) throws IOException {
        printImage(BitmapFactory.decodeFile(filePath));
    }

    public void printImage(int drawable) throws IOException {
        printImage(BitmapFactory.decodeResource(context.getResources(),drawable));
    }

    private void printImage(Bitmap image) throws IOException {
        Image img = new Image();
        int[][] pixels = img.getPixelsSlow(image);
        for (int y = 0; y < pixels.length; y += 24) {
            escPosMessage.write(LINE_SPACE_24);
            escPosMessage.write(SELECT_BIT_IMAGE_MODE);
            escPosMessage.write(new byte[]{(byte)(0x00ff & pixels[y].length), (byte)((0xff00 & pixels[y].length) >> 8)});
            for (int x = 0; x < pixels[y].length; x++) {
                escPosMessage.write(img.recollectSlice(y, x, pixels));
            }
            escPosMessage.write(CTL_LF);
        }
        escPosMessage.write(CTL_LF);
        escPosMessage.write(LINE_SPACE_30);
    }

    public void printImage(int drawable, int width, int height) throws IOException {
        printImage(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),drawable), width, height, false));

    }

    public void setCharCode(String code) throws IOException {
        switch (code){
            case "USA":
                escPosMessage.write(CHARCODE_PC437);
                break;
            case "JIS":
                escPosMessage.write(CHARCODE_JIS);
                break;
            case "MULTILINGUAL":
                escPosMessage.write(CHARCODE_PC850);
                break;
            case "PORTUGUESE":
                escPosMessage.write(CHARCODE_PC860);
                break;
            case "CA_FRENCH":
                escPosMessage.write(CHARCODE_PC863);
                break;
            default: case "NORDIC":
                escPosMessage.write(CHARCODE_PC865);
                break;
            case "WEST_EUROPE":
                escPosMessage.write(CHARCODE_WEU);
                break;
            case "GREEK":
                escPosMessage.write(CHARCODE_GREEK);
                break;
            case "HEBREW":
                escPosMessage.write(CHARCODE_HEBREW);
                break;
            case "WPC1252":
                escPosMessage.write(CHARCODE_PC1252);
                break;
            case "CIRILLIC2":
                escPosMessage.write(CHARCODE_PC866);
                break;
            case "LATIN2":
                escPosMessage.write(CHARCODE_PC852);
                break;
            case "EURO":
                escPosMessage.write(CHARCODE_PC858);
                break;
            case "THAI42":
                escPosMessage.write(CHARCODE_THAI42);
                break;
            case "THAI11":
                escPosMessage.write(CHARCODE_THAI11);
                break;
            case "THAI13":
                escPosMessage.write(CHARCODE_THAI13);
                break;
            case "THAI14":
                escPosMessage.write(CHARCODE_THAI14);
                break;
            case "THAI16":
                escPosMessage.write(CHARCODE_THAI16);
                break;
            case "THAI17":
                escPosMessage.write(CHARCODE_THAI17);
                break;
            case "THAI18":
                escPosMessage.write(CHARCODE_THAI18);
                break;
        }
    }

    public void init() throws IOException {
        escPosMessage.write(HW_INIT);
    }

    public void openCashDrawerPin2() throws IOException {
        escPosMessage.write(CD_KICK_2);
    }

    public void openCashDrawerPin5() throws IOException {
        escPosMessage.write(CD_KICK_5);
    }


}