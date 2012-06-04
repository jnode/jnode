/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jnode.shell.PathnamePattern;

/**
 * <p>
 * <code>AbstractDirectoryWalker</code> - walk through a directory hierarchy
 * recursively
 * </p>
 * <code>AbstractDirectoryWalker</code> will start at a given starting depth
 * relatively to the given directory and walk recursively through the directory
 * hierarchy until stopping depth is reached. <br>
 * On its way, it will call "handleFile()" and "handleDir()" for every file and
 * directory, that is not filtered out by any of the filters set for this
 * DirectoryWalker.
 * 
 * @author Alexander Kerner
 * @author chris boertien
 */
public abstract class AbstractDirectoryWalker {

    private static class FileObject {
        final File file;
        final Long depth;

        FileObject(File file, Long depth) {
            this.file = file;
            this.depth = depth;
        }
    }
    
    /**
     * A FileFilter that filters based on matching a pathname glob pattern
     */
    public static class PathnamePatternFilter implements FileFilter {
        
        private Matcher matcher;
        private boolean exclude;
        
        /**
         * Create the filter with the given pattern and orientation
         *
         * @param pattern the pathname glob pattern to match with
         * @param exclude if true, reverse the sense of matching and
         * exclude files that match.
         */
        public PathnamePatternFilter(String pattern, boolean exclude) {
            this.exclude = exclude;
            this.matcher = PathnamePattern.compilePosixShellPattern(pattern, 0).matcher("");
        }
        
        @Override
        public boolean accept(File file) {
            return matcher.reset(file.getName()).matches() ^ exclude;
        }
    }
    
    /**
     * A FileFilter that filters based on matching a regular expression.
     */
    public static class RegexPatternFilter implements FileFilter {
        
        private Matcher matcher;
        private boolean exclude;
        
        /**
         * Create the filter with the given pattern and orientation
         *
         * @param pattern the regular expression to match with
         * @param exclude  if true, reverse the sense of matching and
         * exclude files that match the pattern.
         */
        public RegexPatternFilter(String pattern, boolean exclude) {
            this.exclude = exclude;
            this.matcher = Pattern.compile(pattern).matcher("");
        }
        
        @Override
        public boolean accept(File file) {
            return matcher.reset(file.getName()).matches() ^ exclude;
        }
    }
    
    /**
     * A FileFilter that filters based on the file modification time.
     */
    public static class ModTimeFilter implements FileFilter {
        
        private long modTime;
        private boolean newer;
        
        /**
         * Create the filter with the given mod time and direction
         *
         * @param time the time point to filter on
         * @param newer if true, accept if the file mtime is &gt; time, false
         * accepts if the file mtime is &lt;= to time.
         */
        public ModTimeFilter(long time, boolean newer) {
            this.modTime = time;
            this.newer = newer;
        }
        
        @Override
        public boolean accept(File file) {
            return file.lastModified() == modTime || ((file.lastModified() < modTime) ^ newer);
        }
    }
    
    /**
     * A FileFilter that filters based on the file size.
     */
    public static class SizeFilter implements FileFilter {
        
        private long size;
        private boolean greater;
        
        /**
         * Create the filter with the given size and direction.
         *
         * @param size the size point to filter on
         * @param greater if true, accept if the file length is &gt; size, false
         * accepts if the file length is &lt;= to size.
         */
        public SizeFilter(long size, boolean greater) {
            this.size = size;
            this.greater = greater;
        }
        
        @Override
        public boolean accept(File file) {
            return file.length() == size || ((file.length() <= size) ^ greater);
        }
    }

    private final Stack<FileObject> stack = new Stack<FileObject>();
    private final Set<FileFilter> filters = new HashSet<FileFilter>();
    private final Set<FileFilter> dirFilters = new HashSet<FileFilter>();
    private volatile Long maxDepth = null;
    private volatile Long minDepth = null;
    private volatile boolean cancelled = false;

