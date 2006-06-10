/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;



/**
 * @author gvt
 */
public class FatFileSystem extends AbstractFileSystem {
    private static final Logger log =
        Logger.getLogger ( FatFileSystem.class );

    private Fat fat;
    private final CodePage cp;
    

    public FatFileSystem ( Device device, String codePageName, boolean readOnly  )
	throws FileSystemException {
	super ( device, readOnly );
	
	try {
	    fat = Fat.create ( getApi() );
	}
	catch ( IOException ex ) {
	    throw new FileSystemException ( ex );
	}
	catch ( Exception e ) {
	    throw new FileSystemException ( e );
	}

	cp = CodePage.forName ( codePageName );
    }

    public FatFileSystem ( Device device, boolean readOnly )
	throws FileSystemException {
	this ( device, "ISO_8859_1", readOnly );
    }


    public int getClusterSize() {
	return fat.getClusterSize();
    }
    

    public Fat getFat() {
	return fat;
    }


    public BootSector getBootSector() {
	return fat.getBootSector();
    }


    public CodePage getCodePage() {
	return cp;
    }


    protected FSFile createFile ( FSEntry entry )
	throws IOException {
	return
	    entry.getFile();
    }
    
    
    protected FSDirectory createDirectory ( FSEntry entry )
	throws IOException {
	return
	    entry.getDirectory();
    }

    
    protected FSEntry createRootEntry()
	throws IOException {
	return
	    new FatRootDirectory ( this );
    }


    public void flush()
	throws IOException {
	super.flush();
	fat.flush();
	log.debug ( getFat().getCacheStat() );
    }
    

    public String toString() {
	StrWriter out = new StrWriter();

	out.println ( "********************** FatFileSystem ************************" );
	out.println (  getFat() );
	out.print   ( "*************************************************************" );

	return out.toString();
    }
}
