/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package java.lang;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 */
class JNodeProcess extends Process {

    static Process start(String[] cmdarray, java.util.Map<String, String> environment, String dir,
                                boolean redirectErrorStream) throws IOException {
        
        System.out.println("cmdarray: " + Arrays.asList(cmdarray));
        System.out.println("environment: " + environment);
        System.out.println("dir: " + dir);
        System.out.println("redirectErrorStream: " + redirectErrorStream);

        return new JNodeProcess();
    }
    
    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public InputStream getErrorStream() {
        return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public int exitValue() {
        return 0;
    }

    @Override
    public void destroy() {

    }
}