    /**
     * Walk the directory hierarchies of the given directories.
     *
     * Before walking begins on each of the given directories, the
     * extending class has a chance to do some initialization through
     * {@link #handleStartingDir(File)}. Once walking has commenced, each file
     * will be checked against the current set of constraints, and
     * passed to the extending class for further processing if accepted.
     * When walking is complete for that branch, the {@code lastAction}
     * method is called, and the walker moves on to the next directory,
     * or returns if there are no more directories to walk.
     *
     * If an IOException propagates beyond the {@code walk} method, there
     * is currently no way to resume walking. The following reasons may
     * cause this to happen.
     * <ul>
     * <li>Any of the supplied directories are null, or not a directory.
     * <li>A SecurityException was triggered, and the caller has not overriden
     * the {@link #handleRestrictedFile(File)} method.
     * </ul>
     * 
     * @param dirs array of {@link java.io.File} to walk through.
     * @throws IOException if any IO error occurs.
     * @throws NullPointerException if dirs is null, or contains no directories
     */
    public synchronized void walk(final File... dirs) throws IOException {
        if (dirs == null || dirs.length == 0) {
            throw new NullPointerException("Directory to walk from must not be null");
        }
        for (File dir : dirs) {
            // perhaps this shouldn't fail like this, as it may
            // be possible that this was simply due to a race condition
            // with another process that has deleted the directory already
            if (dir == null || !dir.isDirectory())
                throw new IOException("No such directory " + dir);
            
            /* See note in handleChilds()
            dir = dir.getCanonicalPath();
            */
            handleStartingDir(dir);
            stack.push(new FileObject(dir, 0L));
            while (!cancelled && !stack.isEmpty()) {
                handle(stack.pop());
            }
            lastAction(cancelled);
            // if this was canceled, we need to clear the stack
            stack.clear();
        }
    }
    
    public synchronized void walk(final List<File> dirs) throws IOException {
        walk(dirs.toArray(new File[dirs.size() ]));
    }
    
    private void handle(final FileObject file) throws IOException {
        if (minDepth != null && file.depth < minDepth) {
            // out of boundaries
        } else if (notFiltered(file.file)) {
            handleFileOrDir(file);
        } else {
            // filtered out
        }
        try {
            // Don't descend into directories beyond maxDepth
            if (file.file.isDirectory() && (maxDepth == null || file.depth < maxDepth) && dirNotFiltered(file.file)) {
                handleChildren(file);
            }
        } catch (SecurityException e) {
            // Exception rises, when access to folder content was denied
            handleRestrictedFile(file.file);
        }
    }
    
    /**
     * Add a directories contents to the stack.
     */
    private void handleChildren(final FileObject file) throws IOException, SecurityException {
        final Stack<File> stack = new Stack<File>();
        final File[] content = file.file.listFiles();
        if (content == null) {
            // I/O Error or file
        } else if (content.length == 0) {
            // dir is empty
        } else {
            for (File f : content) {
                /* I dont think is the right way to handle this. getCanonicalPath()
                 * does more than just trim symlinks, and symlinks aren't something
                 * we need to worry about. Even when we do we should have a lower
                 * level API to work with.
                 * - Chris
                if (f.toString().equals(f.getCanonicalPath())) {
                    stack.push(f);
                } else {
                    // dont follow symlinks
                }
                */
                stack.push(f);
            }
            while (!stack.isEmpty()) {
                this.stack.push(new FileObject(stack.pop(), file.depth + 1));
            }
        }

    }
    
    /**
     * Trigger the callbacks
     */
    private void handleFileOrDir(final FileObject file) throws IOException {
        if (file.file.isDirectory())
            handleDir(file.file);
        else if (file.file.isFile())
            handleFile(file.file);
        else {
            handleSpecialFile(file.file);
        }
    }
    
    /**
     * Process a file through a set of file filters.
     *
     * This may be called from extending classes in order to bypass
     * the regular walking procedure.
     *
     * As an example, if the caller simply wants to process specific
     * files through the filter set, without setting up a full directory
     * walk.
     *
     * @param file the file to check
     * @return true if the file was accepted by all the filters, or if there
     * were not filters.
     */
    protected final boolean notFiltered(final File file) {
        if (!filters.isEmpty())
            for (FileFilter filter : filters)
                if (!filter.accept(file))
                    return false;
        return true;
    }
    
