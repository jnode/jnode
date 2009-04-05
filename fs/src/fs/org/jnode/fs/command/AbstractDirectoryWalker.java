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
        if (dirs == null) {
            throw new NullPointerException("Directory to walk from must not be null");
        }
        for (File dir : dirs) {
            if (dir == null || !dir.isDirectory())
                throw new IOException("No such directroy " + dir);
            dir = dir.getCanonicalFile(); // to be able to handle relative paths
            // and . / ..
            handleStartingDir(dir);
            stack.push(new FileObject(dir, 0L));
            while (!cancelled && !stack.isEmpty()) {
                go1(stack.pop());
            }
        }
    }

    private void go1(final FileObject file) throws IOException {
        if ((minDepth != null && file.depth < minDepth) ||
                (maxDepth != null && file.depth > maxDepth)) {
            // out of boundaries
        } else if (notFiltered(file)) {
            handleFileOrDir(file);
        } else {
            // filtered out
        }
        try {
            go2(file);
        } catch (SecurityException e) {
            handleRestrictedFile(file.file);
        }
    }

    private void go2(final FileObject file) throws IOException, SecurityException {
        final Stack<File> stack = new Stack<File>();
        final File[] content = file.file.listFiles();
        if (content == null) {
            // I/O Error or file
        } else if (content.length == 0) {
            // dir is empty
        } else {
            for (File f : content) {
                if (f.toString().equals(f.getCanonicalPath())) {
                    stack.push(f);
                } else {
                    // dont follow symlinks
                }
            }
            while (!stack.isEmpty()) {
                this.stack.push(new FileObject(stack.pop(), file.depth + 1));
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

    private boolean notFiltered(final FileObject file) {
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

    protected void handleRestrictedFile(final File file) throws IOException {
        throw new IOException("Permission denied for " + file);
    }

    protected void handleStartingDir(final File file) {
        // do nothing
    }

    public abstract void handleDir(final File file);

    public abstract void handleFile(final File file);

}
