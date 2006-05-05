/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSObject;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemFullException;
import org.jnode.util.NumberUtils;

/**
 * @author gvt
 *
 */
public class FatChain {
    private static final Logger log =
        Logger.getLogger ( FatChain.class );

    private final FatFileSystem  fs;
    private final Fat            fat;
    
    private Integer              head;
    private boolean              dirty;

    private boolean              dolog = false;
    
    
    public FatChain ( FatFileSystem fs, Integer startEntry ) {
	this.fs     = fs;
	this.fat    = fs.getFat();

	setStartCluster ( startEntry );

	this.dirty  = false;
    }


    private void mylog ( String msg ) {
	if ( dolog )
	    log.debug ( msg );
    }


    public FatFileSystem getFatFileSystem() {
	return fs;
    }


    public int getStartCluster() {
	return head;
    }


    private void setStartCluster ( int value ) {
	if ( ( value < 0 ) || ( value > fat.size() ) )
	    throw new IllegalArgumentException ( "illegal head: " + value );

	head  =  value;
	dirty =  true;
    }


    public boolean isDirty() {
	return dirty;
    }


    public void flush() {
	dirty = false;
    }
    

    private ChainIterator listIterator ( int index )
	throws IOException {
	return new ChainIterator ( index );
    }


    private int getEndCluster()
	throws IOException {
	Integer last = 0;
	/*
	 * not cheap: we have to follow the whole chain to get
	 *            the last cluster value
	 */
	for (
	     ChainIterator i = listIterator ( 0 );
	     i.hasNext();
	     last = i.next()
	     );
	
	return last;
    }
    

    public int size()
	throws IOException {
	int count = 0;
	/*
	 * not cheap: we have to follow the whole chain to know
	 *            the chain size
	 */
	for (
	     ChainIterator i = listIterator ( 0 );
	     i.hasNext();
	     i.next()
	     )
	    count++;
	
	return count;
    }


    private int allocateTail ( int n, int m, int offset, boolean zero )
	throws IOException {
	if ( n <= 0 )
	    throw new IllegalArgumentException ( "n<=0" );

	if ( m < 0 )
	    throw new IllegalArgumentException ( "m<0" );

	if ( offset < 0 )
	    throw new IllegalArgumentException ( "offset<0" );


	mylog ( "n[" + n + "] m[" + m + "] offset[" + offset + "]" );
	

	byte[] buf = new byte[fat.getClusterSize()];
	final int last;
	int i, found = 0, l = 0;
	int k = ( offset > 0 ) ? 2 : 1;
	
	for ( i = fat.getLastFree(); i < fat.size(); i++ ) {
	    if ( fat.isFreeEntry ( i ) ) {
		l = i;
		found++;
	    }
	    if ( found == n )
		break;
	}


	if ( found < n ) {
	    for ( i = fat.firstCluster(); i < fat.getLastFree(); i++ ) {
		if ( fat.isFreeEntry ( i ) )
		    found++;
		if ( found == n )
		    break;
	    }
	}


	if ( found < n )
	    throw new FileSystemFullException ( "no free clusters" );

	last = l;

	mylog ( "found[" + found + "] last[" + last + "]" );

	fat.set ( last, fat.eofChain() );
	mylog ( n + "\t|allo|\t" + last + " " + fat.eofChain() );

	if ( zero ) {
	    mylog ( n + "\t|ZERO|\t" + last + " " + fat.eofChain() );
	    fat.clearCluster ( last );
	}

	//
	found = 0; l = last; i = last;
	//
	for (; found < (n-m-k); i-- ) {
	    if ( fat.isFreeEntry ( i ) ) {
		fat.set ( i, l );
		mylog ( (n-found-1) + "\t|allo|\t" + i + " " + l );
		l = i;
		found++;
	    }
	}
	//
	if ( offset > 0 ) {
	    for (;; i-- ) {
		if ( fat.isFreeEntry ( i ) ) {
		    fat.clearCluster ( i, 0, offset );
		    fat.set ( i, l );
		    mylog ( (n-found-1) + "\t|part|\t" + i + " " + l );
		    l = i;
		    found++;
		    break;
		}

	    }
	}
	//
	for (; found < (n-1); i-- ) {
	    if ( fat.isFreeEntry ( i ) ) {
		fat.clearCluster ( i );
		fat.set ( i, l );
		mylog ( (n-found-1) + "\t|zero|\t" + i + " " + l );
		l = i;
		found++;
	    }
	}

	//
	fat.rewindFree();
	//
	for ( i = last; i < fat.size(); i++ ) {
	    if ( fat.isFreeEntry ( i ) ) {
		fat.setLastFree ( i );
		break;
	    }
	}

	mylog ( "LastFree: " + fat.getLastFree() );

	return l;
    }


