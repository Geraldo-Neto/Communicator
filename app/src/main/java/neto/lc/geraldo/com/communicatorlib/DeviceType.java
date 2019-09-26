package neto.lc.geraldo.com.communicatorlib;

public enum DeviceType {
    WAITER_CALLER("wc"), SCALE("sc"), NFC_SALE_DEVICE("npdv"), PDV_SMARTPHONE("spdv");
    private final String type;

    DeviceType(final String type){
        this.type = type;
    }

    @Override
    public String toString(){
        return type;
    }
    public static DeviceType getDeviceTypeFromString(String s){
        for(DeviceType deviceType:DeviceType.values()){
            if(s.contains(deviceType.toString()))
                return deviceType;
        }

        return null;
    }
}
