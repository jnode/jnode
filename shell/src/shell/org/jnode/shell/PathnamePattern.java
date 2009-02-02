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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loosely modeled on java.util.regex.Pattern, this class provides a simple
 * mechanism for expanding UNIX-style pathname patterns into a list of pathnames
 * for filesystem objects.
 * 
 * Depending on the flags supplied when a pattern is compiled, the following
 * pattern constructs are available:
 * <ul>
 * <li>A star ("*") matches zero or more characters.
 * <li>A question mark ("?") matches exactly one character.
 * <li>A matching pair of square brackets ("[]") denote a character class. The
 * character class "[abz]" matches one of "a", "b" or "z". Ranges are allowed,
 * so that "[0-9A-F]" matches a hexadecimal digit. If the first character of a
 * character class is "!" or "^", the character class is negated; i.e.
 * "[^a-zA-Z]" matches any chatacter that is not an ASCII letter.
 * <li>A single quote ("'") causes characters up to the next "'" to be treated
 * as literal characters.
 * <li>A backslash ("\") causes the next character (even a single quote) to be
 * treated as a literal character; i.e. any special meaning.
 * </ul>
 * 
 * Patterns are first split into file components on "/" boundaries, then the
 * sub-patterns are used to match names in a given directory. Neither quoting or
 * escaping affect "/" interpretation, and a "/" in a character class causes it
 * to be treated as literal characters.
 * 
 * The pattern expander treats "dot" files (i.e. files starting with ".") as
 * hidden. A hidden file is only matched when the pattern has an explicit "." as
 * the first character of a component. Thus the pattern "*" does not match "."
 * or "..", but the pattern ".*" does.
 * 
 * TODO:
 * <ul>
 * <li>Provide a method that returns a "lazy" pathname iterator for cases where
 * we don't want to build a (potentially huge) in-memory list of pathnames.
 * <li>Support expansions of ~ and {..,..} patterns.
 * <li>Add a parameter (or parameters) to allow the caller to limit the size of
 * the result list.
 * </ul>
 * 
 * @author crawley@jnode
 */
public class PathnamePattern {

    /**
     * When set, this flag causes the pathname list returned by 'expand' to be
     * lexically sorted.
     */
    public static final int SORT_MATCHES = 0x01;

    /**
     * When set, this flag enables UNIX like handling of hidden files. File and
     * directories whose name starts with a "." are only matched if the first
     * character in the pattern is a ".".
     */
    public static final int HIDE_DOT_FILENAMES = 0x02;

    /**
     * When set, this flag causes the '.' and '..' directories to be included in
     * domain of objects to be matched. (You probably don't want to set this
     * flag without setting HIDE_DOT_FILENAMES as well. Under normal
     * circumstances a user doesn't expect '.' and '..' to be returned in a
     * pattern match.)
     */
    public static final int INCLUDE_DOT_AND_DOTDOT = 0x04;

    /**
     * When set, this flag causes a '\' in a pattern to escape the next
     * character. For example, the sequence "\*" in a pattern will match a "*"
     * character in a filename.
     */
    public static final int SLASH_ESCAPES = 0x08;

    /**
     * When set, this flag causes characters inside matching single-quote
     * characters to be match literal characters in the pathname. Only a '\' is
     * unaffected. Thus "'a*c'" will match the file "a*c", but "'a\'c'" will
     * match "a'c"; i.e. a filename containing a single-quote character.
     */
    public static final int SINGLE_QUOTE_ESCAPES = 0x10;

    /**
     * When set, this flag causes the [...] character class syntax to be
     * recognized.
     */
    public static final int CHARACTER_CLASSES = 0x20;

    public static final int DEFAULT_FLAGS = SORT_MATCHES | HIDE_DOT_FILENAMES
            | INCLUDE_DOT_AND_DOTDOT | SLASH_ESCAPES | SINGLE_QUOTE_ESCAPES
            | CHARACTER_CLASSES;

    private static final boolean DEBUG = false;

    private final String source;
    private final Object[] pattern;
    private final boolean isAbsolute;

    // Use a weak reference for the pattern cache to avoid storage leakage.
    private static WeakReference<HashMap<String, PathnamePattern>> compiledPatterns;

    private PathnamePattern(String source, Object[] pattern, boolean isAbsolute) {
        this.source = source;
        this.pattern = pattern;
        this.isAbsolute = isAbsolute;
    }

    /**
     * Expand a pattern, returning the pathnames of the file system objects that
     * it matches.
     * 
     * @param current this is the notional current directory for expanding a
     *        relative pattern.
     * @return the lest of matching pathnames. The names will be absolute if the
     *         original pattern was absolute, and relative if not.
     */
    public LinkedList<String> expand(File current) {
        return doGlob(isAbsolute ? new File(File.separator) : current, 0,
                DEFAULT_FLAGS);
    }

    /**
     * Expand a pattern, returning the pathnames of the file system objects that
     * it matches.
     * 
     * @param current this is the notional current directory for expanding a
     *        relative pattern.
     * @param flags these flags control the behavior of the expander.
     * @return the lest of matching pathnames. The names will be absolute if the
     *         original pattern was absolute, and relative if not.
     */
    public LinkedList<String> expand(File current, int flags) {
        return doGlob(isAbsolute ? new File(File.separator) : current, 0, flags);
    }

    /**
     * This method recursively visits each element of the compiled pattern,
     * building a list of the pathname strings for FS objects that match it.
     * 
     * @param current the current file context for expansion.
     * @param pos our index into the 'pattern' array.
     * @return the list of partial pathnames matched in the context of
     *         'current'.
     */
    private LinkedList<String> doGlob(File current, int pos, int flags) {
        LinkedList<File> matches = new LinkedList<File>();
        LinkedList<String> res = new LinkedList<String>();
        if (pattern[pos] instanceof String) {
            File file = new File(current, (String) pattern[pos]);
            if (file.exists()) {
                matches.add(file);
            }
        } else {
            final Pattern pat = (Pattern) pattern[pos];
            final Matcher mat = pat.matcher("");
            final FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return mat.reset(name).matches();
                }
            };
            // A directory's "." and ".." entries are not returned by
            // File.listFiles
            // so we have to match / add them explicitly.
            if ((flags & INCLUDE_DOT_AND_DOTDOT) != 0) {
                if (filter.accept(current, ".")) {
                    matches.add(new File(current, "."));
                }
                if (filter.accept(current, "..")) {
                    matches.add(new File(current, ".."));
                }
            }
            // Process the 'regular' directory contents
            for (File file : current.listFiles(filter)) {
                matches.add(file);
            }
            if ((flags & SORT_MATCHES) == SORT_MATCHES) {
                Collections.sort(matches);
            }
        }

        for (File match : matches) {
            String name = match.getName();
            if (pos == pattern.length - 1) {
                res.add(name);
            } else if (match.isDirectory()) {
                LinkedList<String> subList = doGlob(match, pos + 1, flags);
                for (String sub : subList) {
                    res.add(name + File.separator + sub);
                }
            }
        }
        return res;
    }

    /**
     * Create and compile a pathname pattern using the default flags.
     * 
     * @param source the pattern source
     * @return a compiler pattern for the source.
     */
    public static PathnamePattern compile(String source) {
        return compile(source, DEFAULT_FLAGS);
    }

    /**
     * Create and compile a pathname pattern. The flags determine which pattern
     * meta-characters are recognized by the compiled pattern. If a pattern
     * meta-character is not recognized, it will be treated as a literal
     * character.
     * 
     * @param source the pattern source
     * @param flags pattern compilation flags
     * @return a compiler pattern for the source.
     */
    public static PathnamePattern compile(String source, int flags) {
        String key = flags + ":" + source;
        synchronized (PathnamePattern.class) {
            HashMap<String, PathnamePattern> cp;
            if (compiledPatterns != null
                    && (cp = compiledPatterns.get()) != null) {
                PathnamePattern pat = cp.get(key);
                if (pat != null) {
                    return pat;
                }
            }
        }

        boolean isAbsolute;
        if (source.startsWith(File.separator)) {
            while (source.startsWith(File.separator)) {
                source = source.substring(1);
            }
            isAbsolute = true;
        } else {
            isAbsolute = false;
        }
        String[] parts = source.split(File.separator + "+", -1);
        Object[] res = new Object[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            res[i] = (isPattern(part, flags)) ? Pattern.compile(createRegex(
                    part, flags)) : part;
            if (DEBUG)
                System.err.println(i + ": " + res[i]);
        }
        PathnamePattern pat = new PathnamePattern(source, res, isAbsolute);
        synchronized (PathnamePattern.class) {
            HashMap<String, PathnamePattern> cp = null;
            if (compiledPatterns == null
                    || (cp = compiledPatterns.get()) == null) {
                cp = new HashMap<String, PathnamePattern>();
                compiledPatterns = new WeakReference<HashMap<String, PathnamePattern>>(
                        cp);
            }
            cp.put(key, pat);
        }
        return pat;
    }

    /**
     * Provide a fast determination if a string requires pattern expansion,
     * assuming the default pattern flags.
     * 
     * @param str the string to be examined
     * @return <code>true</code> if the string is potentially a pattern; i.e.
     *         if it contains '*', '?' or '[' characters.
     */
    public static boolean isPattern(String str) {
        return isPattern(str, DEFAULT_FLAGS);
    }

    /**
     * Provide a fast determination if a string requires pattern expansion.
     * 
     * @param str the string to be examined
     * @param flags pattern compilation flags
     * @return <code>true</code> if the string is potentially a pattern; i.e.
     *         if it contains meta-characters enabled in the compilation flags.
     */
    public static boolean isPattern(String str, int flags) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            switch (str.charAt(i)) {
                case '*':
                case '?':
                    return true;
                case '[':
                    if ((flags & CHARACTER_CLASSES) != 0) {
                        return true;
                    }
                    break;
                case '\\':
                    if ((flags & SLASH_ESCAPES) != 0) {
                        return true;
                    }
                    break;
                case '\'':
                    if ((flags & SINGLE_QUOTE_ESCAPES) != 0) {
                        return true;
                    }
                    break;
                default:
            }
        }
        return false;
    }

    /**
     * Turn a string representing a pathname component into a regex.
     * 
     * @param filePattern the pathname pattern component
     * @return the corresponding regex.
     */
    private static String createRegex(String filePattern, int flags) {
        // This method needs to be really careful to avoid 'ordinary' characters
        // in the source pattern being accidentally mapped to Java regex
        // meta-characters.
        int len = filePattern.length();
        StringBuffer sb = new StringBuffer(len);
        boolean quoted = false;
        for (int i = 0; i < len; i++) {
            char ch = filePattern.charAt(i);
            switch (ch) {
                case '?':
                    if (quoted) {
                        sb.append(ch);
                    } else if (i == 0 && (flags & HIDE_DOT_FILENAMES) != 0) {
                        sb.append("[^\\.]");
                    } else {
                        sb.append(".");
                    }
                    break;
                case '*':
                    if (quoted) {
                        sb.append(ch);
                    } else if (i == 0 && (flags & HIDE_DOT_FILENAMES) != 0) {
                        sb.append("(|[^\\.].*)");
                    } else {
                        sb.append(".*");
                    }
                    break;
                case '[':
                    if ((flags & CHARACTER_CLASSES) != 0) {
                        int j;
                        StringBuffer sb2 = new StringBuffer(len);
                    LOOP: 
                        for (j = i + 1; j < len; j++) {
                            char ch2 = filePattern.charAt(j);
                            switch (ch2) {
                                case ']':
                                    break LOOP;
                                case '\\':
                                    sb2.append(protect(filePattern.charAt(++j)));
                                    break;
                                case '!':
                                case '^':
                                    sb2.append((j == i + 1) ? "^" : protect(ch2));
                                    break;
                                case '-':
                                    sb2.append('-');
                                    break;
                                default:
                                    sb2.append(protect(ch2));
                            }
                        }
                        if (j == len) {
                            sb.append('[');
                        } else {
                            sb.append("[").append(sb2).append(']');
                            i = j;
                        }
                    } else {
                        sb.append(protect(ch));
                    }
                    break;
                case '\\':
                    if ((flags & SLASH_ESCAPES) != 0) {
                        sb.append(protect(filePattern.charAt(++i)));
                    } else {
                        sb.append(protect(ch));
                    }
                    break;
                case '\'':
                    if ((flags & SINGLE_QUOTE_ESCAPES) != 0) {
                        quoted = !quoted;
                    } else {

                        sb.append(protect(ch));
                    }
                    break;
                default:
                    sb.append(protect(ch));
            }
        }
        return sb.toString();
    }

    private static String protect(char ch) {
        switch (ch) {
            case '.':
            case '|':
            case '[':
            case ']':
            case '(':
            case ')':
            case '+':
            case '*':
            case '?':
            case '$':
            case '^':
            case '\\':
                return "\\" + ch;
            default:
                return Character.toString(ch);
        }
    }

    public String toString() {
        return source;
    }
}