    private int allocateTail ( int n, int m, int offset )
	throws IOException {
	return allocateTail ( n, m, offset, false );
    }
	

    
    private int allocateTail ( int n )
	throws IOException {
	return allocateTail ( n, 0, 0 );
    }

    
    private void allocate ( int n )
	throws IOException {
	int last  = allocateTail ( n );
	int first = getEndCluster();

	mylog ( first + ":" + last );

	if ( first != 0 )
	    fat.set ( first, last );
	else {
	    mylog ( "allocate chain" );
	    setStartCluster ( last );
	}
    }


    public void allocateAndClear ( int n )
	throws IOException {
	int last  = allocateTail ( n, n-1 ,0, true );
	int first = getEndCluster();

	mylog ( first + ":" + last );

	if ( first != 0 )
	    fat.set ( first, last );
	else {
	    mylog ( "allocate chain" );
	    setStartCluster ( last );
	}
    }
    

    public void free ( int n )
	throws IOException {
	if ( n <= 0 )
	    throw new IllegalArgumentException ( "n<=0" );

	int count = size();

	if ( count < n )
	    throw new IOException ( "not enough cluster: count[" +
				    count + "] n[" + n + "]" );

	mylog ( "count[" + count + "] n[" + n + "]" );

	ChainIterator i;

	if ( count > n ) {
	    i = listIterator ( count - n - 1 );
	    int l = i.next();
	    fat.set ( l, fat.eofChain() );
	    mylog ( l + ":" + fat.eofChain() );
	}
	else
	    i = listIterator ( 0 );
	
	while ( i.hasNext() ) {
	    int l = i.next();
	    fat.set ( l, fat.freeEntry() );
	    mylog ( l + ":" + fat.freeEntry() );
	}
	
	if ( count == n ) {
	    setStartCluster ( 0 );
	    mylog ( "zero" );
	}
    }


    /*
     * implemented separately for efficiency
     */
    public void freeAllClusters()
	throws IOException {

	ChainIterator i = listIterator ( 0 );

	while ( i.hasNext() ) {
	    int l = i.next();
	    fat.set ( l, fat.freeEntry() );
	}

	setStartCluster ( 0 );
    }

    
    public void read ( long offset, ByteBuffer dst )
	throws IOException {
	if ( offset < 0 )
	    throw new IllegalArgumentException ( "offset<0" );
	
	if ( dst.remaining() == 0 )
	    return;
	
	ChainIterator  i;
	ChainPosition  p = new ChainPosition ( offset );

	try {
	    i  = listIterator ( p.getIndex() );
	}
	catch ( IndexOutOfBoundsException ex ) {
	    final IOException ioe =
		new IOException ( "attempt to seek after End Of Chain " + offset );
	    ioe.initCause ( ex );
	    throw ioe;
	}

	
	for ( int l = dst.remaining(), sz = p.getPartial(), ofs = p.getOffset(), size;
	      l > 0;
	      l -= size, sz = p.getSize(), ofs = 0 ) {
	    
	    int cluster = i.next();
	    
	    size = Math.min ( sz, l );

	    mylog ( "read " + size + " bytes from cluster " + cluster +
		    " at offset " + ofs );

	    dst.limit ( dst.position() + size );

	    fat.readCluster ( cluster, ofs, dst );
	}
    }


