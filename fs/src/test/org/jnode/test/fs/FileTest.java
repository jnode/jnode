/*
 * $Id$
 */
package org.jnode.test.fs;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author gbin
 */
public class FileTest {

	public static void main(String[] args) {
        System.out.println("Create a file " + args[0]);
        try {
			FileOutputStream fos = new FileOutputStream(args[0]);
         byte [] nimp = new byte[] {(byte)0xDE, (byte)0xAD, (byte)0xCA, (byte)0xFE}; 
         fos.write(nimp);
         fos.close();
            
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}
}
