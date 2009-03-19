package java.net;

import java.util.Collection;
import java.util.List;

/**
 * @see java.net.NetworkInterface
 */
class NativeNetworkInterface {
    /**
     * @see java.net.NetworkInterface#getByIndex(int)
     */
    private static NetworkInterface getByIndex(int arg1) {
        //todo implement it
        //return null;
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.net.NetworkInterface#getAll()
     */
    private static NetworkInterface[] getAll() throws SocketException {
        //todo getNetDevices() &  getInetAddresses(dev) could return arrays
        final Collection<VMNetDevice> vmNetDevices = VMNetUtils.getAPI().getNetDevices();
        final NetworkInterface[] ret = new NetworkInterface[vmNetDevices.size()];
        int n = 0;
        for (VMNetDevice dev :  vmNetDevices) {
            List<InetAddress> al = VMNetUtils.getAPI().getInetAddresses(dev);
            ret[n] = new NetworkInterface(dev.getId(), n, al.toArray(new InetAddress[al.size()]));
            n++;
        }

        return ret;
    }

    /**
     * @see java.net.NetworkInterface#getByName0(java.lang.String)
     */
    private static NetworkInterface getByName0(String name) throws SocketException {
        final VMNetDevice dev = VMNetUtils.getAPI().getByName(name);
        if (dev != null) {
            List<InetAddress> al = VMNetUtils.getAPI().getInetAddresses(dev);
            //todo fix index
            return new NetworkInterface(dev.getId(), 0, al.toArray(new InetAddress[al.size()]));
        } else {
            throw new SocketException("No network interface found for name: " + name);
        }
    }

    /**
     * @see java.net.NetworkInterface#getByInetAddress0(java.net.InetAddress)
     */
    private static NetworkInterface getByInetAddress0(InetAddress inetAddress) throws SocketException {
        final VMNetDevice dev = VMNetUtils.getAPI().getByInetAddress(inetAddress);
        if (dev != null) {
            List<InetAddress> al = VMNetUtils.getAPI().getInetAddresses(dev);
            //todo fix index
            return new NetworkInterface(dev.getId(), 0, al.toArray(new InetAddress[al.size()]));
        } else {
            throw new SocketException("No network interface found for InetAddress: " + inetAddress);
        }
    }

    /**
     * @see java.net.NetworkInterface#getSubnet0(java.lang.String, int)
     */
    private static long getSubnet0(String arg1, int arg2) {
        //todo implement it
        //return 0;
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.net.NetworkInterface#getBroadcast0(java.lang.String, int)
     */
    private static Inet4Address getBroadcast0(String arg1, int arg2) {
        //todo implement it
        //return null;
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.net.NetworkInterface#isUp0(java.lang.String, int)
     */
    private static boolean isUp0(String arg1, int arg2) {
        //todo implement it
        //return false;
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.net.NetworkInterface#isLoopback0(java.lang.String, int)
     */
    private static boolean isLoopback0(String arg1, int arg2) {
        //todo implement it
        //return false;
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.net.NetworkInterface#supportsMulticast0(java.lang.String, int)
     */
    private static boolean supportsMulticast0(String arg1, int arg2) {
        //todo implement it
        //return false;
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.net.NetworkInterface#isP2P0(java.lang.String, int)
     */
    private static boolean isP2P0(String arg1, int arg2) {
        //todo implement it
        //return false;
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.net.NetworkInterface#getMacAddr0(byte[], java.lang.String, int)
     */
    private static byte[] getMacAddr0(byte[] arg1, String arg2, int arg3) {
        //todo implement it
        //return null;
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.net.NetworkInterface#getMTU0(java.lang.String, int)
     */
    private static int getMTU0(String arg1, int arg2) {
        //todo implement it
        //return 0;
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.net.NetworkInterface#init()
     */
    private static void init() {
        //nothing to do
    }
}