    /*
     * length is used to zero the last cluster
     * allocated to a chain when this is required
     * i.e. from FatFile
     *
     * when there is no need to zero the cluster
     * at the end of the chain, last cluster,
     * we can use any clsize multiple or zero
     */
    public void write ( long length, long offset, ByteBuffer src )
	throws IOException {

	if ( length < 0 )
	    throw new IllegalArgumentException ( "length<0" );

	if ( offset < 0 )
	    throw new IllegalArgumentException ( "offset<0" );

	ChainPosition p = new ChainPosition ( offset );

	int last;
	int cluster = 0;
	int clsize  = p.getSize();
	int clidx   = p.getIndex();
	
	ChainIterator i = listIterator ( 0 );

	for ( last = 0; last < clidx; last++ )
	    if ( i.hasNext() )
		cluster = i.next();
	    else
		break;

	if ( last != clidx ) {
	    int m = clidx - last;

	    long lst = offset + src.remaining() - last * clsize;

	    int n = (int)( lst / clsize );
	    if ( ( lst % clsize ) != 0 )
		n++;

	    last = allocateTail ( n, m, p.getOffset() );

	    if ( cluster != 0 ) {
		fat.set ( cluster, last );
		((ChainIterator)i).setCursor ( last );
	    }
	    else {
		setStartCluster ( last );
		i = listIterator ( clidx );
	    }

	    /*
	     * here length is used to decide
	     * if we have to zero the data
	     * inside the last cluster tail
	     */
	    int ofs = (int)( length % clsize );
	    
	    if ( ofs != 0 )
		fat.clearCluster ( cluster, ofs, clsize );
	}


	for ( int l = src.remaining(), sz = p.getPartial(), ofs = p.getOffset(), size;
	      l > 0;
	      l -= size, sz = clsize, ofs = 0 ) {
	    
	    try {
		cluster =  i.next();
	    }
	    catch ( NoSuchElementException ex ) {
		int n = l / clsize;
		if ( ( l % clsize ) != 0 )
		    n++;
		
		last = allocateTail ( n );

		if ( cluster != 0 ) {
		    fat.set ( cluster, last );
		    ((ChainIterator)i).setCursor ( last );
		}
		else {
		    setStartCluster ( last );
		    i = listIterator ( 0 );
		}

		cluster = i.next();
	    }
	    
	    size = Math.min ( sz, l );

	    mylog ( "write " + size + " bytes to cluster " + cluster +
		    " at offset " + ofs );

	    src.limit ( src.position() + size );

	    fat.writeCluster ( cluster, ofs, src );
	}
    }


    /*
     * used when we don't need to zero the data
     * inside the last cluster tail
     */
    public void write ( long offset, ByteBuffer src )
	throws IOException {
	write ( 0, offset, src );
    }

    
    public long getLength()
	throws IOException {
	/*
	 * not cheap: we have to follow the whole chain to know
	 *            the chain length
	 */
	return 
	    size() * fat.getClusterSize();
    }

    
    public String toString() {
	StrWriter out = new StrWriter();

	boolean first = true;

	int prev = 0;
	int last = 0;

	try {
	    ChainIterator i = listIterator ( 0 );

	    out.print ( "[(Start:" + head + ",Size:" + size() + ") " );
	    
	    out.print ( "<" );
	    
	    while ( i.hasNext() ) {
		int curr = i.next();
		
		if ( first ) {
		    first = false;
		    out.print ( curr );
		    last = curr;
		}
		else if ( curr != prev + 1 ) {
		    if ( prev != last )
			out.print ( "-" + prev );
		    out.print ( "> <" + curr );
		    last = curr;
		}
		
		prev = curr;
	    }
	    
	    if ( prev != last )
		out.print ( "-" + prev );
	    
	    out.print ( ">]" );
	}
	catch ( IOException ex ) {
	    log.debug ( "error in chain" );
	    out.print ( "error in chain" );
	}

	return out.toString();
    }


