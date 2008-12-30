/*
 * $Id: CommandShell.java 4556 2008-09-13 08:02:20Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
package org.jnode.util;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;

import org.apache.log4j.Logger;

/**
 * Common utility code for higher-level operations on IO streams.  Notwithstanding the
 * nominal access for the class and its methods, user (command) code should avoid using
 * this class directly.  You should program against the 
 * {@link org.jnode.shell.io.CommandIO} API instead.
 * 
 * @author crawley@jnode.org
 */
public class IOUtils {
    
    private IOUtils() {
        // Prevent instantiation
    }
    
    public static boolean isTTY(Closeable stream) {
        if (stream instanceof ConsoleStream) {
            return true;
        } else if (stream instanceof ProxyStream<?>) {
            try {
                return isTTY(((ProxyStream<?>) stream).getRealStream());
            } catch (ProxyStreamException ex) {
                return false;
            }
        } else if (stream instanceof OutputStreamWriter) {
            return isTTY(findOutputStream((OutputStreamWriter) stream));
        } else if (stream instanceof InputStreamReader) {
            return isTTY(findInputStream((InputStreamReader) stream));
        } else if (stream instanceof ReaderInputStream) {
            return isTTY(((ReaderInputStream) stream).getReader());
        } else if (stream instanceof WriterOutputStream) {
            return isTTY(((WriterOutputStream) stream).getWriter());
        } else if (stream instanceof FilterInputStream) {
            return isTTY(findInputStream((FilterInputStream) stream));
        } else if (stream instanceof FilterOutputStream) {
            return isTTY(findOutputStream((FilterOutputStream) stream));
        } else {
            return false;
        }
    }
    
    public static Closeable findBaseStream(Closeable stream) {
        if (stream instanceof ConsoleStream) {
            return stream;
        } else if (stream instanceof ProxyStream<?>) {
            try {
                return findBaseStream(((ProxyStream<?>) stream).getRealStream());
            } catch (ProxyStreamException ex) {
                return null;
            }
        } else if (stream instanceof OutputStreamWriter) {
            return findBaseStream(findOutputStream((OutputStreamWriter) stream));
        } else if (stream instanceof InputStreamReader) {
            return findBaseStream(findInputStream((InputStreamReader) stream));
        } else if (stream instanceof ReaderInputStream) {
            return findBaseStream(((ReaderInputStream) stream).getReader());
        } else if (stream instanceof WriterOutputStream) {
            return findBaseStream(((WriterOutputStream) stream).getWriter());
        } else if (stream instanceof FilterInputStream) {
            return findBaseStream(findInputStream((FilterInputStream) stream));
        } else if (stream instanceof FilterOutputStream) {
            return findBaseStream(findOutputStream((FilterOutputStream) stream));
        } else {
            return stream;
        }
    }
    
    private static InputStream findInputStream(final FilterInputStream inputStream) {
        PrivilegedAction<InputStream> pa = new PrivilegedAction<InputStream>() {
            public InputStream run() {
                try {
                    Class<FilterInputStream> cls = FilterInputStream.class;
                    Field field = cls.getDeclaredField("in");
                    field.setAccessible(true);
                    Object in = field.get(inputStream);
                    field.setAccessible(false);
                    return (InputStream) in;
                } catch (Exception ex) {
                    Logger.getLogger(IOUtils.class).error("Cannot extract the 'in' field", ex);
                    return null;
                }
            }
        };
        return pa.run();
    }
    
    private static OutputStream findOutputStream(final FilterOutputStream outputStream) {
        PrivilegedAction<OutputStream> pa = new PrivilegedAction<OutputStream>() {
            public OutputStream run() {
                try {
                    Class<FilterOutputStream> cls = FilterOutputStream.class;
                    Field field = cls.getDeclaredField("out");
                    Object out = field.get(outputStream);
                    return (OutputStream) out;
                } catch (Exception ex) {
                    Logger.getLogger(IOUtils.class).error("Cannot extract the 'out' field", ex);
                    return null;
                }
            }
        };
        return pa.run();
    }

    
    private static OutputStream findOutputStream(final OutputStreamWriter writer) {
        // This implementation is based on the knowledge that an OutputStreamWriter
        // uses the underlying OutputStream as its 'lock' object.
        PrivilegedAction<OutputStream> pa = new PrivilegedAction<OutputStream>() {
            public OutputStream run() {
                try {
                    Class<Writer> cls = Writer.class;
                    Field field = cls.getDeclaredField("lock");
                    Object lock = field.get(writer);
                    return (OutputStream) lock;
                } catch (Exception ex) {
                    Logger.getLogger(IOUtils.class).error("Cannot extract the 'lock' field", ex);
                    return null;
                }
            }
        };
        return pa.run();
    }

    private static InputStream findInputStream(final InputStreamReader reader) {
        // This implementation is based on the knowledge that an InputStreamReader
        // uses the underlying InputStream as its 'lock' object.
        PrivilegedAction<InputStream> pa = new PrivilegedAction<InputStream>() {
            public InputStream run() {
                try {
                    Class<Reader> cls = Reader.class;
                    Field field = cls.getDeclaredField("lock");
                    Object lock = field.get(reader);
                    return (InputStream) lock;
                } catch (Exception ex) {
                    Logger.getLogger(IOUtils.class).error("Cannot extract the 'lock' field", ex);
                    return null;
                }
            }
        };
        return pa.run();
    }

}
