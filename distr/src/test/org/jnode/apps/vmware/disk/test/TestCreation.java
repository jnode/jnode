package org.jnode.apps.vmware.disk.test;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.junit.Test;


/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class TestCreation extends BaseTest
{
	private static final Logger LOG = Logger.getLogger(TestCreation.class);
	
	public TestCreation() throws IOException 
	{
		super(Utils.createTempFile("create"), false);
	}

	@Test
	public void createSparseDisk() throws Exception
	{
		
	}	 
}