    /*
     * dump a chain on a file: used for debugging and testing
     *                         inside the FatChain class
     *                         size() can and must be used
     */
    public void dump ( String fileName )
	throws IOException, FileNotFoundException {
	int size = size();
	FileOutputStream f = new FileOutputStream ( fileName );
	ByteBuffer buf = ByteBuffer.allocate ( fat.getClusterSize() );

	for ( int i = 0; i < size; i++ ) {
	    buf.clear();
	    read ( i * fat.getClusterSize(), buf );
	    buf.flip();
	    f.getChannel().write ( buf );
	}

	f.close();
    }

    
    /*
     * dump a chain cluster: used for debugging and testing
     *                       "inside" the FatChain class
     */
    public void dumpCluster ( String fileName, int index )
	throws IOException, FileNotFoundException {
	FileOutputStream f = new FileOutputStream ( fileName );
	ByteBuffer buf = ByteBuffer.allocate ( fat.getClusterSize() );

	buf.clear();
	read ( index * fat.getClusterSize(), buf );
	buf.flip();
	f.getChannel().write ( buf );

	f.close();
    }


    private class ChainPosition {
	private final long position;
	private final int  index;
	private final int  offset;
	private final int  size;

	
	private ChainPosition ( long pos ) {
	    if ( pos < 0L || pos > 0xFFFFFFFFL )
		throw new IllegalArgumentException();

	    this.position = pos;
	    this.size = fat.getClusterSize();

	    this.index  =  (int)( pos / size );
	    this.offset =  (int)( pos % size );
	}


	private final int getIndex() {
	    return index;
	}


	private final int getOffset() {
	    return offset;
	}


	private final int getSize() {
	    return size;
	}


	private final int getPartial() {
	    return ( size - offset );
	}
	
	
	private final long getPosition() {
	    return
		position;
	}
    }

    

    private class ChainIterator {
	private Integer cursor;
	private int     index;
	
	private ChainIterator ( int index )
	    throws IOException {
	    cursor = head;

	    if ( index < 0 )
		throw new IndexOutOfBoundsException
		    ( "negative index: " + index );

	    for ( int i = 0; i < index; i++ ) {
		if ( hasNext() )
		    next();
		else
		    throw new IndexOutOfBoundsException
			( "index overflow: " + index );
	    }

	    this.index = index;
	}


	private void setCursor ( Integer value ) {
	    cursor = value;
	}
	
	
	private boolean hasNext() {
	    return ( fat.hasNext ( cursor ) );
	}


	private Integer next()
	    throws IOException {
	    if ( !hasNext() )
		throw new NoSuchElementException();

	    Integer current = cursor;

	    cursor = fat.get ( cursor );

	    if ( cursor == current )
		throw new IOException ( "circular chain at: " + cursor );

	    if ( fat.isFree ( cursor )  )
		throw new IOException ( "free entry in chain at: " + current );

	    index++;
	    
	    return current;
	}


	private boolean hasPrevious() {
	    return !( cursor == head );
	}


	/*
	 * Take care: this method is implemented for the 
	 * sake of interface completness, but it is expensive ...
	 * this a "true forward only list" ... the previous element
	 * can be recovered only by a complete list scan
	 *
	 * ... peraphs an UnsupportedOperationException
	 * would be better here ... but who knows? ;-)
	 */
	private Integer previous()
	    throws IOException {
	    if ( !hasPrevious() )
		throw new NoSuchElementException();

	    int prev = index - 1;
	    
	    cursor = head;
	    index  = 0;

	    for ( int i = 0; i < prev; i++ )
		next();

	    return cursor;
	}


	private int nextIndex() {
	    return index;
	}


	private int previousIndex() {
	    return ( index - 1 );
	}
    }
}
