package org.jnode.fs.service.def;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;

public class FileSystemManagerTest {

    private FileSystemManager fsm;
    private FileSystem<?> fs;
    private Device device;

    @Before
    public void setUp() throws Exception {
        fsm = new FileSystemManager();
        device = mock(Device.class);
        fs = mock(FileSystem.class);
        when(fs.getDevice()).thenReturn(device);
    }

    @Test
    public void testUnregisterFileSystem() throws Exception {
    	fsm.registerFileSystem(fs);
    	FileSystem<?> result = fsm.unregisterFileSystem(device);
        assertNotNull(result);
    }

    @Test
    public void testGetFileSystem() throws Exception {
    	fsm.registerFileSystem(fs);
    	FileSystem<?> result = fsm.getFileSystem(device);
    	assertNotNull(result);
    }

    @Test
    public void testFileSystems() throws Exception {
    	fsm.registerFileSystem(fs);
        Collection<FileSystem<?>> result = fsm.fileSystems();
        assertNotNull(result);
        assertEquals(1,result.size());
        
        fsm.unregisterFileSystem(device);
        
        result = fsm.fileSystems();
        assertNotNull(result);
        assertEquals(0,result.size());
    }
    
    
}
