package org.jnode.fs.ext2.test.command;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Sorry, this is not a proper JNode command...
 * @author Andras Nagy
 *
 */
public class FillTest {
	public FillTest(String fname, int kilos) throws FileNotFoundException, IOException{
		byte[] bbuf=new byte[1024];
		for(int i=0; i<1024; i++)
			bbuf[i]=(byte)(i%8+65);
		
		FileOutputStream fos = new FileOutputStream(fname, false);
		int written=0;
		while(written<kilos) {
			if(written%10 == 0)
				System.out.print(".");
			fos.write(bbuf, 0, 1024);
			written++;
			if((written%100)==0)
				System.out.println(written+" KB");
		}
		
		fos.close();
	}
	
	public static void main(String args[]) {
		String fname=null;
		int kilos=0;
		if(args.length>=2) {
			fname = args[0];
			kilos=Integer.valueOf(args[1]).intValue();
		}else {
			System.out.println("2 args: [FILENAME] [MEGABYTES TO WRITE]");
		}
			
		try{
			new FillTest(fname, kilos*1024);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
