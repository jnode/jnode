/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.fs.FSEntry;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSFile;
import org.jnode.fs.FSAccessRights;


public class FatRootDirectory extends FatDirectory {
    /*
     * for root directory
     */
    public FatRootDirectory ( FatFileSystem fs )
	throws IOException {
	super ( fs );
	
	if       ( getFatFileSystem().getFat().isFat32() )
	    setRoot32( (int) getFatFileSystem().getBootSector().getRootDirectoryStartCluster() );
	else if  ( getFatFileSystem().getFat().isFat16() )
	    throw new UnsupportedOperationException ( "Fat16" );
	else if  ( getFatFileSystem().getFat().isFat12() )
	    throw new UnsupportedOperationException ( "Fat12" );
	else
	    throw new UnsupportedOperationException ( "Unknown Fat Type" );

	//getChain().dump ( "root" );

	scanDirectory();
    }


    public String getShortName() {
	return getName();
    }
    

    public boolean isDirty() {
	return false;
    }


    public int getIndex() {
	throw new UnsupportedOperationException ( "Root has not an index" );
    }

    
    public boolean isRoot() {
	return true;
    }


    public void setName ( String newName )
	throws IOException {
	throw new UnsupportedOperationException ( "cannot change root name" );
    }


    public String getLabel() {
	FatShortDirEntry label = getEntry();

	if ( label != null )
	    return label.getLabel();
	else
	    return "";
    }

    
    public long getLastModified()
	throws IOException {
	FatShortDirEntry label = getEntry();
	
	if ( label != null )
	    return label.getLastModified();
	else
	    return FatUtils.getMinDateTime();
    }


    public void setLastModified ( long lastModified )
	throws IOException {
	throw new UnsupportedOperationException ( "cannot change root time" );
    }


    public String toString() {
	StrWriter out = new StrWriter();

	out.println ( "*******************************************"             );
	out.println ( "FatRootDirectory"                                        );
	out.println ( "*******************************************"             );
	out.println ( toStringValue()                                           );
	out.println ( "Visited\t\t"     +  getVisitedChildren()                 );
	out.print   ( "*******************************************"             );

	return out.toString();
    }
}
