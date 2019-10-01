package neto.lc.geraldo.com.communicator.escpospi;

import java.io.IOException;
import java.util.Calendar;

import neto.lc.geraldo.com.communicator.R;

public class PrinterUtils {

    public static String getCurrentDate(){

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        String months = String.valueOf(month).length() == 1 ? ("0" + String.valueOf(month)) : String.valueOf(month);
        String years = String.valueOf(year).length() == 1 ? ("0" + String.valueOf(year)) : String.valueOf(year);
        String days = String.valueOf(day).length() == 1 ? ("0" + String.valueOf(day)) : String.valueOf(day);

        return days + "-" + months + "-" + years;
    }

    public static String getCurrentTime(){
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        String minutes = String.valueOf(min).length() == 1 ? ("0" + String.valueOf(min)) : String.valueOf(min);
        String seconds = String.valueOf(sec).length() == 1 ? ("0" + String.valueOf(sec)) : String.valueOf(sec);
        String hours = String.valueOf(hour).length() == 1 ? ("0" + String.valueOf(hour)) : String.valueOf(hour);

        return hours + ":" + minutes + ":" + seconds;
    }


    static public String formatPrice(double totalPrice) {
        String price = String.valueOf(totalPrice).replace(".",",");
        price = "R$" + price  + (price.split(",")[1].length()==2 ? "":"0");
        return price;
    }

    public static void printProduct(EscPos printer,String name, Double price,int sectorNumber, int counter){
        try {
            String date = getCurrentDate();
            String time = getCurrentTime();
            printer.init();
            printer.setEncoding(12);
            printer.selectCodePage(39);
            printer.setJustification(2);
            printer.setCharSize(1);
            printer.printLn(String.valueOf(sectorNumber*10000 + counter));
            printer.setJustification(1);
            printer.setCharSize(0);
            printer.printImage(R.drawable.saude_bw,216,120);
            printer.printLn("\n");
            printer.setCharSize(1);
            printer.setJustification(2);
            printer.printLn(name + "\n");
            printer.setCharSize(4);
            printer.printLn(formatPrice(price));
            printer.printLn("\n\n\n");
            printer.setCharSize(1);
            printer.setJustification(0);
            printer.printLn(" " + date + "     " + time+ "\n");
            printer.cutFull();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
