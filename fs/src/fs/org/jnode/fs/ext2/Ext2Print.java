/*
 * $Id$
 */
package org.jnode.fs.ext2;

import org.apache.log4j.Logger;
import org.jnode.driver.block.BlockDeviceAPI;

/**
 * @author Andras Nagy
 */
public class Ext2Print {
	private static final Logger log = Logger.getLogger("EXT2");
	public static String hexFormat(int i) {
		String pad="00000000";
		String res=Integer.toHexString(i);
		int len=Math.max(0,8-res.length());
		res=pad.substring(0,len)+res;
		return res;
	}
	
	private static int unsignedByte(byte i) {
		if(i<0)
			return 256+i;
		else
			return i;
	}

	public static String hexFormat(byte b) {
		int i=unsignedByte(b);
		String pad="00";
		String res=Integer.toHexString(i);
		int len=Math.max(0,2-res.length());
		res=pad.substring(0,len)+res;
		return res;
	}
			
	public static void dumpData(BlockDeviceAPI api, int offset, int length) {
		byte[] data = new byte[length];
		try{
			api.read(offset, data, 0, length);
		}catch(Exception e) {
			return;
		}
		int pageWidth=16;
		for(int i=0; i<length; i+=pageWidth) {
			System.out.print(hexFormat(i)+": ");
			for(int j=0; j<pageWidth; j++) 
				if(i+j<length) {
					log.info(hexFormat(data[i+j])+" ");
					if((i+j)%4==3)
						System.out.print(" - ");
				}	
			System.out.println();
		}						
	}	
}