    /**
     * Stop recursing if this is false.
     */
    private boolean dirNotFiltered(File file) {
        if (!dirFilters.isEmpty()) {
            for (FileFilter filter : dirFilters) {
                if (!filter.accept(file)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Stop walking the current directory hierarchy.
     *
     * This will not stop the walker altogether if there were multiple directories
     * passed to {@code walk}. Instead, walking of the current directory hierarchy
     * will stop, {@code lastAction(true)} is called, and the walker is reset with
     * the next directory. If this it was the last directory, or there was only one
     * then walk will return without error.
     */
    public void stopWalking() {
        cancelled = true;
    }

    /**
     * The minimum depth level (exclusive) at which to begin handling files.
     *
     * The initial directory has a depth level of 0. Therefore if you set the
     * minimum depth to 0, the initial directory will not handled, but its
     * contents will.
     *
     * A negative value will be seen as null.
     * 
     * @param min starting depth at which actual action performing is started.
     */
    public void setMinDepth(Long min) {
        if (min >= 0) {
            minDepth = min;
        }
    }

    /**
     * The maximum depth level (inclusive) at which to stop handling files.
     *
     * When the walker reaches this level, it will not recurse any deeper
     * into the file hierarchy. If the maximum depth is 0, the initial directory
     * will be handled, but the walker will not query for its contents.
     *
     * A negative value will be seen as null.
     *
     * @param max ending depth at which actual action performing is stopped.
     */
    public void setMaxDepth(Long max) {
        if (max >= 0) {
            maxDepth = max;
        }
    }

    /**
     * Add a FileFilter to this walker.
     *
     * Before the extended class is asked to handle a file or directory, it
     * must be accepted by the set of filters supplied. If no filters are
     * supplied, then every file and directory will be handled.
     *
     * @param filter a {@link FileFilter} to be added to this
     *            DirectoryWalker's FilterSet.
     */
    public synchronized void addFilter(FileFilter filter) {
        filters.add(filter);
    }
    
    /**
     * Add a FileFilter to stop recursing of directories.
     *
     * Before recursing a directory, it must be accepted by the set of
     * directory filters supplied. If no filters are supplied, then this
     * will not prevent recursing of directories.
     *
     * @param filter {@link FileFilter} to be added
     */
    public synchronized void addDirectoryFilter(FileFilter filter) {
        dirFilters.add(filter);
    }
    
    /**
     * Handle a file or directory that triggered a SecurityException.
     * This method is called, when access to a file was denied.
     * <p>
     * The default implementation will raise an {@link IOException} instead of a
     * {@link SecurityException}. May be overridden by extending classes to
     * do something else.
     *
     * Because this method throws an IOException that will propagate beyond the
     * walk method, if an application wishes to continue walking after encountering
     * a SecurityException while accessing a file or directory, then it must override
     * this and provide an implementation that does not throw an exception.
     * 
     * @param file {@code File} object, to which access was restricted.
     * @throws IOException in default implementation.
     */
    protected void handleRestrictedFile(final File file) throws IOException {
        throw new IOException("Permission denied for " + file);
    }

    /**
     * Handle the initial directory of a tree.
     *
     * This method is called, when walking is about to start. It gets called
     * for each directory that was initially supplied to the walk method.
     * 
     * By default, it does nothing. May be overridden by extending classes to do
     * something else.
     *
     * Override this to do some initialization before starting to walk each of the
     * given directory roots.
     * 
     * @param file {@code File} object, that represents starting dir.
     * @throws IOException if IO error occurs.
     */
    protected void handleStartingDir(final File file) throws IOException {
        // do nothing by default
    }
    
    /**
     * This method is called, when walking has finished.
     * By default, it does nothing. May be overridden by extending classes to do something else.
     * @param wasCancelled true, if directory walking was aborted.
     */
    protected void lastAction(boolean wasCancelled){
        // do nothing by default
    }

    /**
     * Handle a directory.
     *
     * Override this to perform some operation on this directory.
     *
     * @param file {@code File} object, that represents current directory.
     * @throws IOException if IO error occurs.
     */
    public abstract void handleDir(final File file) throws IOException;

    /**
     * Handle a file.
     *
     * Override this to perform some operation on this file.
     *
     * @param file {@code File} object, that represents current file.
     * @throws IOException if IO error occurs.
     */
    public abstract void handleFile(final File file) throws IOException;
    
    /**
     * Handle a special file.
     *
     * Override this to perform some operation on this special file.
     *
     * @param file {@code File} object that represents a special file.
     * @throws IOException if an IO error occurs.
     */
    public void handleSpecialFile(File file) throws IOException {
        // do nothing by default
    }
}
