/*
 * $Id$
 */
package org.jnode.test.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jnode.util.NumberUtils;

/**
 * @author gbin
 */
public class LfnTest {

	private static final String[] fileNames =
		new String[] {
			"This is a long filename test.ext",
			"With a long.extension",
			"With.a.multiple.extensions",
			"short.one",
			".ext" };

	public static void main(String[] args) {
		String directory = args[0];
		System.out.println("Create some files in directory = " + directory);

		// create files
		for (int i = 0; i < fileNames.length; i++) {
			args[0] = directory + "/" + fileNames[i];
			FileTest.main(args);
		}

		// list files
		listFiles(directory);

		// create a directory
		System.out.println("------------ create dir");
		File directoryToCreate = new File(directory + "/this is a long file name directory");
		directoryToCreate.mkdir();

		listFiles(directory);

		// delete a directory
		/*System.out.println("------------ delete dir");
		File directoryToDelete = new File(directory + "/this is a long file name directory");
		directoryToCreate.delete();

		listFiles(directory);

		// delete files
		System.out.println("------------ remove Files dir");
		for (int i = 0; i < fileNames.length; i++) {
			System.out.println("remove file entry = " + directory + "/" + fileNames[i]);
			new File(directory + "/" + fileNames[i]).delete();
		}

		listFiles(directory);*/

	}

	private static void listFiles(String directory) {
        File dir = new File(directory);
        String[] all = dir.list();
		for (int i = 0; i < all.length; i++) {
         File toTest =new File(all[i]); 
			System.out.println("dir entry = " + all[i] + " isDirectory = " + toTest.isDirectory());
			if (!toTest.isDirectory()) {
				System.out.print("MiniDump content :");
				byte[] raw = new byte[4];
				FileInputStream fis;
				try {
					fis = new FileInputStream(directory + "/" + all[i]);
					fis.read(raw);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println("hexdata = " + NumberUtils.hex(raw));
			}
		}
	}
}
