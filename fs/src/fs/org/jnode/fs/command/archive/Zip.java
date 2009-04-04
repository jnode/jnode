/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

package org.jnode.fs.command.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;

public class Zip extends ArchiveCommand {

    private static final int ZIP_DELETE = 1;
    private static final int ZIP_REFRESH = 2;
    private static final int ZIP_MOVE = 3;
    private static final int ZIP_ADD = 4;
    private static final int ZIP_UPDATE = 5;
    private static final int ZIP_EXTRACT = 6;
    
    private File archive;
    
    private int mode;
    
    public Zip(String s) {
        super(s);
    }
    
    public void execute(String command) {
        super.execute(command);
    }
    
    void extract() throws IOException {
        
    }
}
