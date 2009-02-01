/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * @author Levente S\u00e1ntha
 */
public class CopyFile implements ProgressAware {
    private ProgressSupport progress = new ProgressSupport();
    private boolean progessAware;
    private File source;
    private File destination;

    public CopyFile(boolean progessAware) {
        this.progessAware = progessAware;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public void setDestination(File destination) {
        this.destination = destination;
    }

    public void execute() throws Exception {
        try {
            if (source == null || destination == null)
                throw new RuntimeException("Source or destination is null.");

            long length = source.length();
            byte[] buf = new byte[128 * 1024];
            FileInputStream fis = new FileInputStream(source);
            FileOutputStream fos = new FileOutputStream(destination);
            int count;
            long status = 0;
            while ((count = fis.read(buf)) > -1) {
                fos.write(buf, 0, count);
                if (progessAware) {
                    status += count;
                    int val = (int) (100L * status / length);
                    progress.fireProgressEvent(new ProgressEvent(val));
                }
            }
            fis.close();
            fos.flush();
            fos.close();
        } catch (FileNotFoundException x) {
            x.printStackTrace();
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public void addProgressListener(ProgressListener p) {
        progress.addProgressListener(p);
    }
}
