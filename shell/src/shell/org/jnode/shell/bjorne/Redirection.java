package org.jnode.shell.bjorne;

import java.io.File;

public class Redirection {
    private final File file;

    private final int fd;

    private final boolean output;

    private final boolean append;

    public Redirection(File file, boolean output, boolean append) {
        super();
        this.file = file;
        this.output = output;
        this.append = append;
        this.fd = -1;
    }

    public Redirection(int fd, boolean output) {
        super();
        this.file = null;
        this.output = output;
        this.append = true;
        this.fd = fd;
    }

    public boolean isFileRedirection() {
        return file != null;
    }

    public boolean isAppend() {
        return append;
    }

    public File getFile() {
        return file;
    }

    public boolean isOutput() {
        return output;
    }

    public int getFD() {
        return fd;
    }

}
