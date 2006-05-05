/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.util.LittleEndian;


/**
 * @author gvt
 */
public class Fat32 extends Fat {
    protected Fat32 ( BootSector bs, BlockDeviceAPI api ) {
	super ( bs, api );
    }

    
    protected long offset ( int index ) {
	return (long)( 4 * index );
    }


    public Integer get ( int index )
	throws IOException {
	return
	    (int)( LittleEndian.getUInt32 ( readEntry ( index, 4 ), 0 ) & 0x0FFFFFFF );
    }


    public Integer set ( int index, Integer element )
	throws IOException {
	byte [] value = new byte[4];
	
	long old = LittleEndian.getUInt32 ( readEntry ( index, 4 ), 0 );

	LittleEndian.setInt32
	    ( value, 0, (int)( ( element & 0x0FFFFFFF ) | ( old & 0xF0000000 ) ) );
	
	writeEntry ( index, value );

	return (int)( old & 0x0FFFFFFF );
    }


    public boolean isEofChain ( Integer entry ) {
	return ( entry >= 0x0FFFFFF8 );
    }


    public int eofChain() {
	return 0x0FFFFFFF;
    }
}
