package org.jnode.driver.video.vesa;

import org.vmmagic.unboxed.Address;

public class VesaUtils {
	public static boolean isEmpty(Address address, int size)
	{
		boolean empty = true;
		
		for(int i = 0 ; i < size ; i++)
		{
			if(address.add(i).loadByte() != 0)
			{
				empty = false;
				break;
			}
		}
		
		return empty;
	}
}
