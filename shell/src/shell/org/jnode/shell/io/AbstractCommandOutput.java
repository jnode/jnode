package org.jnode.shell.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

// FIXME implement our own one ...
import org.apache.tools.ant.util.ReaderInputStream;

public abstract class AbstractCommandOutput implements CommandOutput {
    
    private Writer writer;
    private OutputStream outputStream;

    AbstractCommandOutput(Writer writer, OutputStream outputStream) {
        this.writer = writer;
        this.outputStream = outputStream;
    }

    @Override
    public synchronized OutputStream getOutputStream() {
        if (outputStream == null) {
            outputStream = new WriterOutputStream(writer, getEncoding());
        }
        return outputStream;
    }

    @Override
    public Writer getWriter() throws CommandIOException {
        if (writer == null) {
            try {
                writer = new OutputStreamWriter(outputStream, getEncoding());
            } catch (UnsupportedEncodingException ex) {
                throw new CommandIOException("Cannot get writer", ex);
            }
        }
        return writer;
    }

    @Override
    public final int getDirection() {
        return DIRECTION_OUT;
    }

    @Override
    public Object getSystemObject() {
        return null;
    }

    @Override
    public boolean isTTY() {
        // TODO Auto-generated method stub
        return false;
    }

}
