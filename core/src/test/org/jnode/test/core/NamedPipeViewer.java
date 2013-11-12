package org.jnode.test.core;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class NamedPipeViewer {

	public static void main(String[] args) throws IOException {
		File pipeFile = new File("\\\\.\\pipe\\jnode-com1");
		final RandomAccessFile raf = new RandomAccessFile(pipeFile, "rw");
		
		Thread readThread = new Thread(new Runnable() {			
			@Override
			public void run() {
				int ch;
				try {
					while ((ch = raf.read()) >= 0) {
						System.out.print((char)ch);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		readThread.start();
		
		// Write to kdb
		int ch;
		System.out.println("Started. Press q to exit");
		while ((ch = System.in.read()) >= 0) {
			System.out.println("read " + (char)ch);
			if (ch == 'q') {
				break;
			}
			raf.write(ch);
		}
				
		System.out.println("Closing...");
		readThread.interrupt();
		raf.close();
	}

}
