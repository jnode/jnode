/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.jfat.BootSector;
import org.jnode.fs.FileSystemFullException;
import org.jnode.fs.FileSystemException;
import org.jnode.util.LittleEndian;
import org.jnode.util.NumberUtils;


/**
 * @author gvt
 */
public abstract class Fat {
    private static final Logger log =
        Logger.getLogger ( Fat.class );

    private final BlockDeviceAPI api;
    private final BootSector     bs;

    private final ByteBuffer     buf;
    private       long           lastread;
    private       int            lastfree;

    private final byte[]         clearbuf;

     
    protected Fat ( BootSector bs, BlockDeviceAPI api ) {
	this.bs      =  bs;
	this.api     =  api;

	/*
	 * must be one byte longer of the sector size
	 * to accomodate the last byte of a FAT12 cluster entry
	 */
	buf = ByteBuffer.allocate ( bs.getBytesPerSector() + 1 );

	/*
	 * can and must be initialized to a negative long
	 *     by contract fat positions cannot be negative longs
	 */
	lastread = -1;

	/*
	 * set lastfree
	 */
	rewindFree();

	/*
	 * and blank the clear buffer
	 */
	clearbuf = new byte[getClusterSize()];
	Arrays.fill ( clearbuf, 0, clearbuf.length, (byte)0x00 );
    }
    

    public static Fat create ( BlockDeviceAPI api )
	throws IOException, FileSystemException {
	BootSector bs = new BootSector ( 512 );
	
	bs.read ( api );

	if ( bs.isFat32() )
	    return new Fat32 ( bs, api );
	else if ( bs.isFat16() )
	    return new Fat16 ( bs, api );
	else if ( bs.isFat12() )
	    return new Fat12 ( bs, api );

	throw new FileSystemException ( "FAT not recognized" );
    }


    public final BootSector getBootSector() {
	return bs;
    }
    

    public final BlockDeviceAPI getApi() {
	return api;
    }
    

    public final int getClusterSize() {
	return
	    getBootSector().getBytesPerSector() *
	    getBootSector().getSectorsPerCluster();
    }


    public final long getFirstSector ( int fatnum ) {
	if ( fatnum < 0 || fatnum >= getBootSector().getNrFats() )
	    throw new
		IndexOutOfBoundsException ( "illegal fat: " + fatnum );
	return
	    (long)getBootSector().getNrReservedSectors() +
	    getBootSector().getSectorsPerFat() * (long)fatnum;
    }


    public final boolean isFirstSector ( int fatnum, long sector ) {
	return ( sector == getFirstSector ( fatnum ) );
    }

    
    public final long getLastSector ( int fatnum ) {
	return
	    getFirstSector ( fatnum ) +
	    getBootSector().getSectorsPerFat() - 1;
    }


    public final boolean isLastSector ( int fatnum, long sector ) {
	return ( sector == getLastSector ( fatnum ) );
    }


    public final long getFirst ( int fatnum ) {
	return
	    getFirstSector ( fatnum ) *
	    (long)getBootSector().getBytesPerSector();
    }


    public final long getLast ( int fatnum ) {
	return
	    getLast ( fatnum ) + offset ( size() - 1 );
    }


    private final long position ( int fatnum, int index )
	throws IOException {
	if ( index < 0 || index >= size() )
	    throw new IllegalArgumentException ( "illegal entry: " + index );
	return
	    getFirst ( fatnum ) + offset ( index );
    }


    /*
     * it is "optimized" for "sequential" accesses to the FAT
     * random accesses requires to reread the FAT
     *
     * BytesPerSector "must be" a multiple of 4
     *   acceptable values are 512, 1024, 2048, 4096
     *     they should be checked in the BootSector class
     *
     * have to be revised anyway
     *
     * the logic is to read the whole sector that contains
     * a FAT entry into the buffer
     *
     * in the FAT12 case it reads the whole sector and one
     * byte from the "next sector"
     *
     * (gvt) I know is possible todo a better job here
     *       but it should be quite easy to change
     *       just the read/write core routines
     */
    protected byte[] readEntry ( int index, int length )
	throws IOException {
	byte[] value = new byte[length];

	long pos  =  position ( 0, index );
	int  sz   =  getBootSector().getBytesPerSector();

	long bufidx;
	int  bufofs;

	bufidx = pos / sz;
	bufofs = (int)pos % sz;

	
	if ( bufidx != lastread ) {
	    buf.clear();
	    if ( getBootSector().isFat12() && !isLastSector ( 0, bufidx ) )
		/*
		 * accomodate the "one" byte remaining
		 * if it is a FAT12 and it is not the last
		 * sector of the current FAT
		 */
		buf.limit ( sz + 1 );
	    else
		/*
		 * otherwise just read the whole sector
		 */
		buf.limit ( sz );
	    getApi().read ( bufidx * sz, buf );
	}
	
	if ( buf.position() != bufofs )
	    buf.position ( bufofs );

	buf.get ( value );
	
	lastread = bufidx;
		
	return value;
    }
    

