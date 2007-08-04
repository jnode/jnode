/**
 * 
 */
package org.jnode.apps.vmware.disk.handler.simple;

import java.io.IOException;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.IOHandler;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class SimpleIOHandler extends IOHandler {
	public SimpleIOHandler(FileDescriptor fileDescriptor) throws IOException, UnsupportedFormatException {		
		super(fileDescriptor.getDescriptor());
	}
}