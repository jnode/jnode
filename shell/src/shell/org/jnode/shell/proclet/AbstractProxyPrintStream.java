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
 
package org.jnode.shell.proclet;

import gnu.classpath.SystemProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Formatter;
import java.util.Locale;

import org.jnode.util.ProxyStream;

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
        print(new char[] {ch}, 0, 1, false);
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
        print(new char[] {ch}, 0, 1, true);
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
