/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
import java.util.ArrayList;
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
 * "[^a-zA-Z]" matches any character that is not an ASCII letter.
 * <li>A single quote ("'") causes characters up to the next "'" to be treated
 * as literal characters.
 * <li>A backslash ("\") causes the next character (even a single quote) to be
 * treated as a literal character; i.e. any special meaning.
 * </ul>
 * <p>
 * Patterns are first split into file components on "/" boundaries, then the
 * sub-patterns are used to match names in a given directory. Neither quoting or
 * escaping affect "/" interpretation, and a "/" in a character class causes it
 * to be treated as literal characters.
 * <p>
 * The pattern expander treats "dot" files (i.e. files starting with ".") as
 * hidden. A hidden file is only matched when the pattern has an explicit "." as
 * the first character of a component. Thus the pattern "*" does not match "."
 * or "..", but the pattern ".*" does.
 * <p>
 * This class also exposes a static method for compiling patterns in the UNIX
 * shell-style syntax to Java {@link Pattern} objects.  The resulting
 * objects allow you to use the shell-style syntax for matching arbitrary 
 * strings.  The pathname-specific matching behaviors of PathnamePattern 
 * such as implicit anchoring, and the handling of '/' in character classes
 * are supported via flags.
 * <p>
 * TODO:
 * <ul>
 * <li>Provide a method that returns a "lazy" pathname iterator for cases where
 * we don't want to build a (potentially huge) in-memory list of pathnames.
 * <li>Support expansions of ~ and {..,..} patterns.  (Note that the latter are
 * not part of the POSIX specification.)
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
    public static final int BACKSLASH_ESCAPES = 0x08;

    /**
     * When set, this flag causes characters inside matching single-quote
     * characters to be match literal characters in the pathname. Only a '\' is
     * unaffected. Thus "'a*c'" will match the file "a*c", but "'a\'c'" will
     * match "a'c"; i.e. a filename containing a single-quote character.
     */
    public static final int SINGLE_QUOTE_ESCAPES = 0x10;
    
    /**
     * When set, this flag causes characters inside matching double-quote
     * characters to be match literal characters in the pathname. Only a '\' is
     * unaffected. Thus ""a*c"" will match the file "a*c", but ""a\"c"" will
     * match "a"c"; i.e. a filename containing a double-quote character.
     */
    public static final int DOUBLE_QUOTE_ESCAPES = 0x20;

    /**
     * When set, this flag causes the [...] character class syntax to be
     * recognized.
     */
    public static final int CHARACTER_CLASSES = 0x40;
    
    /**
     * When set, the pattern is anchored to the left of the string to be searched. 
     * This is set implicitly by the pathname matching methods.
     */
    public static final int ANCHOR_LEFT = 0x80;
    
    /**
     * When set, the pattern is anchored to the right of the string to be searched.  
     * This is set implicitly by the pathname matching methods.
     */
    public static final int ANCHOR_RIGHT = 0x100;
    
    /**
     * When set, '*' is eager, matching as many characters as possible.  
     * This is set implicitly by the pathname matching methods. 
     * matching is always eager.
     */
    public static final int EAGER = 0x200;
    
    /**
     * When set, an unescaped '/' inside a character class causes the entire class
     * to be interpreted as a literal character sequence.  
     * This is set implicitly by the pathname matching methods.
     */
    public static final int SLASH_DISABLES_CHARACTER_CLASSES = 0x400;
    

    public static final int DEFAULT_FLAGS = SORT_MATCHES | HIDE_DOT_FILENAMES
            | INCLUDE_DOT_AND_DOTDOT | BACKSLASH_ESCAPES | SINGLE_QUOTE_ESCAPES
            | DOUBLE_QUOTE_ESCAPES | CHARACTER_CLASSES;

    private static final boolean DEBUG = false;

    private final String source;
    private ArrayList<Object> patterns;
    private boolean isAbsolute;
    private char lastQuote;

    // Use a weak reference for the pattern cache to avoid storage leakage.
    private static WeakReference<HashMap<String, PathnamePattern>> cache;

    private PathnamePattern(String source) {
        this.source = source;
        this.patterns = new ArrayList<Object>();
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
        if (patterns.get(pos) instanceof String) {
            File file = new File(current, (String) patterns.get(pos));
            if (file.exists()) {
                matches.add(file);
            }
        } else {
            final Pattern pat = (Pattern) patterns.get(pos);
            final Matcher mat = pat.matcher("");
            final FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return mat.reset(name).matches();
                }
            };
            // A directory's "." and ".." entries are not returned by
            // File.listFiles so we have to match / add them explicitly.
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
            if (pos == 0 && isAbsolute) {
                name = File.separator + name;
            }
            if (pos == patterns.size() - 1) {
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
    public static PathnamePattern compilePathPattern(String source) {
        return compilePathPattern(source, DEFAULT_FLAGS);
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
    public static PathnamePattern compilePathPattern(String source, int flags) {
        String key = flags + ":" + source;
        synchronized (PathnamePattern.class) {
            HashMap<String, PathnamePattern> cp;
            if (cache != null && (cp = cache.get()) != null) {
                PathnamePattern pat = cp.get(key);
                if (pat != null) {
                    return pat;
                }
            }
        }

        PathnamePattern pp = new PathnamePattern(source);
        String[] parts = source.split(File.separator + "+", -1);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            Object pat = (isPattern(part, flags)) ?
                compilePosixShellPattern(part, 
                        flags | ANCHOR_LEFT | ANCHOR_RIGHT | EAGER | SLASH_DISABLES_CHARACTER_CLASSES,
                        pp) : part;
            if (pat == null || pat.toString().length() == 0) {
                if (i == 0) {
                    pp.isAbsolute = true;
                }
            } else {
                pp.patterns.add(pat);
            }
            if (DEBUG) {
                System.err.println(i + ": " + pat);
            }
        }
        if (pp.lastQuote != 0) {
            throw new IllegalArgumentException("Unbalanced quotes in pattern");
        }
        synchronized (PathnamePattern.class) {
            HashMap<String, PathnamePattern> cp = null;
            if (cache == null || (cp = cache.get()) == null) {
                cp = new HashMap<String, PathnamePattern>();
                cache = new WeakReference<HashMap<String, PathnamePattern>>(cp);
            }
            cp.put(key, pp);
        }
        return pp;
    }
    
    /**
     * Clear the pattern cache
     */
    public static void clearCache() {
        synchronized (PathnamePattern.class) {
            cache = null;
        }
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
                    if ((flags & BACKSLASH_ESCAPES) != 0) {
                        return true;
                    }
                    break;
                case '\'':
                    if ((flags & SINGLE_QUOTE_ESCAPES) != 0) {
                        return true;
                    }
                    break;
                case '\"':
                    if ((flags & DOUBLE_QUOTE_ESCAPES) != 0) {
                        return true;
                    }
                    break;
                default:
            }
        }
        return false;
    }

    /**
     * Turn a string in POSIX shell pattern syntax into a regex.  This method
     * generates a {@link Pattern} that can be matched against a character sequence.
     * 
     * @param pattern the pattern in shell syntax.
     * @param flags compilation flags
     * @return the corresponding regex as a {@link Pattern}.
     */
    public static Pattern compilePosixShellPattern(CharSequence pattern, int flags) {
        return compilePosixShellPattern(pattern, flags, null);
    }
    
    /**
     * @param pattern the pattern in shell syntax.
     * @param flags compilation flags
     * @param pp if not {@code null}, 
     * @return the corresponding regex as a {@link Pattern}.
     */
    private static Pattern compilePosixShellPattern(
            CharSequence pattern, int flags, PathnamePattern pp) {
        // This method needs to be really careful to avoid 'ordinary' characters
        // in the source pattern being accidentally mapped to Java regex
        // meta-characters.
        int len = pattern.length();
        StringBuilder sb = new StringBuilder(len);
        char quote = (pp == null) ? ((char) 0) : pp.lastQuote;
        boolean eager = (flags & EAGER) != 0;
        for (int i = 0; i < len; i++) {
            char ch = pattern.charAt(i);
            switch (ch) {
                case '?':
                    if (quote != 0) {
                        sb.append(protect(ch));
                    } else if (i == 0 && (flags & HIDE_DOT_FILENAMES) != 0) {
                        sb.append("[^\\.]");
                    } else {
                        sb.append(".");
                    }
                    break;
                case '*':
                    if (quote != 0) {
                        sb.append(protect(ch));
                    } else if (i == 0 && (flags & HIDE_DOT_FILENAMES) != 0) {
                        sb.append("(|[^\\.]").append(eager ? ".*" : ".*?").append(")");
                    } else {
                        sb.append(eager ? ".*" : ".*?");
                    }
                    break;
                case '[':
                    if ((flags & CHARACTER_CLASSES) != 0) {
                        int j;
                        StringBuilder sb2 = new StringBuilder(len);
                        boolean charClassOK = true;
                    LOOP: 
                        for (j = i + 1; j < len; j++) {
                            char ch2 = pattern.charAt(j);
                            switch (ch2) {
                                case ']':
                                    break LOOP;
                                case '\\':
                                    sb2.append(protect(pattern.charAt(++j)));
                                    break;
                                case '!':
                                case '^':
                                    sb2.append((j == i + 1) ? "^" : protect(ch2));
                                    break;
                                case '-':
                                    sb2.append('-');
                                    break;
                                case '/':
                                    sb2.append(protect(ch2));
                                    charClassOK = ((flags & SLASH_DISABLES_CHARACTER_CLASSES) == 0);
                                    break;
                                default:
                                    sb2.append(protect(ch2));
                            }
                        }
                        if (j == len) {
                            sb.append(protect('['));
                        } else if (!charClassOK) {
                            sb.append(protect('[')).append(sb2).append(protect(']'));
                            i = j;
                        } else {
                            sb.append("[").append(sb2).append(']');
                            i = j;
                        }
                    } else {
                        sb.append(protect(ch));
                    }
                    break;
                case '\\':
                    if ((flags & BACKSLASH_ESCAPES) != 0) {
                        sb.append(protect(pattern.charAt(++i)));
                    } else {
                        sb.append(protect(ch));
                    }
                    break;
                case '\'':
                    if ((flags & SINGLE_QUOTE_ESCAPES) != 0) {
                        if (quote == '\'') {
                            quote = 0;
                        } else if (quote == 0) {
                            quote = '\'';
                        } else {
                            sb.append(protect(ch));
                        }
                    } else {
                        sb.append(protect(ch));
                    }
                    break;
                case '\"':
                    if ((flags & DOUBLE_QUOTE_ESCAPES) != 0) {
                        if (quote == '\"') {
                            quote = 0;
                        } else if (quote == 0) {
                            quote = '\"';
                        } else {
                            sb.append(protect(ch));
                        }
                    } else {
                        sb.append(protect(ch));
                    }
                    break;
                default:
                    sb.append(protect(ch));
            }
        }
        if (pp != null) {
            pp.lastQuote = quote;
        }
        if (sb.length() == 0) {
            return null;
        }
        if ((flags & ANCHOR_LEFT) != 0) {
            sb.insert(0, '^');
        }
        if ((flags & ANCHOR_RIGHT) != 0) {
            sb.append('$');
        }
        return Pattern.compile(sb.toString());
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
            case '{':
            case '}':
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

    public String toRegexString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PathnamePattern{source='").append(this.source);
        sb.append("',absolute=").append(this.isAbsolute);
        sb.append(",patterns=[");
        int len = this.patterns.size();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append('\'').append(patterns.get(i)).append('\'');
        }
        sb.append("]}");
        return sb.toString();
    }
}
