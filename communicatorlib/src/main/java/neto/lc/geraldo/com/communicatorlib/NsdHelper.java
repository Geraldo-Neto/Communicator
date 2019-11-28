package neto.lc.geraldo.com.communicatorlib;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;

public class NsdHelper {

    private static final String TAG = "NsdHelper";
    private Context context;
    private NsdManager.RegistrationListener registrationListener;
    private String serviceName = DeviceType.PDV_SMARTPHONE.toString();
    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private String SERVICE_TYPE ="_things._tcp"; // "_services._dns-sd._udp";
    private NsdManager.ResolveListener resolveListener;
    private OnDeviceFoundListener onDeviceFoundListener;
    private String hostAddress = "";
    private int deviceTimeout = 3000;

    public void setServerIp(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public interface OnDeviceFoundListener {
        void onDeviceFound(Device device);
    }

    public NsdHelper(Context context){
        this.context = context;
        this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

    }

    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "onServiceRegistered: " + serviceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.d(TAG, "onServiceUnregistered: ");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
    }

    public void registerService(int port, String deviceName) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(deviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    private void initializeDiscoveryListener() {
        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(final NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (service.getServiceType().contains("things")) {
                    /*if (service.getServiceName().equals(serviceName))
                        return;//same device*/
                    nsdManager.resolveService(service, new NsdManager.ResolveListener() {

                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            // Called when the resolve fails. Use the error code to debug.
                            Log.e(TAG, "Resolve failed: " + errorCode);
                            if(errorCode == NsdManager.FAILURE_ALREADY_ACTIVE ){
                                nsdManager.resolveService(service,this);
                            }
                            Log.e(TAG, "Resolve failed: " + serviceInfo);

                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                            Log.d(TAG, "onServiceResolved: " + serviceInfo.getHost().getHostAddress());

                            int port = serviceInfo.getPort();
                            InetAddress host = serviceInfo.getHost();
                            if(host.getHostAddress().equals(hostAddress) || host.getHostAddress().contains("127.0.0.1")){
                                Log.e(TAG, "onServiceResolved: " + host.getHostAddress() + " | " + Utils.getIpAddress(context) );
                                return;
                            }

                            DeviceType deviceType = DeviceType.getDeviceTypeFromString(serviceInfo.getServiceName());
                            TCPClient tcpClient = new TCPClient(host.getHostAddress(),port);
                            tcpClient.setTimeout(deviceTimeout);
                            Device device = new Device(serviceInfo.getServiceName(),deviceType,tcpClient);
                            onDeviceFoundListener.onDeviceFound(device);
                        }
                    });
                    Log.d(TAG, "onServiceFound: Resolving service!");
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }


    public void initDeviceDiscovery(OnDeviceFoundListener deviceFoundListener) {
        this.onDeviceFoundListener = deviceFoundListener;
        initializeDiscoveryListener();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }


    public int getDeviceTimeout() {
        return deviceTimeout;
    }

    public void setDeviceTimeout(int deviceTimeout) {
        this.deviceTimeout = deviceTimeout;
    }

    public void tearDown() {
        if(registrationListener!=null && discoveryListener!=null){
            nsdManager.unregisterService(registrationListener);
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
    }

    public void stopDiscovery(){
        if(discoveryListener!=null)
            nsdManager.stopServiceDiscovery(discoveryListener);
    }
}
