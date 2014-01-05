/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.fs.service.def;

import java.util.Collection;
import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        assertEquals(1, result.size());

        fsm.unregisterFileSystem(device);

        result = fsm.fileSystems();
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
