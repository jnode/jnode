/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2007 JNode.org
 */

/* PrintStream.java -- OutputStream for printing output
 Copyright (C) 1998, 1999, 2001, 2003, 2004, 2005, 2006
 Free Software Foundation, Inc.

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */

package org.jnode.shell.proclet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Formatter;

import gnu.classpath.SystemProperties;

/**
 * This class provides some infrastructure for PrintStream proxies.
 * 
 * @author crawley@jnode.org
 */
public abstract class AbstractProxyPrintStream extends PrintStream implements
        ProxyStream<PrintStream> {
    /*
     * Note: the implementation is mostly copied from PrintStream. Blame this on
     * limitations of the afore-mentioned class's specification ...
     */
    private static PrintStream nullStream;
    private static final char[] line_separator = SystemProperties.getProperty(
            "line.separator", "\n").toCharArray();

    private boolean error_occurred = false;

    public AbstractProxyPrintStream() {
        super((OutputStream) null);
    }

    public boolean checkError() {
        flush();
        return error_occurred || effectiveOutput().checkError();
    }

    protected void setError() {
        error_occurred = true;
    }

    protected void clearError() {
        error_occurred = false;
    }

    protected abstract PrintStream effectiveOutput();

    public void close() {
        PrintStream eo = effectiveOutput();
        eo.flush();
        eo.close();
    }

    public void flush() {
        effectiveOutput().flush();
    }

    protected synchronized void print(String str, boolean println) {
        PrintStream eo = effectiveOutput();
        writeChars(eo, str, 0, str.length());
        if (println) {
            writeChars(eo, line_separator, 0, line_separator.length);
        }
        flush();
    }

    protected synchronized void print(char[] chars, int pos, int len,
            boolean println) {
        PrintStream eo = effectiveOutput();
        writeChars(eo, chars, pos, len);
        if (println) {
            writeChars(eo, line_separator, 0, line_separator.length);
        }
        flush();
    }

    protected void writeChars(PrintStream eo, char[] buf, int offset, int count) {
        // This is inefficient, but it ensures that we use the encoding
        // scheme of the effective stream
        eo.print(new String(buf, offset, count));
    }

    protected void writeChars(PrintStream eo, String str, int offset, int count) {
        // This is inefficient, but it ensures that we use the encoding
        // scheme of the effective stream
        eo.print(str.substring(offset, offset + count));
    }

    public void print(boolean bool) {
        print(String.valueOf(bool), false);
    }

    public void print(int inum) {
        print(String.valueOf(inum), false);
    }

    public void print(long lnum) {
        print(String.valueOf(lnum), false);
    }

    public void print(float fnum) {
        print(String.valueOf(fnum), false);
    }

    public void print(double dnum) {
        print(String.valueOf(dnum), false);
    }

    public void print(Object obj) {
        print(obj == null ? "null" : obj.toString(), false);
    }

    public void print(String str) {
        print(str == null ? "null" : str, false);
    }

    public synchronized void print(char ch) {
        print(new char[] { ch }, 0, 1, false);
    }

    public void print(char[] charArray) {
        print(charArray, 0, charArray.length, false);
    }

    public void println() {
        print(line_separator, 0, line_separator.length, false);
    }

    public void println(boolean bool) {
        print(String.valueOf(bool), true);
    }

    public void println(int inum) {
        print(String.valueOf(inum), true);
    }

    public void println(long lnum) {
        print(String.valueOf(lnum), true);
    }

    public void println(float fnum) {
        print(String.valueOf(fnum), true);
    }

    public void println(double dnum) {
        print(String.valueOf(dnum), true);
    }

    public void println(Object obj) {
        print(obj == null ? "null" : obj.toString(), true);
    }

    public void println(String str) {
        print(str == null ? "null" : str, true);
    }

    public synchronized void println(char ch) {
        print(new char[] { ch }, 0, 1, true);
    }

    public void println(char[] charArray) {
        print(charArray, 0, charArray.length, true);
    }

    public void write(int oneByte) {
        effectiveOutput().write(oneByte & 0xff);
        if (oneByte == '\n') {
            flush();
        }
    }

    public void write(byte[] buffer, int offset, int len) {
        effectiveOutput().write(buffer, offset, len);
        flush();
    }

    public PrintStream append(char c) {
        print(c);
        return this;
    }

    public PrintStream append(CharSequence cs) {
        print(cs == null ? "null" : cs.toString());
        return this;
    }

    public PrintStream append(CharSequence cs, int start, int end) {
        print(cs == null ? "null" : cs.subSequence(start, end).toString());
        return this;
    }

    public PrintStream printf(String format, Object... args) {
        return format(format, args);
    }

    public PrintStream printf(Locale locale, String format, Object... args) {
        return format(locale, format, args);
    }

    public PrintStream format(String format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }

    public PrintStream format(Locale locale, String format, Object... args) {
        Formatter f = new Formatter(this, locale);
        f.format(format, args);
        return this;
    }

    /**
     * Return a print stream that will "deep six" any output.
     */
    protected static synchronized PrintStream getNullPrintStream() {
        if (nullStream == null) {
            nullStream = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            }, false);
        }
        return nullStream;
    }
}