    protected void writeEntry ( int index, byte[] value )
	throws IOException {
	/*
	 * invalidate the cache ;-)
	 *   take care: it has to be changed as the read logic
	 *              change
	 */
	lastread = 0;
	
	for ( int i = 0; i < getBootSector().getNrFats(); i++ )
	    getApi().write ( position ( i, index ), ByteBuffer.wrap ( value ) );
    }


    public void readCluster ( int cluster, int offset, ByteBuffer dst )
	throws IOException {
	if ( offset < 0 )
	    throw new IllegalArgumentException ( "offset<0" );

	if ( ( offset + dst.remaining() ) > getClusterSize() )
	    throw new
		IllegalArgumentException ( "length[" +
					   ( offset + dst.remaining() ) + "] " +
					   "exceed clusterSize[" + getClusterSize() +
					   "]" );

	getApi().read ( getClusterPosition ( cluster ) + offset, dst );
    }


    public void writeCluster ( int cluster, int offset, ByteBuffer src )
	throws IOException {
	if ( offset < 0 )
	    throw new IllegalArgumentException ( "offset<0" );
	
	if ( ( offset + src.remaining() ) > getClusterSize() )
	    throw new
		IllegalArgumentException ( "length[" +
					   ( offset + src.remaining() ) + "] " +
					   "exceed clusterSize[" + getClusterSize() +
					   "]" );

	getApi().write ( getClusterPosition ( cluster ) + offset, src );
    }
    

    public void clearCluster ( int cluster, int start, int end )
	throws IOException {
	if ( start < 0 )
	    throw new IllegalArgumentException ( "start<0" );
	
	if ( end < start )
	    throw new IllegalArgumentException ( "end<start " + start + " " + end );
	
	if ( end > getClusterSize() )
	    throw new
		IllegalArgumentException ( "end[" + end + "] " +
					   "exceed clusterSize[" +
					   getClusterSize() + "]" );

	ByteBuffer clear = ByteBuffer.wrap ( clearbuf );
	clear.limit ( end - start );

	writeCluster ( cluster, start, clear );
    }


    public void clearCluster ( int cluster )
	throws IOException {
	clearCluster ( cluster, 0, getClusterSize() );
    }

    
    public final int firstCluster() {
	return 2;
    }

    
    public final long getClusterSector ( int index ) {
	if ( index < firstCluster() || index >= size() )
	    throw new IllegalArgumentException ( "illegal cluster # : " + index );
	
	return
	    (long)( index - firstCluster()) * (long)bs.getSectorsPerCluster() +
	    getBootSector().getFirstDataSector();
    }


    public final long getClusterPosition ( int index ) {
	return
	    getClusterSector ( index ) * (long)bs.getBytesPerSector();
    }


    public final int size() {
	return (int)(bs.getCountOfClusters() + firstCluster());
    }


    protected abstract long offset ( int index );
    
    
    public abstract boolean isEofChain ( Integer entry );

    
    public abstract int eofChain();


    public boolean hasNext ( Integer entry ) {
	/*
	 * cluster 0(zero) and 1(one) are EndOfChains!
	 */
	if ( ( entry == 0 ) || ( entry == 1 ) )
	    return false;
	return !isEofChain ( entry );
    }


    public final int freeEntry() {
	return 0;
    }


    public final boolean isFree ( Integer entry ) {
	return ( entry == freeEntry() );
    }


    public abstract Integer get ( int index )
	throws IOException;


    public abstract Integer set ( int index, Integer element )
	throws IOException;


    public final boolean isFreeEntry ( Integer entry )
	throws IOException {
	return isFree ( get ( entry ) );
    }


    public final int getLastFree() {
	return lastfree;
    }


    public final void setLastFree ( int value ) {
	lastfree = value;
    }


    public final void rewindFree() {
	lastfree = firstCluster();
    }
    
    
    public final int freeEntries()
	throws IOException {
	int count = 0;
	
	for ( int i = 0; i < size(); i++ )
	    if ( isFreeEntry (  i ) )
		count++;
	
	return count;
    }


    public final boolean isFat32() {
	return getBootSector().isFat32();
    }


    public final boolean isFat16() {
	return getBootSector().isFat16();
    }


    public final boolean isFat12() {
	return getBootSector().isFat12();
    }

    
    public String toString() {
	StrWriter out = new StrWriter();
	
	out.println ( "***************************  Fat   **************************" );
	out.println (  getBootSector() );
	out.println ( "ClusterSize\t"  + getClusterSize() );
	out.println ( "Size\t\t"       + size() );
	out.print   ( "FirstSector" );
	for ( int i = 0; i < getBootSector().getNrFats(); i++ )
	    out.print ( "\t" + getFirstSector ( i ) );
	out.println();
	out.print   ( "LastSector" );
	for ( int i = 0; i < getBootSector().getNrFats(); i++ )
	    out.print ( "\t" + getLastSector ( i ) );
	out.println();
	//out.println ( "FreeEntries\t" + freeEntries() );
	out.print   ( "*************************************************************" );
	return out.toString();
    }
}
