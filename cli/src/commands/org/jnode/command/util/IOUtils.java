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
package org.jnode.command.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Flushable;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Convenience IO methods.
 *
 * @author chris boertien
 */
public final class IOUtils {

    private static final int BUFFER_SIZE = 4096;
    
    private static final String ex_null_param = "A required paramater is null";
    
    /**
     * Call the close method of a list of Closeable objects.
     *
     * This will not throw a NullPointerException if any of the objects are null.
     * 
     * This is a convenience method that traps the exception from various
     * stream close() methods.
     *
     * @param objs one or more Closeable objects.
     */
    public static void close(Closeable... objs) {
        close(false, objs);
    }
    
    /**
     * Call the close method of a list of Closeable objects.
     *
     * If the flush paramater is set to true, and an object implements
     * the Flushable interface, then the flush method will be called before
     * the close method is called.
     *
     * This will not throw a NullPointerException if any of the objects are null.
     *
     * This will not throw an IOException if either the close or flush methods throw
     * an IOException.
     *
     * If calling flush causes an IOException, close will still be called.
     *
     * @param flush if true, check if the object is Flushable
     * @param objs one or more Closeable objects
     */
    public static void close(boolean flush, Closeable... objs) {
        for (Closeable obj : objs) {
            if (obj != null) {
                if (flush && (obj instanceof Flushable)) {
                    try {
                        ((Flushable) obj).flush();
                    } catch (IOException _) {
                        // ignore
                    }
                }
                try {
                    obj.close();
                } catch (IOException _) {
                    // ignore
                }
            }
        }
    }
    
    /**
     * Copies data from an Inputstream to an OutputStream.
     *
     * This method allocates a 4096 byte buffer each time it is called.
     *
     * At the end of writing, the OutputStream will be flushed, but no streams will be closed.
     *
     * @param in the stream to read from
     * @param out the stream to write to
     * @param bufferSize the size of buffer to use for the copy
     * @return the number of bytes read from the input stream
     * @throws NullPointerException if either in or out are null
     * @throws IOException if an I/O error occurs
     */
    public static long copyStream(InputStream in, OutputStream out) throws IOException {
        return copyStream(in, out, new byte[BUFFER_SIZE]);
    }
    
    /**
     * Copies data from an Inputstream to an OutputStream.
     *
     * This method allocates a buffer of 'bufferSize' when it is called.
     *
     * At the end of writing, the OutputStream will be flushed, but no streams will be closed.
     *
     * @param in the stream to read from
     * @param out the stream to write to
     * @param bufferSize the size of buffer to use for the copy
     * @return the number of bytes read from the input stream
     * @throws NullPointerException if either in or out are null
     * @throws IOException if an I/O error occurs
     */
    public static long copyStream(InputStream in, OutputStream out, int bufferSize) throws IOException {
        return copyStream(in, out, new byte[bufferSize]);
    }
    
