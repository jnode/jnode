package org.jnode.shell.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public abstract class AbstractCommandInput implements CommandInput {
    
    private Reader reader;
    private InputStream inputStream;

    AbstractCommandInput(Reader reader, InputStream inputStream) {
        this.reader = reader;
        this.inputStream = inputStream;
    }

    @Override
    public synchronized InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new ReaderInputStream(reader, getEncoding());
        }
        return inputStream;
    }

    @Override
    public Reader getReader() throws CommandIOException {
        if (reader == null) {
            try {
                reader = new InputStreamReader(inputStream, getEncoding());
            } catch (UnsupportedEncodingException ex) {
                throw new CommandIOException("Cannot get reader", ex);
            }
        }
        return reader;
    }

    @Override
    public final int getDirection() {
        return DIRECTION_IN;
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
