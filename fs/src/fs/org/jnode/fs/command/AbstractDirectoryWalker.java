package org.jnode.fs.command;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public abstract class AbstractDirectoryWalker {

    private class FileObject {
        final File file;
        final Long depth;

        FileObject(File file, Long depth) {
            this.file = file;
            this.depth = depth;
        }
    }

    private final Stack<FileObject> stack = new Stack<FileObject>();
    private final Set<FileFilter> filters = new HashSet<FileFilter>();
    private volatile boolean cancelled = false;
    private volatile Long maxDepth = null;
    private volatile Long minDepth = null;

    public synchronized void walk(final File... dirs) throws IOException {
        for (File dir : dirs) {
            if (dir == null)
                throw new IOException("No such directroy " + dir);
            dir = dir.getAbsoluteFile(); // to be able to handle relative paths
            if (!dir.canRead() || !dir.isDirectory()) {
                throw new IOException("Cannot read directroy " + dir);
            }
            stack.push(new FileObject(dir, 0L));
            while (!cancelled && !stack.isEmpty()) {
                go1(stack.pop());
            }
        }
    }

    private void go1(final FileObject file) throws IOException {
        if ((minDepth != null && file.depth < minDepth) || (maxDepth != null && file.depth > maxDepth)) {
            // out of boundaries
        } else if (!file.file.canRead()) {
            // ignore for now
        } else if (validFileOrDirectory(file)) {
            handleFileOrDir(file);
        } else {
            // filtered out
        }
        go2(file);
    }

    private void go2(final FileObject file) throws IOException {
        final Stack<File> stack = new Stack<File>();
        final File[] content = file.file.listFiles();
        if (content != null) {
            for (File f : content) {
                if (f.toString().equals(f.getCanonicalPath())) {
                    stack.push(f);
                } else {
                    // dont follow symlinks
                }
            }
            while (!stack.isEmpty()) {
                File tmp = stack.pop();
                // addToStack(stack.pop(), file.depth + 1);
                this.stack.push(new FileObject(tmp, file.depth + 1));
            }
        }
    }

    private void handleFileOrDir(final FileObject file) {
        if (file.file.isDirectory())
            handleDir(file.file);
        else if (file.file.isFile())
            handleFile(file.file);
        else {
            // ignore unknown file type
        }
    }

    private boolean validFileOrDirectory(final FileObject file) {
        if (!filters.isEmpty())
            for (FileFilter filter : filters)
                if (!filter.accept(file.file))
                    return false;
        return true;
    }

    public void stoppWalking() {
        cancelled = true;
    }

    public void setMinDepth(Long min) {
        minDepth = min;
    }

    public void setMaxDepth(Long max) {
        maxDepth = max;
    }

    public synchronized void addFilter(FileFilter filter) {
        filters.add(filter);
    }

    public abstract void handleDir(File f);

    public abstract void handleFile(File f);

}
