/**
 * $Id$
 */
package org.jnode.fs.fat.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLConnection;

import org.jnode.driver.DriverException;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.fat.BootSector;
import org.jnode.fs.fat.FatDirectory;
import org.jnode.fs.fat.FatFileSystem;
import org.jnode.fs.fat.FatFormatter;
import org.jnode.fs.fat.GrubFatFormatter;
import org.jnode.plugin.PluginException;
import org.jnode.util.FileUtils;

/**
 * <description>
 * 
 * @author epr
 */
public class FatTest {

	public static void main(String[] args) throws Exception {

		PrintWriter out = new PrintWriter(System.out, true);

		/*String fn;
		if (args.length == 0) {
			fn = "lib/floppy.img";
		} else {
			fn = args[0];
		}
		printInfo(new File(fn), out);
		
		System.out.println("\nGenerating new floppy");*/

		File f = new File("build/testfloppy.img");

		createFloppy(f);
		printInfo(f, out);
	}

	public static void printInfo(File file, PrintWriter out)
		throws IOException, PluginException, DriverException, FileSystemException {
		FileDevice fd = new FileDevice(file, "r");
		FatFileSystem fs = new FatFileSystem(fd);
		try {
			BootSector bs = fs.getBootSector();
			bs.read(fd);

			out.println("OEM name          " + bs.getOemName());
			out.println("bytes/sector      " + bs.getBytesPerSector());
			out.println("sectors/cluster   " + bs.getSectorsPerCluster());
			out.println("#reserved sectors " + bs.getNrReservedSectors());
			out.println("#fats             " + bs.getNrFats());
			out.println("#rootdir entries  " + bs.getNrRootDirEntries());
			out.println("#logical sectors  " + bs.getNrLogicalSectors());
			out.println(
				"Medium descriptor 0x"
					+ Integer.toHexString(bs.getMediumDescriptor()));
			out.println("sectors/fat       " + bs.getSectorsPerFat());
			out.println("sectors/track     " + bs.getSectorsPerTrack());
			out.println("#heads            " + bs.getNrHeads());
			out.println("#hidden sectors   " + bs.getNrHiddenSectors());

			fs.getFat().printTo(out);
			fs.getRootDir().printTo(out);

			try {
				FatDirectory dir = (FatDirectory) fs.getRootEntry().getDirectory().getEntry("AAP").getDirectory();
				dir.printTo(out);
			} catch (FileNotFoundException ex) {
				out.println("No AAP directory");
			}

			try {
				FatDirectory dir = (FatDirectory) fs.getRootEntry().getDirectory().getEntry("boot").getDirectory();
				dir.printTo(out);
			} catch (FileNotFoundException ex) {
				out.println("No boot directory");
			}
			
		} finally {
			//fd.stop();
			fd.close();
		}
	}

	public static void createFloppy(File f) throws Exception {

      GrubFatFormatter ff = new GrubFatFormatter(0, null, null);
		FileDevice newFd = new FileDevice(f, "rw");
		newFd.setLength(1440 * 1024);
		ff.format(newFd);

		//newFd.start();
		FatFileSystem fs = new FatFileSystem(newFd);

		FSDirectory dir = fs.getRootEntry().getDirectory();
		FSDirectory bDir = dir.addDirectory("boot").getDirectory();
		FSDirectory bgDir = bDir.addDirectory("grub").getDirectory();

		URLConnection urlConn =
			FatTest
				.class
				.getClassLoader()
				.getResource("menu.lst")
				.openConnection();
		byte[] buf = new byte[urlConn.getContentLength()];
		FileUtils.copy(urlConn.getInputStream(), buf);

		final FSFile fh1 = dir.addFile("test.lst").getFile();
		fh1.setLength(urlConn.getContentLength());
		fh1.write(0, buf, 0, buf.length);

		final FSFile fh2 = bgDir.addFile("menu.lst").getFile();
		fh2.setLength(urlConn.getContentLength());
		fh2.write(0, buf, 0, buf.length);

		fs.flush();
	
		//newFd.stop();
		newFd.close();
	}
}
