package org.jnode.test.util;

import static org.junit.Assert.assertEquals;

import org.jnode.util.Version;
import org.junit.Test;

public class VersionTest {
    @Test
    public void testToString1() throws Exception {
        Version v = new Version(1, 2);
        assertEquals("1.2", v.toString());
    }

    @Test
    public void testToString1b() throws Exception {
        Version v = new Version(1, 2, "dev");
        assertEquals("1.2-dev", v.toString());
    }

    @Test
    public void testToString2() throws Exception {
        Version v = new Version(1, 2, 3);
        assertEquals("1.2.3", v.toString());
    }

    @Test
    public void testToString2b() throws Exception {
        Version v = new Version(1, 2, 3, "foo");
        assertEquals("1.2.3-foo", v.toString());
    }

    @Test
    public void testToString3() throws Exception {
        Version v = new Version(1, 2, 3, 4);
        assertEquals("1.2.3.4", v.toString());
    }

    @Test
    public void testToString3b() throws Exception {
        Version v = new Version(1, 2, 3, 4, "foo");
        assertEquals("1.2.3.4-foo", v.toString());
    }

    @Test
    public void testToStringA() throws Exception {
        Version v = new Version("5");
        assertEquals("5", v.toString());
    }

    @Test
    public void testToStringAb() throws Exception {
        Version v = new Version("5-foo");
        assertEquals("5-foo", v.toString());
    }

    @Test
    public void testToStringB() throws Exception {
        Version v = new Version("5.7");
        assertEquals("5.7", v.toString());
    }

    @Test
    public void testToStringBb() throws Exception {
        Version v = new Version("5.7-foo");
        assertEquals("5.7-foo", v.toString());
    }

    @Test
    public void testToStringC() throws Exception {
        Version v = new Version("1.5.2");
        assertEquals("1.5.2", v.toString());
    }

    @Test
    public void testToStringCb() throws Exception {
        Version v = new Version("1.5.2-dev");
        assertEquals("1.5.2-dev", v.toString());
    }

    @Test
    public void testToStringD() throws Exception {
        Version v = new Version("4.3.2.1");
        assertEquals("4.3.2.1", v.toString());
    }

    @Test
    public void testToStringDb() throws Exception {
        Version v = new Version("4.3.2.1-rel");
        assertEquals("4.3.2.1-rel", v.toString());
    }

    @Test
    public void testCompare2a() throws Exception {
        Version a = new Version(1, 2);
        Version b = new Version(1, 2);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    public void testCompare2b() throws Exception {
        Version a = new Version(2, 2);
        Version b = new Version(1, 2);
        assertEquals(1, a.compareTo(b));
    }

    @Test
    public void testCompare3a() throws Exception {
        Version a = new Version(1, 2, 3);
        Version b = new Version(1, 2, 3);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    public void testCompare4a() throws Exception {
        Version a = new Version(1, 2, 3, 4);
        Version b = new Version(1, 2, 3, 5);
        assertEquals(-1, a.compareTo(b));
    }

    @Test
    public void testCompare4b() throws Exception {
        Version a = new Version(1, 2, 3, 5);
        Version b = new Version(1, 2, 3, 4);
        assertEquals(1, a.compareTo(b));
    }

    @Test
    public void testCompare5a() throws Exception {
        Version a = new Version(1, 2);
        Version b = new Version(1, 2, 3);
        assertEquals(-1, a.compareTo(b));
    }

    @Test
    public void testCompare5b() throws Exception {
        Version a = new Version(1, 2, 0);
        Version b = new Version(1, 2);
        assertEquals(1, a.compareTo(b));
    }

    @Test
    public void testCompare5c() throws Exception {
        Version a = new Version(1, 2, 3);
        Version b = new Version(1, 2, 3, 0);
        assertEquals(-1, a.compareTo(b));
    }

    @Test
    public void testCompare5d() throws Exception {
        Version a = new Version(1, 2, 3, 0);
        Version b = new Version(1, 2, 3);
        assertEquals(1, a.compareTo(b));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError2a() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version(-1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError2b() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version(1, -2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError3a() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version(-1, 2, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError3b() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version(1, -2, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError3c() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version(1, 2, -3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError4a() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version(-1, 2, 3, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError4b() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version(1, -2, 3, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError4c() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version(1, 2, -3, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError4d() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version(1, 2, 3, -4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError5a() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version("-1.2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError5b() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version("1.-2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError5c() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version("1.2.-3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError5d() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version("1.2.3.-4");
    }

    @Test(expected = NumberFormatException.class)
    public void testError6a() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version("jon");
    }

    @Test(expected = NumberFormatException.class)
    public void testError6b() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version("1.jon");
    }

    @Test(expected = NumberFormatException.class)
    public void testError6c() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version("1.2.jon");
    }

    @Test(expected = NumberFormatException.class)
    public void testError6d() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version("1.2.3.jon");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError7a() throws Exception {
        @SuppressWarnings("unused")
        Version a = new Version("1.2.3.4.5");
    }
}
