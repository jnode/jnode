/* Matcher.java -- Instance of a regular expression applied to a char sequence.
   Copyright (C) 2002, 2004, 2006 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.util.regex;

import gnu.java.util.regex.CharIndexed;
import gnu.java.util.regex.RE;
import gnu.java.util.regex.REMatch;

/**
 * Instance of a regular expression applied to a char sequence.
 *
 * @since 1.4
 */
public final class Matcher implements MatchResult
{
  private Pattern pattern;
  private CharSequence input;
  // We use CharIndexed as an input object to the getMatch method in order
  // that /\G/ (the end of the previous match) may work.  The information
  // of the previous match is stored in the CharIndexed object.
  private CharIndexed inputCharIndexed;
  private int position;
  private int appendPosition;
  private REMatch match;

  Matcher(Pattern pattern, CharSequence input)
  {
    this.pattern = pattern;
    this.input = input;
    this.inputCharIndexed = RE.makeCharIndexed(input, 0);
  }

  /**
   * @param sb The target string buffer
   * @param replacement The replacement string
   *
   * @exception IllegalStateException If no match has yet been attempted,
   * or if the previous match operation failed
   * @exception IndexOutOfBoundsException If the replacement string refers
   * to a capturing group that does not exist in the pattern
   */
  public Matcher appendReplacement (StringBuffer sb, String replacement)
    throws IllegalStateException
  {
    assertMatchOp();
    sb.append(input.subSequence(appendPosition,
                match.getStartIndex()).toString());
    sb.append(RE.getReplacement(replacement, match,
    RE.REG_REPLACE_USE_BACKSLASHESCAPE));
    appendPosition = match.getEndIndex();
    return this;
  }

  /**
   * @param sb The target string buffer
   */
  public StringBuffer appendTail (StringBuffer sb)
  {
    sb.append(input.subSequence(appendPosition, input.length()).toString());
    return sb;
  }

  /**
   * @exception IllegalStateException If no match has yet been attempted,
   * or if the previous match operation failed
   */
  public int end ()
    throws IllegalStateException
  {
    assertMatchOp();
    return match.getEndIndex();
  }

  /**
   * @param group The index of a capturing group in this matcher's pattern
   *
   * @exception IllegalStateException If no match has yet been attempted,
   * or if the previous match operation failed
   * @exception IndexOutOfBoundsException If the replacement string refers
   * to a capturing group that does not exist in the pattern
   */
  public int end (int group)
    throws IllegalStateException
  {
    assertMatchOp();
    return match.getEndIndex(group);
  }

  public boolean find ()
  {
    boolean first = (match == null);
    match = pattern.getRE().getMatch(inputCharIndexed, position);
    if (match != null)
      {
    int endIndex = match.getEndIndex();
    // Are we stuck at the same position?
    if (!first && endIndex == position)
      {
        match = null;
        // Not at the end of the input yet?
        if (position < input.length() - 1)
          {
        position++;
        return find(position);
          }
        else
          return false;
      }
    position = endIndex;
    return true;
      }
    return false;
  }

  /**
   * @param start The index to start the new pattern matching
   *
   * @exception IndexOutOfBoundsException If the replacement string refers
   * to a capturing group that does not exist in the pattern
   */
  public boolean find (int start)
  {
    match = pattern.getRE().getMatch(inputCharIndexed, start);
    if (match != null)
      {
    position = match.getEndIndex();
    return true;
      }
    return false;
  }

  /**
   * @exception IllegalStateException If no match has yet been attempted,
   * or if the previous match operation failed
   */
  public String group ()
  {
    assertMatchOp();
    return match.toString();
  }

  /**
   * @param group The index of a capturing group in this matcher's pattern
   *
   * @exception IllegalStateException If no match has yet been attempted,
   * or if the previous match operation failed
   * @exception IndexOutOfBoundsException If the replacement string refers
   * to a capturing group that does not exist in the pattern
   */
  public String group (int group)
    throws IllegalStateException
  {
    assertMatchOp();
    return match.toString(group);
  }

  /**
   * @param replacement The replacement string
   */
  public String replaceFirst (String replacement)
  {
    reset();
    // Semantics might not quite match
    return pattern.getRE().substitute(input, replacement, position,
    RE.REG_REPLACE_USE_BACKSLASHESCAPE);
  }

  /**
   * @param replacement The replacement string
   */
  public String replaceAll (String replacement)
  {
    reset();
    return pattern.getRE().substituteAll(input, replacement, position,
    RE.REG_REPLACE_USE_BACKSLASHESCAPE);
  }

  public int groupCount ()
  {
    return pattern.getRE().getNumSubs();
  }

  public boolean lookingAt ()
  {
    match = pattern.getRE().getMatch(inputCharIndexed, 0, RE.REG_FIX_STARTING_POSITION, null);
    if (match != null)
      {
    if (match.getStartIndex() == 0)
      {
        position = match.getEndIndex();
      return true;
      }
    match = null;
      }
    return false;
  }

  /**
   * Attempts to match the entire input sequence against the pattern. 
   *
   * If the match succeeds then more information can be obtained via the
   * start, end, and group methods.
   *
   * @see #start()
   * @see #end()
   * @see #group()
   */
  public boolean matches ()
  {
    match = pattern.getRE().getMatch(inputCharIndexed, 0, RE.REG_TRY_ENTIRE_MATCH|RE.REG_FIX_STARTING_POSITION, null);
    if (match != null)
      {
    if (match.getStartIndex() == 0)
      {
        position = match.getEndIndex();
    if (position == input.length())
      return true;
      }
    match = null;
      }
    return false;
  }

  /**
   * Returns the Pattern that is interpreted by this Matcher
   */
  public Pattern pattern ()
  {
    return pattern;
  }

  public Matcher reset ()
  {
    position = 0;
    match = null;
    return this;
  }

  /**
   * @param input The new input character sequence
   */
  public Matcher reset (CharSequence input)
  {
    this.input = input;
    this.inputCharIndexed = RE.makeCharIndexed(input, 0);
    return reset();
  }

  /**
   * @returns the index of a capturing group in this matcher's pattern
   *
   * @exception IllegalStateException If no match has yet been attempted,
   * or if the previous match operation failed
   */
  public int start ()
    throws IllegalStateException
  {
    assertMatchOp();
    return match.getStartIndex();
  }

  /**
   * @param group The index of a capturing group in this matcher's pattern
   *
   * @exception IllegalStateException If no match has yet been attempted,
   * or if the previous match operation failed
   * @exception IndexOutOfBoundsException If the replacement string refers
   * to a capturing group that does not exist in the pattern
   */
  public int start (int group)
    throws IllegalStateException
  {
    assertMatchOp();
    return match.getStartIndex(group);
  }

  /**
   * @return True if and only if the matcher hit the end of input.
   */
  public boolean hitEnd()
  {
    return inputCharIndexed.hitEnd();
  }

  /**
   * @return A string expression of this matcher.
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getName())
      .append("[pattern=").append(pattern.pattern())
      .append(" region=").append("0").append(",").append(input.length())
      .append(" lastmatch=").append(match == null ? "" : match.toString())
      .append("]");
    return sb.toString();
  }

  private void assertMatchOp()
  {
    if (match == null) throw new IllegalStateException();
  }
}
