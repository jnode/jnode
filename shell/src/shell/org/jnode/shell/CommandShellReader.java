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
package org.jnode.shell;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This Reader class handles CommandShell line buffering in interactive mode.
 * 
 * @author crawley@jnode.org
 */
class CommandShellReader extends Reader {
    
    private StringReader reader;
    private final Reader in;
    private final PrintWriter out;
    private final CommandShell shell;
    private final CommandInterpreter interpreter;
    private final List<String> lines = new ArrayList<String>(1);

    public CommandShellReader(CommandShell shell, CommandInterpreter interpreter, 
            PrintWriter out, Reader in) 
        throws IOException {
        this.shell = shell;
        this.interpreter = interpreter;
        this.out = out;
        this.in = in;
        nextReader(true);
    }
    
    private boolean nextReader(boolean first) throws IOException {
        String prompt;
        if (first) {
            prompt = interpreter.getPrompt(shell, false);
        } else if (interpreter.supportsMultiline()) {
            prompt = interpreter.getPrompt(shell, true);
        } else {
            return false;
        }
        out.print(prompt);
        out.flush();
        StringBuilder sb = new StringBuilder(40);
        try {
            shell.setReadingCommand(true);
            while (true) {
                int ch = in.read();
                if (ch == -1) {
                    if (sb.length() == 0) {
                        return false;
                    }
                    break;
                } else if (ch == '\n') {
                    break;
                } else {
                    sb.append((char) ch);
                }
            }
        } finally {
            shell.setReadingCommand(false);
        }
        this.lines.add(sb.toString());
        sb.append('\n');
        reader = new StringReader(sb.toString());
        return true;
    }
    
    @Override
    public int read() throws IOException {
        if (reader == null) {
            return -1;
        }
        int res = reader.read();
        if (res == -1 && nextReader(false)) {
            res = reader.read();
        }
        return res;
    }

    @Override
    public void close() throws IOException {
        reader = null;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new UnsupportedOperationException("mark is not supported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (reader == null) {
            return -1;
        }
        int res = reader.read(cbuf, off, len);
        if (res == 0 && nextReader(false)) {
            res = reader.read(cbuf, off, len);
        }
        return res;
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        if (reader == null) {
            return -1;
        }
        int res = reader.read(target);
        if (res == 0 && nextReader(false)) {
            res = reader.read(target);
        }
        return res;
    }
    
    @Override
    public boolean ready() throws IOException {
        if (reader == null) {
            return false;
        }
        return reader.ready();
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException("reset is not supported");
    }

    @Override
    public long skip(long n) throws IOException {
        throw new UnsupportedOperationException("skip is not supported");
    }

    public List<String> getLines() {
        return lines;
    }
    
}
