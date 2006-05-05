/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSFile;


public class FatFile extends FatEntry implements FSFile {
    private static final Logger log =
        Logger.getLogger ( FatFile.class );

    
    public FatFile ( FatFileSystem fs, FatDirectory parent, FatRecord record ) {
	super ( fs, parent, record );
    }
    

    public boolean isFile() {
	return true;
    }


    public FSFile getFile() {
	return this;
    }
    

    public long getLength() {
	return getEntry().getLength();
    }


    private void freeClusters ( long oldLength, long newLength )
	throws IOException {
	if ( newLength >= oldLength )
	    throw new UnsupportedOperationException
		( "new[" + newLength + "] >= old[" + oldLength + "]" );

	long clusterSize = getFatFileSystem().getFat().getClusterSize();

	int oldClusters = (int) ( oldLength / clusterSize );
	if ( ( oldLength % clusterSize ) != 0 )
	    oldClusters++;

	int newClusters = (int) ( newLength / clusterSize );
	if ( ( newLength % clusterSize ) != 0 )
	    newClusters++;

	getChain().free ( oldClusters - newClusters );
    }

    
    public void setLength ( long length )
 	throws IOException {
	long l = getLength();

	if ( length == l )
	    return;

	if ( length > l ) {
	    seek ( length );
	}
	else {
	    freeClusters ( l, length );
	    getEntry().setLength ( length );
	    flush();
	}
    }


    public void read ( long offset, ByteBuffer dst )
	throws IOException {
	try {
	    getChain().read ( offset, dst );
	}
	catch ( NoSuchElementException ex ) {
	    log.debug ( "End Of Chain reached: shouldn't happen" );
	}
    }


    public void seek ( long offset )
	throws IOException {
	ByteBuffer buf = ByteBuffer.allocate ( 0 );
	write ( offset, buf );
    }

    
    public void write ( long offset, ByteBuffer src )
	throws IOException {
	if ( offset < 0 )
	    throw new IllegalArgumentException ( "offset<0" );

	long lst = offset + src.remaining();
	long length = getLength();

	FatChain chain = getChain();
	
	chain.write ( length, offset, src );

	if ( lst > length )
	    getEntry().setLength ( lst );

	if ( lst != offset )
	    setLastModified ( System.currentTimeMillis() );

	flush();
    }


    public String toString() {
	StrWriter out = new StrWriter();
	
	out.println ( "*******************************************"             );
	out.println ( "FatFile"                                                 );
	out.println ( "*******************************************"             );
	out.println ( "Index\t\t"       +  getIndex()                           );
	out.println ( toStringValue()                                           );
	out.println ( "Length\t\t"      +  getLength()                          );
	out.print   ( "*******************************************"             );

	return out.toString();
    }
}