    /**
     * Copies data from an InputStream to an OutputStream
     *
     * If copying multiple streams, this method may be prefered to the others
     * as a way to use the same buffer instead of allocating a new buffer on each call.
     *
     * At the end of writing, the OutputStream will be flushed, but no streams will be closed.
     *
     * @param in the stream to read from
     * @param out the stream to write to
     * @param buffer a pre-allocated buffer to use.
     * @return the number of bytes read from the input stream
     * @throws NullPointerException if either in, out or buffer are null
     * @throws IOException if an I/O error occurs
     */
    public static long copyStream(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        checkNull(in, out, buffer);
        
        long totalBytes = 0;
        int len = 0;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
            totalBytes += len;
        }
        out.flush();
        return totalBytes;
    }
    
    /**
     * Opens an InputStream on a file for reading.
     *
     * This method will not throw a FileNotFoundException or SecurityException like
     * the FileInputStream constructor would.
     *
     * @param file the file to open a stream on
     * @return an InputStream on the file, or null if an exception was thrown
     * @throws NullPointerException if file is null
     */
    public static InputStream openInputStream(File file) {
        return openInputStream(file, false);
    }
    
    /**
     * Opens an InputStream on a file for reading.
     *
     * This method will not throw a FileNotFoundException or a SecurityException like
     * the FileInputStream constructor would.
     *
     * @param file the file to open a stream on
     * @param buffer if true, wrap the stream in a buffered stream
     * @return an InpustStream on the file, or null if an exception was thrown
     * @throws NullPointerException if file is null
     */
    public static InputStream openInputStream(File file, boolean buffer) {
        return openInputStream(file, buffer, BUFFER_SIZE);
    }
    
    /**
     * Opens an InputStream on a file for reading.
     * 
     * This method will not throw a FileNotFoundException or a SecurityException like
     * the FileInputStream constructor would.
     *
     * @param file the file to open a stream on
     * @param buffer if true, wrap the stream in a buffered stream
     * @param bufferSize the buffer size to use if buffer is true
     * @return an InpustStream on the file, or null if an exception was thrown
     * @throws NullPointerException if file is null
     * @throws IllegalArgumentException if bufferSize < 0
     */
    public static InputStream openInputStream(File file, boolean buffer, int bufferSize) {
        checkNull(file);
        
        try {
            InputStream in = new FileInputStream(file);
            if (buffer) {
                in = new BufferedInputStream(in, bufferSize);
            }
            return in;
        } catch (FileNotFoundException e) {
            return null;
        } catch (SecurityException e2) {
            return null;
        }
    }
    
    /**
     * Opens a Reader on a file.
     * 
     * This method will not throw a FileNotFoundException like the FileReader
     * constructor would.
     *
     * @param file the file to open the reader on
     * @return the reader, or null if the file could not be opened
     * @throws NullPointerException if file is null
     */
    public static Reader openReader(File file) {
        checkNull(file);
        
        try {
            return new FileReader(file);
        } catch (FileNotFoundException e) {
            // fall through
        }
        
        return null;
    }
    
    /**
     * Opens a BufferedReader on a file.
     * 
     * This method will not throw a FileNotFoundException like the FileReader
     * constructor would.
     *
     * @param file the file to open the reader on
     * @param bufferSize the buffer size to use for this reader
     * @return the reader, or null if the file could not be opened
     * @throws NullPointerException if file is null
     */
    public static BufferedReader openBufferedReader(File file, int bufferSize) {
        Reader reader = openReader(file);
        if (reader != null) {
            return new BufferedReader(reader, bufferSize);
        }
        return null;
    }
    
    /**
     * Opens a LineNumberReader on a file.
     *
     * This method will not throw a FileNotFoundException like the FileReader
     * constructor would.
     *
     * @param file the file to open the reader on
     * @param bufferSize the buffer size to use for this reader
     * @return the reader, or null if the file could not be opened
     * @throws NullPointerException if file is null
     */
    public static LineNumberReader openLineReader(File file, int bufferSize) {
        Reader reader = openReader(file);
        if (reader != null) {
            return new LineNumberReader(reader, bufferSize);
        }
        return null;
    }
    
    /**
     * Opens an OutputStream on a file for writing.
     *
     * If the file exists and has content, it will be overwritten. That is to say the append
     * paramater will be false.
     * 
     * This method will not throw a FileNotFoundException or a SecurityException like
     * the FileOutputStream constructor would.
     *
     * @param file the file to open a stream on
     * @return an OutputStream on the file, or null if an exception was thrown
     * @throws NullPointerException if file is null
     */
    public static OutputStream openOutputStream(File file) {
        return openOutputStream(file, false, 0);
    }
    
    /**
     * Opens an OutputStream on a file for writing.
     *
     * This method will not throw a FileNotFoundException or a SecurityException like
     * the FileOutputStream constructor would.
     *
     * @param file the file to open a stream on
     * @param append if true, open the stream in append mode
     * @return an OutputStream on the file, or null if an exception was thrown
     * @throws NullPointerException if file is null
     */
    public static OutputStream openOutputstream(File file, boolean append) {
        return openOutputStream(file, append, 0);
    }
    
    /**
     * Opens an OutputStream on a file for writing.
     *
     * The stream will be wrapped in a buffered stream if bufferSize is &gt; 0.
     *
     * If the file exists and has content, it will be overwritten. That is to say the append
     * paramater will be false.
     * 
     * This method will not throw a FileNotFoundException or a SecurityException like
     * the FileOutputStream constructor would.
     *
     * @param file the file to open a stream on
     * @param bufferSize if this is &gt; 0, use it as the buffer size for the stream
     * @return an OutputStream on the file, or null if an exception was thrown
     * @throws NullPointerException if file is null
     * @throws IllegalArgumentException if bufferSize &lt; 0
     */
    public static OutputStream openOutputStream(File file, int bufferSize) {
        return openOutputStream(file, false, bufferSize);
    }
    
    /**
     * Opens an OutputStream on a file for writing.
     *
     * The stream will be wrapped in a buffered stream if bufferSize is &gt; 0.
     * 
     * This method will not throw a FileNotFoundException or a SecurityException like
     * the FileOutputStream constructor would.
     *
     * @param file the file to open a stream on
     * @param bufferSize if this is &gt; 0, use it as the buffer size for the stream
     * @param append if true, open the stream in append mode
     * @return an OutputStream on the file, or null if an exception was thrown
     * @throws NullPointerException if file is null
     * @throws IllegalArgumentException if bufferSize &lt; 0
     */
    public static OutputStream openOutputStream(File file, boolean append, int bufferSize) {
        checkNull(file);
        
        try {
            OutputStream out = new FileOutputStream(file, append);
            
            if (bufferSize > 0) {
                out = new BufferedOutputStream(out, bufferSize);
            }
            return out;
        } catch (FileNotFoundException e) {
            return null;
        } catch (SecurityException e2) {
            return null;
        }
    }
    
    /**
     * Prompt the user with a question, asking for a yes or no response.
     *
     * The default response if none is given will be 'yes'
     *
     * @param in the reader to read user input from.
     * @param out the writer to send the prompt to the user
     * @param str the prompt to send to the user
     * @return true if the user said yes, false if no
     */
    public static boolean promptYesOrNo(Reader in, PrintWriter out, String prompt) {
        return promptYesOrNo(in, out, prompt, true);
    }
    
    /**
     * Prompt the user with a question, asking for a yes or no response.
     *
     * If out is not attached to a terminal, then the user will not see the prompt
     * 
     * If in is not attached to a terminal, then this method has undefined behavior.
     * 
     * If the user inputs an answer that does not being with an 'n', 'N', 'y' or 'Y'
     * then it will prompt the user again. If something causes this loop to execute
     * indefinetly, it has limited loop iterations. If this loop cap is reached, the
     * method will return false.
     *
     * If the user inputs nothing, then the defaultChoice is used.
     *
     * If the Reader is not a BufferedReader, then it will be wrapped in one.
     *
     * @param in the reader to read user input from.
     * @param out the writer to send the prompt to the user
     * @param str the prompt to send to the user
     * @param defaultChoice if the user inputs no reply, this value is returned
     * @return true if the user said yes, false if no
     * @throws NullPointerException if in, out or str are null
     */
    public static boolean promptYesOrNo(Reader in, PrintWriter out, String prompt, boolean defaultChoice) {
        String input;
        
        // put a cap on the loops so it doesn't become an infinite loop
        // this can happen if Reader is not attached to a tty
        for (int i = 0; i < 10; i++) {
            input = prompt(in, out, prompt);
            
            if (input == null) {
                return false;
            }
            
            if (input.length() == 0) {
                return defaultChoice;
            }
            
            switch(input.charAt(0)) {
                case 'y' :
                case 'Y' :
                    return true;
                case 'n' :
                case 'N' :
                    return false;
            }
        }
        
        return false;
    }
    
    /**
     * Prompt the user with a question and capture the response.
     *
     * If out is not attached to a terminal, then the user will not see the prompt
     * 
     * If in is not attached to a terminal, then this method has undefined behavior.
     * 
     * If the Reader is not a BufferedReader, then it will be wrapped in one.
     *
     * @param in the reader to read user input from.
     * @param out the writer to send the prompt to the user
     * @param str the prompt to send to the user
     * @return the string captured from the user, or null if an I/O error occurred.
     * @throws NullPointerException if in, out or str are null
     */
    public static String prompt(Reader in, PrintWriter out, String prompt) {
        checkNull(in, out, prompt);
        
        String input;
        BufferedReader reader;
        
        if (in instanceof BufferedReader) {
            reader = (BufferedReader) in;
        } else {
            reader = new BufferedReader(in);
        }
        
        out.print(prompt);
        try {
            input = reader.readLine();
        } catch (IOException e) {
            return null;
        }
        
        return input;
    }
    
    /**
     * Check for null objects in a list of objects.
     */
    private static void checkNull(Object... objs) {
        for (Object obj : objs) {
            if (obj == null) throw new NullPointerException(ex_null_param);
        }
    }
}
