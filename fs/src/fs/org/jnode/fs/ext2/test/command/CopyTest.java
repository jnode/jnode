package org.jnode.fs.ext2.test.command;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Sorry, this is not a proper JNode command...
 * @author Andras Nagy
 */
public class CopyTest {
	public CopyTest(String fname, String fname2) throws FileNotFoundException, IOException{
		byte[] bbuf=new byte[1024];
		
		FileInputStream  fis = new FileInputStream(fname);
		FileOutputStream fos = new FileOutputStream(fname2, false);
		while(fis.available()>0) {
			System.out.print(".");
			int len = fis.read(bbuf);
			fos.write(bbuf, 0, len);
		}
		
		fis.close();
		fos.close();
	}
	
	public static void main(String args[]) {
		String fname, fname2;
		if(args.length>=2) {
			fname = args[0];
			fname2= args[1];
		}else {
			System.out.println("copyTest fromFile toFile");
			return;
		}
			
		try{
			new CopyTest(fname, fname2);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
