package org.jnode.fs.ext2.test.command;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Sorry, this is not a proper JNode command...
 * @author Andras Nagy
 */
public class WriteTest {
	public WriteTest(String fname) throws FileNotFoundException, IOException{
		byte[] bbuf=new byte[20];
		for(byte i=0; i<20; i++)
			bbuf[i]=(byte)(i+65);
		FileOutputStream fos = new FileOutputStream(fname, false);
		fos.write(bbuf);
		fos.close();
	}

	public WriteTest(String fname, String text) throws FileNotFoundException, IOException{
		FileWriter writer = new FileWriter(fname);
		writer.write(text.toCharArray());
		writer.close();
	}
	
	public static void main(String args[]) {
		String fname;
		if(args.length>0)
			fname = args[0];
		else {
			System.out.println("writeTest filename [some_text]");
			return;
		}
			
			
		try{
			if(args.length>1)
				new WriteTest(fname, args[1]);
			else
				new WriteTest(fname);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
