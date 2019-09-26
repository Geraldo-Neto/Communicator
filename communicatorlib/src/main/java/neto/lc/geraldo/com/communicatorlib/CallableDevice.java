package neto.lc.geraldo.com.communicatorlib;

public class CallableDevice {
    private long id;
    private String name;

    public CallableDevice(long id, String name) {
        this.id = id;
        this.name = name;
    }


    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof CallableDevice))
            return false;
        return id==((CallableDevice)obj).getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
