package org.jnode.driver.video.vesa;

import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class VESACommand {
	private static ResourceManager manager;
	private static ResourceOwner owner = new SimpleResourceOwner("VESACommand");
	
	public static void main(String[] args) throws NameNotFoundException, ResourceNotFreeException {
		System.out.println("VESA detected : "+detect());
	}
	
	public static ByteBuffer getBiosMemory() throws NameNotFoundException, ResourceNotFreeException
	{
		// steps 1 & 2 : allocate new buffer and copy the bios image to it 
		manager = InitialNaming.lookup(ResourceManager.NAME);
		Address start = Address.fromInt(0xC0000);
		int size = 0x7FFF; // 32 Kb
		int mode = ResourceManager.MEMMODE_NORMAL; 
		MemoryResource resource = manager.claimMemoryResource(owner, start, size, mode);
		ByteBuffer buffer = ByteBuffer.allocate(size);
		for(int i = 0 ; i < size ; i++)
		{
			buffer.put(resource.getByte(i));
		}
		
		//manager.
		
		buffer.rewind();
		return buffer;
	}
	
	public static PMInfoBlock detect() throws NameNotFoundException, ResourceNotFreeException
	{
		ByteBuffer biosMemory = getBiosMemory();
		
		// step 3 : scan to the bios image to find signature and check the validity of the checksum
		final int signatureLength = 4;
		PMInfoBlock pmInfoBlock = null;
		
		for(int offset = 0 ; offset < biosMemory.limit() - (signatureLength - 1) ; offset++)
		{
			int pos = biosMemory.position() + 1;
			
			boolean p = (biosMemory.get() == (byte)'P'); 
			boolean m = (biosMemory.get() == (byte)'M');
			boolean i = (biosMemory.get() == (byte)'I'); 
			boolean d = (biosMemory.get() == (byte)'D');
/*
			byte b0 = biosMemory.get();
			boolean p = (b0 == (byte)'P') || (b0 == (byte)'D');
			byte b = biosMemory.get();
			boolean m = (b == (byte)'M') || (b == (byte)'I');
			b = biosMemory.get();
			boolean i = (b == (byte)'I') || (b == (byte)'M');
			b = biosMemory.get();
			boolean d = (b == (byte)'D') || (b == (byte)'P');
*/			
			if(p)
			{
				//System.out.println("offset="+NumberUtils.hex(offset)+" value="+b+" p="+p+" m="+m+" i="+i+" d="+d);
				System.out.println("offset="+NumberUtils.hex(offset)+" p="+p+" m="+m+" i="+i+" d="+d);
			}
			
			if(p && m && i && d)
			{
				System.out.println("signature detected at offset "+NumberUtils.hex(offset));
				byte checksum = (byte) (((byte)'P') + ((byte)'M') + ((byte)'I') + ((byte)'D'));
				//int size = 7 * 4 + 2 * 1;
				int size = 7 * 2 + 2 * 1;
				for(int offs = 0 ; offs < size ; offs++)
				{
					checksum += (byte) biosMemory.get();
					System.out.println("at offset "+NumberUtils.hex(offs)+" checksum="+checksum);
				}
				
				if(checksum == 0)
				{
					System.out.println("found correct checksum");
					biosMemory.position(pos + 3).limit();
					pmInfoBlock = new PMInfoBlock(biosMemory);
					break;
				}
				else
				{
					System.err.println("bad checksum");
				}
			}
			
			biosMemory.position(pos);
		}
			
		VmProcessor.current().dumpStatistics(System.out);
    	System.out.println("after dumpStatistics");		
		if(pmInfoBlock != null)
		{
	    	System.out.println("step4");		
			// step 4 
			byte[] biosDataSel = new byte[0x600]; // should be filled with zeros by the VM
	    	System.out.println("step4.1");		
	    	short selector = getSelector(biosDataSel);
	    	System.out.println("step4.2");		
			pmInfoBlock.setBiosDataSel(selector);
						
	    	System.out.println("step5");		
			// step 5
			int size = 0x7FFF; // 32 Kb
			int mode = ResourceManager.MEMMODE_NORMAL;
			int address = 0xA0000;
			MemoryResource resource = manager.claimMemoryResource(owner, Address.fromInt(address), size, mode);

			System.err.println("....");
			System.err.println("....");
			System.err.println("before call to vbe function");
			int codePtr = pmInfoBlock.getEntryPoint();
			System.err.println("codePtr="+NumberUtils.hex(codePtr));
			int result = Unsafe.callVbeFunction(Address.fromInt(codePtr), 0, Address.fromInt(address));
			System.err.println("codePtr="+result);
		}
		
		return pmInfoBlock;
	}
	
	public static short getSelector(Address address)
	{
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.println("getSelector point 1");				
		long addr = address.toLong();
    	System.out.println("getSelector point 2");				
		short result = (short) ((addr & 0xFFFFFFFF00000000L) >> 32);
    	System.out.println("getSelector point 3");				
		return result;
	}
	
	private static short getSelector(Object obj)
	{
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
    	System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");System.out.print(".");
		
    	System.out.println("getSelector point A");				
		if(obj == null) return -1;
		
    	System.out.println("getSelector point B");				
		ObjectReference objRef = ObjectReference.fromObject(obj);
		
    	System.out.println("getSelector point C");				
		return (objRef == null) ? null : getSelector(objRef.toAddress());
	}
}
