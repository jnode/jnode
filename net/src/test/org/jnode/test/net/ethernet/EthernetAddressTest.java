package org.jnode.test.net.ethernet;

import org.jnode.net.HardwareAddress;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;

import junit.framework.TestCase;


public class EthernetAddressTest extends TestCase {

    private static final String TEST_MAC_ADDRESS_STRING = "00-14-22-49-DD-2B";
    private static final String TEST_MAC_ADDRESS_STRING_DISPLAY = "00:14:22:49:DD:2B";
    private static final byte TEST_MAC_ADDRESS_BYTE_0 = (byte) 0x00;
    private static final byte TEST_MAC_ADDRESS_BYTE_1 = (byte) 0x14;
    private static final byte TEST_MAC_ADDRESS_BYTE_2 = (byte) 0x22;
    private static final byte TEST_MAC_ADDRESS_BYTE_3 = (byte) 0x49;
    private static final byte TEST_MAC_ADDRESS_BYTE_4 = (byte) 0xDD;
    private static final byte TEST_MAC_ADDRESS_BYTE_5 = (byte) 0x2B;
    private static final String TEST_MAC_ADDRESS_STRING_WRONG = "00-14-22-49-XX-2B";
    private static final String TEST_MAC_ADDRESS_STRING_WRONG_LENGTH = "00-14-22-49-DD-2B-32";

    public void testEqualsObject() {
        fail("Not yet implemented");
    }

    public void testHashCode() {
        fail("Not yet implemented");
    }

    public void testToString() {
        EthernetAddress mac =
                new EthernetAddress(TEST_MAC_ADDRESS_BYTE_0, TEST_MAC_ADDRESS_BYTE_1,
                        TEST_MAC_ADDRESS_BYTE_2, TEST_MAC_ADDRESS_BYTE_3, TEST_MAC_ADDRESS_BYTE_4,
                        TEST_MAC_ADDRESS_BYTE_5);
        assertEquals(TEST_MAC_ADDRESS_STRING_DISPLAY, mac.toString());
    }

    public void testEthernetAddressByteArrayInt() {
        fail("Not yet implemented");
    }

    public void testEthernetAddressSocketBufferInt() {
        fail("Not yet implemented");
    }

    public void testEthernetAddressString() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertNotNull(mac);
        try {
            mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING_WRONG_LENGTH);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Invalid address"));
        }
        try {
            mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING_WRONG);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Not an ethernet address"));
        }
    }

    public void testEthernetAddressByteByteByteByteByteByte() {
        EthernetAddress mac =
                new EthernetAddress(TEST_MAC_ADDRESS_BYTE_0, TEST_MAC_ADDRESS_BYTE_1,
                        TEST_MAC_ADDRESS_BYTE_2, TEST_MAC_ADDRESS_BYTE_3, TEST_MAC_ADDRESS_BYTE_4,
                        TEST_MAC_ADDRESS_BYTE_5);
        assertNotNull(mac);
    }

    public void testEqualsEthernetAddress() {
        EthernetAddress mac =
                new EthernetAddress(TEST_MAC_ADDRESS_BYTE_0, TEST_MAC_ADDRESS_BYTE_1,
                        TEST_MAC_ADDRESS_BYTE_2, TEST_MAC_ADDRESS_BYTE_3, TEST_MAC_ADDRESS_BYTE_4,
                        TEST_MAC_ADDRESS_BYTE_5);
        assertTrue(mac.equals(new EthernetAddress(TEST_MAC_ADDRESS_STRING)));
    }

    public void testEqualsHardwareAddress() {
        EthernetAddress mac =
                new EthernetAddress(TEST_MAC_ADDRESS_BYTE_0, TEST_MAC_ADDRESS_BYTE_1,
                        TEST_MAC_ADDRESS_BYTE_2, TEST_MAC_ADDRESS_BYTE_3, TEST_MAC_ADDRESS_BYTE_4,
                        TEST_MAC_ADDRESS_BYTE_5);
        HardwareAddress mac2 = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertTrue(mac.equals(mac2));
    }

    public void testGetLength() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertEquals(EthernetConstants.ETH_ALEN, mac.getLength());
    }

    public void testGet() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertEquals(TEST_MAC_ADDRESS_BYTE_0, mac.get(0));
        assertEquals(TEST_MAC_ADDRESS_BYTE_1, mac.get(1));
        assertEquals(TEST_MAC_ADDRESS_BYTE_2, mac.get(2));
        assertEquals(TEST_MAC_ADDRESS_BYTE_3, mac.get(3));
        assertEquals(TEST_MAC_ADDRESS_BYTE_4, mac.get(4));
        assertEquals(TEST_MAC_ADDRESS_BYTE_5, mac.get(5));
    }

    public void testWriteToSocketBufferInt() {
        fail("Not yet implemented");
    }

    public void testWriteToByteArrayInt() {
        fail("Not yet implemented");
    }

    public void testIsBroadcast() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertFalse(mac.isBroadcast());
        EthernetAddress broadcast = new EthernetAddress("FF-FF-FF-FF-FF-FF");
        assertTrue(broadcast.isBroadcast());
    }

    public void testGetDefaultBroadcastAddress() {
        fail("Not yet implemented");
    }

    public void testGetType() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertEquals(1, mac.getType());
    }

}
