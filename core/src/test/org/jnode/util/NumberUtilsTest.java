package org.jnode.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class NumberUtilsTest {
    @Test
    public void testToString() throws Exception {
        String result = NumberUtils.toString(15.2365f,2);
        assertEquals("15.23",result);
    }
    
    @Test
    public void testToStringNeg() throws Exception {
        String result = NumberUtils.toString(-15.2365f,2);
        assertEquals("-15.23",result);
    }
    
    @Test
    public void testHexInt(){
    	String result = NumberUtils.hex(255);
    	assertEquals("000000ff",result.toLowerCase());
    }
    
    @Test
    public void testHexIntNeg(){
    	String result = NumberUtils.hex(-1);
    	assertEquals("ffffffff",result.toLowerCase());
    }
    
    @Test
    public void testHexIntMax(){
    	String result = NumberUtils.hex(Integer.MAX_VALUE);
    	assertEquals("7fffffff",result.toLowerCase());
    }
    
    @Test
    public void testHexLong(){
    	String result = NumberUtils.hex(255L);
    	assertEquals("00000000000000ff",result.toLowerCase());
    }
    
    @Test
    public void testHexLongNeg(){
    	String result = NumberUtils.hex(-1L);
    	assertEquals("ffffffffffffffff",result.toLowerCase());
    }
    
    @Test
    public void testHexLongMax(){
    	String result = NumberUtils.hex(Long.MAX_VALUE);
    	assertEquals("7fffffffffffffff",result.toLowerCase());
    }
    
    @Test
    public void testHexWithLength(){
    	String result = NumberUtils.hex(255,2);
    	assertEquals("ff",result.toLowerCase());
    }
    
    @Test
    public void testToDecimalByte(){
    	String result = NumberUtils.toDecimalByte(65536);
    	assertEquals("65.53 kb",result.toLowerCase());
    }
    
    @Test
    public void testToBinaryByte(){
    	String result = NumberUtils.toBinaryByte(65536);
    	assertEquals("64.0 kb",result.toLowerCase());
    }
    
    @Test
    public void testGetSizeUnit(){
    	assertEquals(1024,NumberUtils.getSizeUnit("1K").getMultiplier());
    	assertEquals("K",NumberUtils.getSizeUnit("1K").getUnit());
    }
    
    @Test
    public void testGetSize(){
    	assertEquals(1024,NumberUtils.getSize("1K"));
    }
}
