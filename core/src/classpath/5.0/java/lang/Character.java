/* java.lang.Character -- Wrapper class for char, and Unicode subsets
   Copyright (C) 1998, 1999, 2001, 2002, 2004, 2005 Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

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


package java.lang;

import gnu.java.lang.CharData;

import java.io.Serializable;
import java.text.Collator;
import java.util.Locale;

/**
 * Wrapper class for the primitive char data type.  In addition, this class
 * allows one to retrieve property information and perform transformations
 * on the 57,707 defined characters in the Unicode Standard, Version 3.0.0.
 * java.lang.Character is designed to be very dynamic, and as such, it
 * retrieves information on the Unicode character set from a separate
 * database, gnu.java.lang.CharData, which can be easily upgraded.
 *
 * <p>For predicates, boundaries are used to describe
 * the set of characters for which the method will return true.
 * This syntax uses fairly normal regular expression notation.
 * See 5.13 of the Unicode Standard, Version 3.0, for the
 * boundary specification.
 *
 * <p>See <a href="http://www.unicode.org">http://www.unicode.org</a>
 * for more information on the Unicode Standard.
 *
 * @author Tom Tromey (tromey@cygnus.com)
 * @author Paul N. Fisher
 * @author Jochen Hoenicke
 * @author Eric Blake (ebb9@email.byu.edu)
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 * @see CharData
 * @since 1.0
 * @status partly updated to 1.5; some things still missing
 */
public final class Character implements Serializable, Comparable<Character>
{
  /**
   * A subset of Unicode blocks.
   *
   * @author Paul N. Fisher
   * @author Eric Blake (ebb9@email.byu.edu)
   * @since 1.2
   */
  public static class Subset
  {
    /** The name of the subset. */
    private final String name;

    /**
     * Construct a new subset of characters.
     *
     * @param name the name of the subset
     * @throws NullPointerException if name is null
     */
    protected Subset(String name)
    {
      // Note that name.toString() is name, unless name was null.
      this.name = name.toString();
    }

    /**
     * Compares two Subsets for equality. This is <code>final</code>, and
     * restricts the comparison on the <code>==</code> operator, so it returns
     * true only for the same object.
     *
     * @param o the object to compare
     * @return true if o is this
     */
    public final boolean equals(Object o)
    {
      return o == this;
    }

    /**
     * Makes the original hashCode of Object final, to be consistent with
     * equals.
     *
     * @return the hash code for this object
     */
    public final int hashCode()
    {
      return super.hashCode();
    }

    /**
     * Returns the name of the subset.
     *
     * @return the name
     */
    public final String toString()
    {
      return name;
    }
  } // class Subset

  /**
   * A family of character subsets in the Unicode specification. A character
   * is in at most one of these blocks.
   *
   * This inner class was generated automatically from
   * <code>doc/unicode/Blocks-4.0.0.txt</code>, by some perl scripts.
   * This Unicode definition file can be found on the
   * <a href="http://www.unicode.org">http://www.unicode.org</a> website.
   * JDK 1.5 uses Unicode version 4.0.0.
   *
   * @author scripts/unicode-blocks.pl (written by Eric Blake)
   * @since 1.2
   */
  public static final class UnicodeBlock extends Subset
  {
    /** The start of the subset. */
    private final int start;

    /** The end of the subset. */
    private final int end;

    /** The canonical name of the block according to the Unicode standard. */
    private final String canonicalName;

    /** Enumeration for the <code>forName()</code> method */
    private enum NameType { CANONICAL, NO_SPACES, CONSTANT; };

    /**
     * Constructor for strictly defined blocks.
     *
     * @param start the start character of the range
     * @param end the end character of the range
     * @param name the block name
     * @param canonicalName the name of the block as defined in the Unicode
     *        standard.
     */
    private UnicodeBlock(int start, int end, String name,
			 String canonicalName)
    {
      super(name);
      this.start = start;
      this.end = end;
      this.canonicalName = canonicalName;
    }

    /**
     * Returns the Unicode character block which a character belongs to.
     * <strong>Note</strong>: This method does not support the use of
     * supplementary characters.  For such support, <code>of(int)</code>
     * should be used instead.
     *
     * @param ch the character to look up
     * @return the set it belongs to, or null if it is not in one
     */
    public static UnicodeBlock of(char ch)
    {
      return of((int) ch);
    }

    /**
     * Returns the Unicode character block which a code point belongs to.
     *
     * @param codePoint the character to look up
     * @return the set it belongs to, or null if it is not in one.
     * @throws IllegalArgumentException if the specified code point is
     *         invalid.
     * @since 1.5
     */
    public static UnicodeBlock of(int codePoint)
    {
      if (codePoint > MAX_CODE_POINT)
	throw new IllegalArgumentException("The supplied integer value is " +
					   "too large to be a codepoint.");
      // Simple binary search for the correct block.
      int low = 0;
      int hi = sets.length - 1;
      while (low <= hi)
        {
          int mid = (low + hi) >> 1;
          UnicodeBlock b = sets[mid];
          if (codePoint < b.start)
            hi = mid - 1;
          else if (codePoint > b.end)
            low = mid + 1;
          else
            return b;
        }
      return null;
    }

    /**
     * <p>
     * Returns the <code>UnicodeBlock</code> with the given name, as defined
     * by the Unicode standard.  The version of Unicode in use is defined by
     * the <code>Character</code> class, and the names are given in the
     * <code>Blocks-<version>.txt</code> file corresponding to that version.
     * The name may be specified in one of three ways:
     * </p>
     * <ol>
     * <li>The canonical, human-readable name used by the Unicode standard.
     * This is the name with all spaces and hyphens retained.  For example,
     * `Basic Latin' retrieves the block, UnicodeBlock.BASIC_LATIN.</li>
     * <li>The canonical name with all spaces removed e.g. `BasicLatin'.</li>
     * <li>The name used for the constants specified by this class, which
     * is the canonical name with all spaces and hyphens replaced with
     * underscores e.g. `BASIC_LATIN'</li>
     * </ol>
     * <p>
     * The names are compared case-insensitively using the case comparison
     * associated with the U.S. English locale.  The method recognises the
     * previous names used for blocks as well as the current ones.  At
     * present, this simply means that the deprecated `SURROGATES_AREA'
     * will be recognised by this method (the <code>of()</code> methods
     * only return one of the three new surrogate blocks).
     * </p>
     *
     * @param blockName the name of the block to look up.
     * @return the specified block.
     * @throws NullPointerException if the <code>blockName</code> is
     *         <code>null</code>.
     * @throws IllegalArgumentException if the name does not match any Unicode
     *         block.
     * @since 1.5
     */
    public static final UnicodeBlock forName(String blockName)
    {
      NameType type;
      if (blockName.indexOf(' ') != -1)
        type = NameType.CANONICAL;
      else if (blockName.indexOf('_') != -1)
        type = NameType.CONSTANT;
      else
        type = NameType.NO_SPACES;
      Collator usCollator = Collator.getInstance(Locale.US);
      usCollator.setStrength(Collator.PRIMARY);
      /* Special case for deprecated blocks not in sets */
      switch (type)
      {
        case CANONICAL:
          if (usCollator.compare(blockName, "Surrogates Area") == 0)
            return SURROGATES_AREA;
          break;
        case NO_SPACES:
          if (usCollator.compare(blockName, "SurrogatesArea") == 0)
            return SURROGATES_AREA;
          break;
        case CONSTANT:
          if (usCollator.compare(blockName, "SURROGATES_AREA") == 0) 
            return SURROGATES_AREA;
          break;
      }
      /* Other cases */
      switch (type)
      {
        case CANONICAL:
          for (UnicodeBlock block : sets)
            if (usCollator.compare(blockName, block.canonicalName) == 0)
              return block;
          break;
        case NO_SPACES:
          for (UnicodeBlock block : sets)
	    {
	      String nsName = block.canonicalName.replaceAll(" ","");
	      if (usCollator.compare(blockName, nsName) == 0)
		return block;
	    }
	  break;
        case CONSTANT:
          for (UnicodeBlock block : sets)
            if (usCollator.compare(blockName, block.toString()) == 0)
              return block;
          break;
      }
      throw new IllegalArgumentException("No Unicode block found for " +
                                         blockName + ".");
    }

    /**
     * Basic Latin.
     * 0x0000 - 0x007F.
     */
    public static final UnicodeBlock BASIC_LATIN
      = new UnicodeBlock(0x0000, 0x007F,
                         "BASIC_LATIN", 
                         "Basic Latin");

    /**
     * Latin-1 Supplement.
     * 0x0080 - 0x00FF.
     */
    public static final UnicodeBlock LATIN_1_SUPPLEMENT
      = new UnicodeBlock(0x0080, 0x00FF,
                         "LATIN_1_SUPPLEMENT", 
                         "Latin-1 Supplement");

    /**
     * Latin Extended-A.
     * 0x0100 - 0x017F.
     */
    public static final UnicodeBlock LATIN_EXTENDED_A
      = new UnicodeBlock(0x0100, 0x017F,
                         "LATIN_EXTENDED_A", 
                         "Latin Extended-A");

    /**
     * Latin Extended-B.
     * 0x0180 - 0x024F.
     */
    public static final UnicodeBlock LATIN_EXTENDED_B
      = new UnicodeBlock(0x0180, 0x024F,
                         "LATIN_EXTENDED_B", 
                         "Latin Extended-B");

    /**
     * IPA Extensions.
     * 0x0250 - 0x02AF.
     */
    public static final UnicodeBlock IPA_EXTENSIONS
      = new UnicodeBlock(0x0250, 0x02AF,
                         "IPA_EXTENSIONS", 
                         "IPA Extensions");

    /**
     * Spacing Modifier Letters.
     * 0x02B0 - 0x02FF.
     */
    public static final UnicodeBlock SPACING_MODIFIER_LETTERS
      = new UnicodeBlock(0x02B0, 0x02FF,
                         "SPACING_MODIFIER_LETTERS", 
                         "Spacing Modifier Letters");

    /**
     * Combining Diacritical Marks.
     * 0x0300 - 0x036F.
     */
    public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS
      = new UnicodeBlock(0x0300, 0x036F,
                         "COMBINING_DIACRITICAL_MARKS", 
                         "Combining Diacritical Marks");

    /**
     * Greek.
     * 0x0370 - 0x03FF.
     */
    public static final UnicodeBlock GREEK
      = new UnicodeBlock(0x0370, 0x03FF,
                         "GREEK", 
                         "Greek");

    /**
     * Cyrillic.
     * 0x0400 - 0x04FF.
     */
    public static final UnicodeBlock CYRILLIC
      = new UnicodeBlock(0x0400, 0x04FF,
                         "CYRILLIC", 
                         "Cyrillic");

    /**
     * Cyrillic Supplementary.
     * 0x0500 - 0x052F.
     * @since 1.5
     */
    public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY
      = new UnicodeBlock(0x0500, 0x052F,
                         "CYRILLIC_SUPPLEMENTARY", 
                         "Cyrillic Supplementary");

    /**
     * Armenian.
     * 0x0530 - 0x058F.
     */
    public static final UnicodeBlock ARMENIAN
      = new UnicodeBlock(0x0530, 0x058F,
                         "ARMENIAN", 
                         "Armenian");

    /**
     * Hebrew.
     * 0x0590 - 0x05FF.
     */
    public static final UnicodeBlock HEBREW
      = new UnicodeBlock(0x0590, 0x05FF,
                         "HEBREW", 
                         "Hebrew");

    /**
     * Arabic.
     * 0x0600 - 0x06FF.
     */
    public static final UnicodeBlock ARABIC
      = new UnicodeBlock(0x0600, 0x06FF,
                         "ARABIC", 
                         "Arabic");

    /**
     * Syriac.
     * 0x0700 - 0x074F.
     * @since 1.4
     */
    public static final UnicodeBlock SYRIAC
      = new UnicodeBlock(0x0700, 0x074F,
                         "SYRIAC", 
                         "Syriac");

    /**
     * Thaana.
     * 0x0780 - 0x07BF.
     * @since 1.4
     */
    public static final UnicodeBlock THAANA
      = new UnicodeBlock(0x0780, 0x07BF,
                         "THAANA", 
                         "Thaana");

    /**
     * Devanagari.
     * 0x0900 - 0x097F.
     */
    public static final UnicodeBlock DEVANAGARI
      = new UnicodeBlock(0x0900, 0x097F,
                         "DEVANAGARI", 
                         "Devanagari");

    /**
     * Bengali.
     * 0x0980 - 0x09FF.
     */
    public static final UnicodeBlock BENGALI
      = new UnicodeBlock(0x0980, 0x09FF,
                         "BENGALI", 
                         "Bengali");

    /**
     * Gurmukhi.
     * 0x0A00 - 0x0A7F.
     */
    public static final UnicodeBlock GURMUKHI
      = new UnicodeBlock(0x0A00, 0x0A7F,
                         "GURMUKHI", 
                         "Gurmukhi");

    /**
     * Gujarati.
     * 0x0A80 - 0x0AFF.
     */
    public static final UnicodeBlock GUJARATI
      = new UnicodeBlock(0x0A80, 0x0AFF,
                         "GUJARATI", 
                         "Gujarati");

    /**
     * Oriya.
     * 0x0B00 - 0x0B7F.
     */
    public static final UnicodeBlock ORIYA
      = new UnicodeBlock(0x0B00, 0x0B7F,
                         "ORIYA", 
                         "Oriya");

    /**
     * Tamil.
     * 0x0B80 - 0x0BFF.
     */
    public static final UnicodeBlock TAMIL
      = new UnicodeBlock(0x0B80, 0x0BFF,
                         "TAMIL", 
                         "Tamil");

    /**
     * Telugu.
     * 0x0C00 - 0x0C7F.
     */
    public static final UnicodeBlock TELUGU
      = new UnicodeBlock(0x0C00, 0x0C7F,
                         "TELUGU", 
                         "Telugu");

    /**
     * Kannada.
     * 0x0C80 - 0x0CFF.
     */
    public static final UnicodeBlock KANNADA
      = new UnicodeBlock(0x0C80, 0x0CFF,
                         "KANNADA", 
                         "Kannada");

    /**
     * Malayalam.
     * 0x0D00 - 0x0D7F.
     */
    public static final UnicodeBlock MALAYALAM
      = new UnicodeBlock(0x0D00, 0x0D7F,
                         "MALAYALAM", 
                         "Malayalam");

    /**
     * Sinhala.
     * 0x0D80 - 0x0DFF.
     * @since 1.4
     */
    public static final UnicodeBlock SINHALA
      = new UnicodeBlock(0x0D80, 0x0DFF,
                         "SINHALA", 
                         "Sinhala");

    /**
     * Thai.
     * 0x0E00 - 0x0E7F.
     */
    public static final UnicodeBlock THAI
      = new UnicodeBlock(0x0E00, 0x0E7F,
                         "THAI", 
                         "Thai");

    /**
     * Lao.
     * 0x0E80 - 0x0EFF.
     */
    public static final UnicodeBlock LAO
      = new UnicodeBlock(0x0E80, 0x0EFF,
                         "LAO", 
                         "Lao");

    /**
     * Tibetan.
     * 0x0F00 - 0x0FFF.
     */
    public static final UnicodeBlock TIBETAN
      = new UnicodeBlock(0x0F00, 0x0FFF,
                         "TIBETAN", 
                         "Tibetan");

    /**
     * Myanmar.
     * 0x1000 - 0x109F.
     * @since 1.4
     */
    public static final UnicodeBlock MYANMAR
      = new UnicodeBlock(0x1000, 0x109F,
                         "MYANMAR", 
                         "Myanmar");

    /**
     * Georgian.
     * 0x10A0 - 0x10FF.
     */
    public static final UnicodeBlock GEORGIAN
      = new UnicodeBlock(0x10A0, 0x10FF,
                         "GEORGIAN", 
                         "Georgian");

    /**
     * Hangul Jamo.
     * 0x1100 - 0x11FF.
     */
    public static final UnicodeBlock HANGUL_JAMO
      = new UnicodeBlock(0x1100, 0x11FF,
                         "HANGUL_JAMO", 
                         "Hangul Jamo");

    /**
     * Ethiopic.
     * 0x1200 - 0x137F.
     * @since 1.4
     */
    public static final UnicodeBlock ETHIOPIC
      = new UnicodeBlock(0x1200, 0x137F,
                         "ETHIOPIC", 
                         "Ethiopic");

    /**
     * Cherokee.
     * 0x13A0 - 0x13FF.
     * @since 1.4
     */
    public static final UnicodeBlock CHEROKEE
      = new UnicodeBlock(0x13A0, 0x13FF,
                         "CHEROKEE", 
                         "Cherokee");

    /**
     * Unified Canadian Aboriginal Syllabics.
     * 0x1400 - 0x167F.
     * @since 1.4
     */
    public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS
      = new UnicodeBlock(0x1400, 0x167F,
                         "UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS", 
                         "Unified Canadian Aboriginal Syllabics");

    /**
     * Ogham.
     * 0x1680 - 0x169F.
     * @since 1.4
     */
    public static final UnicodeBlock OGHAM
      = new UnicodeBlock(0x1680, 0x169F,
                         "OGHAM", 
                         "Ogham");

    /**
     * Runic.
     * 0x16A0 - 0x16FF.
     * @since 1.4
     */
    public static final UnicodeBlock RUNIC
      = new UnicodeBlock(0x16A0, 0x16FF,
                         "RUNIC", 
                         "Runic");

    /**
     * Tagalog.
     * 0x1700 - 0x171F.
     * @since 1.5
     */
    public static final UnicodeBlock TAGALOG
      = new UnicodeBlock(0x1700, 0x171F,
                         "TAGALOG", 
                         "Tagalog");

    /**
     * Hanunoo.
     * 0x1720 - 0x173F.
     * @since 1.5
     */
    public static final UnicodeBlock HANUNOO
      = new UnicodeBlock(0x1720, 0x173F,
                         "HANUNOO", 
                         "Hanunoo");

    /**
     * Buhid.
     * 0x1740 - 0x175F.
     * @since 1.5
     */
    public static final UnicodeBlock BUHID
      = new UnicodeBlock(0x1740, 0x175F,
                         "BUHID", 
                         "Buhid");

    /**
     * Tagbanwa.
     * 0x1760 - 0x177F.
     * @since 1.5
     */
    public static final UnicodeBlock TAGBANWA
      = new UnicodeBlock(0x1760, 0x177F,
                         "TAGBANWA", 
                         "Tagbanwa");

    /**
     * Khmer.
     * 0x1780 - 0x17FF.
     * @since 1.4
     */
    public static final UnicodeBlock KHMER
      = new UnicodeBlock(0x1780, 0x17FF,
                         "KHMER", 
                         "Khmer");

    /**
     * Mongolian.
     * 0x1800 - 0x18AF.
     * @since 1.4
     */
    public static final UnicodeBlock MONGOLIAN
      = new UnicodeBlock(0x1800, 0x18AF,
                         "MONGOLIAN", 
                         "Mongolian");

    /**
     * Limbu.
     * 0x1900 - 0x194F.
     * @since 1.5
     */
    public static final UnicodeBlock LIMBU
      = new UnicodeBlock(0x1900, 0x194F,
                         "LIMBU", 
                         "Limbu");

    /**
     * Tai Le.
     * 0x1950 - 0x197F.
     * @since 1.5
     */
    public static final UnicodeBlock TAI_LE
      = new UnicodeBlock(0x1950, 0x197F,
                         "TAI_LE", 
                         "Tai Le");

    /**
     * Khmer Symbols.
     * 0x19E0 - 0x19FF.
     * @since 1.5
     */
    public static final UnicodeBlock KHMER_SYMBOLS
      = new UnicodeBlock(0x19E0, 0x19FF,
                         "KHMER_SYMBOLS", 
                         "Khmer Symbols");

    /**
     * Phonetic Extensions.
     * 0x1D00 - 0x1D7F.
     * @since 1.5
     */
    public static final UnicodeBlock PHONETIC_EXTENSIONS
      = new UnicodeBlock(0x1D00, 0x1D7F,
                         "PHONETIC_EXTENSIONS", 
                         "Phonetic Extensions");

    /**
     * Latin Extended Additional.
     * 0x1E00 - 0x1EFF.
     */
    public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL
      = new UnicodeBlock(0x1E00, 0x1EFF,
                         "LATIN_EXTENDED_ADDITIONAL", 
                         "Latin Extended Additional");

    /**
     * Greek Extended.
     * 0x1F00 - 0x1FFF.
     */
    public static final UnicodeBlock GREEK_EXTENDED
      = new UnicodeBlock(0x1F00, 0x1FFF,
                         "GREEK_EXTENDED", 
                         "Greek Extended");

    /**
     * General Punctuation.
     * 0x2000 - 0x206F.
     */
    public static final UnicodeBlock GENERAL_PUNCTUATION
      = new UnicodeBlock(0x2000, 0x206F,
                         "GENERAL_PUNCTUATION", 
                         "General Punctuation");

    /**
     * Superscripts and Subscripts.
     * 0x2070 - 0x209F.
     */
    public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS
      = new UnicodeBlock(0x2070, 0x209F,
                         "SUPERSCRIPTS_AND_SUBSCRIPTS", 
                         "Superscripts and Subscripts");

    /**
     * Currency Symbols.
     * 0x20A0 - 0x20CF.
     */
    public static final UnicodeBlock CURRENCY_SYMBOLS
      = new UnicodeBlock(0x20A0, 0x20CF,
                         "CURRENCY_SYMBOLS", 
                         "Currency Symbols");

    /**
     * Combining Marks for Symbols.
     * 0x20D0 - 0x20FF.
     */
    public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS
      = new UnicodeBlock(0x20D0, 0x20FF,
                         "COMBINING_MARKS_FOR_SYMBOLS", 
                         "Combining Marks for Symbols");

    /**
     * Letterlike Symbols.
     * 0x2100 - 0x214F.
     */
    public static final UnicodeBlock LETTERLIKE_SYMBOLS
      = new UnicodeBlock(0x2100, 0x214F,
                         "LETTERLIKE_SYMBOLS", 
                         "Letterlike Symbols");

    /**
     * Number Forms.
     * 0x2150 - 0x218F.
     */
    public static final UnicodeBlock NUMBER_FORMS
      = new UnicodeBlock(0x2150, 0x218F,
                         "NUMBER_FORMS", 
                         "Number Forms");

    /**
     * Arrows.
     * 0x2190 - 0x21FF.
     */
    public static final UnicodeBlock ARROWS
      = new UnicodeBlock(0x2190, 0x21FF,
                         "ARROWS", 
                         "Arrows");

    /**
     * Mathematical Operators.
     * 0x2200 - 0x22FF.
     */
    public static final UnicodeBlock MATHEMATICAL_OPERATORS
      = new UnicodeBlock(0x2200, 0x22FF,
                         "MATHEMATICAL_OPERATORS", 
                         "Mathematical Operators");

    /**
     * Miscellaneous Technical.
     * 0x2300 - 0x23FF.
     */
    public static final UnicodeBlock MISCELLANEOUS_TECHNICAL
      = new UnicodeBlock(0x2300, 0x23FF,
                         "MISCELLANEOUS_TECHNICAL", 
                         "Miscellaneous Technical");

    /**
     * Control Pictures.
     * 0x2400 - 0x243F.
     */
    public static final UnicodeBlock CONTROL_PICTURES
      = new UnicodeBlock(0x2400, 0x243F,
                         "CONTROL_PICTURES", 
                         "Control Pictures");

    /**
     * Optical Character Recognition.
     * 0x2440 - 0x245F.
     */
    public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION
      = new UnicodeBlock(0x2440, 0x245F,
                         "OPTICAL_CHARACTER_RECOGNITION", 
                         "Optical Character Recognition");

    /**
     * Enclosed Alphanumerics.
     * 0x2460 - 0x24FF.
     */
    public static final UnicodeBlock ENCLOSED_ALPHANUMERICS
      = new UnicodeBlock(0x2460, 0x24FF,
                         "ENCLOSED_ALPHANUMERICS", 
                         "Enclosed Alphanumerics");

    /**
     * Box Drawing.
     * 0x2500 - 0x257F.
     */
    public static final UnicodeBlock BOX_DRAWING
      = new UnicodeBlock(0x2500, 0x257F,
                         "BOX_DRAWING", 
                         "Box Drawing");

    /**
     * Block Elements.
     * 0x2580 - 0x259F.
     */
    public static final UnicodeBlock BLOCK_ELEMENTS
      = new UnicodeBlock(0x2580, 0x259F,
                         "BLOCK_ELEMENTS", 
                         "Block Elements");

    /**
     * Geometric Shapes.
     * 0x25A0 - 0x25FF.
     */
    public static final UnicodeBlock GEOMETRIC_SHAPES
      = new UnicodeBlock(0x25A0, 0x25FF,
                         "GEOMETRIC_SHAPES", 
                         "Geometric Shapes");

    /**
     * Miscellaneous Symbols.
     * 0x2600 - 0x26FF.
     */
    public static final UnicodeBlock MISCELLANEOUS_SYMBOLS
      = new UnicodeBlock(0x2600, 0x26FF,
                         "MISCELLANEOUS_SYMBOLS", 
                         "Miscellaneous Symbols");

    /**
     * Dingbats.
     * 0x2700 - 0x27BF.
     */
    public static final UnicodeBlock DINGBATS
      = new UnicodeBlock(0x2700, 0x27BF,
                         "DINGBATS", 
                         "Dingbats");

    /**
     * Miscellaneous Mathematical Symbols-A.
     * 0x27C0 - 0x27EF.
     * @since 1.5
     */
    public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A
      = new UnicodeBlock(0x27C0, 0x27EF,
                         "MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A", 
                         "Miscellaneous Mathematical Symbols-A");

    /**
     * Supplemental Arrows-A.
     * 0x27F0 - 0x27FF.
     * @since 1.5
     */
    public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A
      = new UnicodeBlock(0x27F0, 0x27FF,
                         "SUPPLEMENTAL_ARROWS_A", 
                         "Supplemental Arrows-A");

    /**
     * Braille Patterns.
     * 0x2800 - 0x28FF.
     * @since 1.4
     */
    public static final UnicodeBlock BRAILLE_PATTERNS
      = new UnicodeBlock(0x2800, 0x28FF,
                         "BRAILLE_PATTERNS", 
                         "Braille Patterns");

    /**
     * Supplemental Arrows-B.
     * 0x2900 - 0x297F.
     * @since 1.5
     */
    public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B
      = new UnicodeBlock(0x2900, 0x297F,
                         "SUPPLEMENTAL_ARROWS_B", 
                         "Supplemental Arrows-B");

    /**
     * Miscellaneous Mathematical Symbols-B.
     * 0x2980 - 0x29FF.
     * @since 1.5
     */
    public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B
      = new UnicodeBlock(0x2980, 0x29FF,
                         "MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B", 
                         "Miscellaneous Mathematical Symbols-B");

    /**
     * Supplemental Mathematical Operators.
     * 0x2A00 - 0x2AFF.
     * @since 1.5
     */
    public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS
      = new UnicodeBlock(0x2A00, 0x2AFF,
                         "SUPPLEMENTAL_MATHEMATICAL_OPERATORS", 
                         "Supplemental Mathematical Operators");

    /**
     * Miscellaneous Symbols and Arrows.
     * 0x2B00 - 0x2BFF.
     * @since 1.5
     */
    public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS
      = new UnicodeBlock(0x2B00, 0x2BFF,
                         "MISCELLANEOUS_SYMBOLS_AND_ARROWS", 
                         "Miscellaneous Symbols and Arrows");

    /**
     * CJK Radicals Supplement.
     * 0x2E80 - 0x2EFF.
     * @since 1.4
     */
    public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT
      = new UnicodeBlock(0x2E80, 0x2EFF,
                         "CJK_RADICALS_SUPPLEMENT", 
                         "CJK Radicals Supplement");

    /**
     * Kangxi Radicals.
     * 0x2F00 - 0x2FDF.
     * @since 1.4
     */
    public static final UnicodeBlock KANGXI_RADICALS
      = new UnicodeBlock(0x2F00, 0x2FDF,
                         "KANGXI_RADICALS", 
                         "Kangxi Radicals");

    /**
     * Ideographic Description Characters.
     * 0x2FF0 - 0x2FFF.
     * @since 1.4
     */
    public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS
      = new UnicodeBlock(0x2FF0, 0x2FFF,
                         "IDEOGRAPHIC_DESCRIPTION_CHARACTERS", 
                         "Ideographic Description Characters");

    /**
     * CJK Symbols and Punctuation.
     * 0x3000 - 0x303F.
     */
    public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION
      = new UnicodeBlock(0x3000, 0x303F,
                         "CJK_SYMBOLS_AND_PUNCTUATION", 
                         "CJK Symbols and Punctuation");

    /**
     * Hiragana.
     * 0x3040 - 0x309F.
     */
    public static final UnicodeBlock HIRAGANA
      = new UnicodeBlock(0x3040, 0x309F,
                         "HIRAGANA", 
                         "Hiragana");

    /**
     * Katakana.
     * 0x30A0 - 0x30FF.
     */
    public static final UnicodeBlock KATAKANA
      = new UnicodeBlock(0x30A0, 0x30FF,
                         "KATAKANA", 
                         "Katakana");

    /**
     * Bopomofo.
     * 0x3100 - 0x312F.
     */
    public static final UnicodeBlock BOPOMOFO
      = new UnicodeBlock(0x3100, 0x312F,
                         "BOPOMOFO", 
                         "Bopomofo");

    /**
     * Hangul Compatibility Jamo.
     * 0x3130 - 0x318F.
     */
    public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO
      = new UnicodeBlock(0x3130, 0x318F,
                         "HANGUL_COMPATIBILITY_JAMO", 
                         "Hangul Compatibility Jamo");

    /**
     * Kanbun.
     * 0x3190 - 0x319F.
     */
    public static final UnicodeBlock KANBUN
      = new UnicodeBlock(0x3190, 0x319F,
                         "KANBUN", 
                         "Kanbun");

    /**
     * Bopomofo Extended.
     * 0x31A0 - 0x31BF.
     * @since 1.4
     */
    public static final UnicodeBlock BOPOMOFO_EXTENDED
      = new UnicodeBlock(0x31A0, 0x31BF,
                         "BOPOMOFO_EXTENDED", 
                         "Bopomofo Extended");

    /**
     * Katakana Phonetic Extensions.
     * 0x31F0 - 0x31FF.
     * @since 1.5
     */
    public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS
      = new UnicodeBlock(0x31F0, 0x31FF,
                         "KATAKANA_PHONETIC_EXTENSIONS", 
                         "Katakana Phonetic Extensions");

    /**
     * Enclosed CJK Letters and Months.
     * 0x3200 - 0x32FF.
     */
    public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS
      = new UnicodeBlock(0x3200, 0x32FF,
                         "ENCLOSED_CJK_LETTERS_AND_MONTHS", 
                         "Enclosed CJK Letters and Months");

    /**
     * CJK Compatibility.
     * 0x3300 - 0x33FF.
     */
    public static final UnicodeBlock CJK_COMPATIBILITY
      = new UnicodeBlock(0x3300, 0x33FF,
                         "CJK_COMPATIBILITY", 
                         "CJK Compatibility");

    /**
     * CJK Unified Ideographs Extension A.
     * 0x3400 - 0x4DBF.
     * @since 1.4
     */
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
      = new UnicodeBlock(0x3400, 0x4DBF,
                         "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A", 
                         "CJK Unified Ideographs Extension A");

    /**
     * Yijing Hexagram Symbols.
     * 0x4DC0 - 0x4DFF.
     * @since 1.5
     */
    public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS
      = new UnicodeBlock(0x4DC0, 0x4DFF,
                         "YIJING_HEXAGRAM_SYMBOLS", 
                         "Yijing Hexagram Symbols");

    /**
     * CJK Unified Ideographs.
     * 0x4E00 - 0x9FFF.
     */
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS
      = new UnicodeBlock(0x4E00, 0x9FFF,
                         "CJK_UNIFIED_IDEOGRAPHS", 
                         "CJK Unified Ideographs");

    /**
     * Yi Syllables.
     * 0xA000 - 0xA48F.
     * @since 1.4
     */
    public static final UnicodeBlock YI_SYLLABLES
      = new UnicodeBlock(0xA000, 0xA48F,
                         "YI_SYLLABLES", 
                         "Yi Syllables");

    /**
     * Yi Radicals.
     * 0xA490 - 0xA4CF.
     * @since 1.4
     */
    public static final UnicodeBlock YI_RADICALS
      = new UnicodeBlock(0xA490, 0xA4CF,
                         "YI_RADICALS", 
                         "Yi Radicals");

    /**
     * Hangul Syllables.
     * 0xAC00 - 0xD7AF.
     */
    public static final UnicodeBlock HANGUL_SYLLABLES
      = new UnicodeBlock(0xAC00, 0xD7AF,
                         "HANGUL_SYLLABLES", 
                         "Hangul Syllables");

    /**
     * High Surrogates.
     * 0xD800 - 0xDB7F.
     * @since 1.5
     */
    public static final UnicodeBlock HIGH_SURROGATES
      = new UnicodeBlock(0xD800, 0xDB7F,
                         "HIGH_SURROGATES", 
                         "High Surrogates");

    /**
     * High Private Use Surrogates.
     * 0xDB80 - 0xDBFF.
     * @since 1.5
     */
    public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES
      = new UnicodeBlock(0xDB80, 0xDBFF,
                         "HIGH_PRIVATE_USE_SURROGATES", 
                         "High Private Use Surrogates");

    /**
     * Low Surrogates.
     * 0xDC00 - 0xDFFF.
     * @since 1.5
     */
    public static final UnicodeBlock LOW_SURROGATES
      = new UnicodeBlock(0xDC00, 0xDFFF,
                         "LOW_SURROGATES", 
                         "Low Surrogates");

    /**
     * Private Use Area.
     * 0xE000 - 0xF8FF.
     */
    public static final UnicodeBlock PRIVATE_USE_AREA
      = new UnicodeBlock(0xE000, 0xF8FF,
                         "PRIVATE_USE_AREA", 
                         "Private Use Area");

    /**
     * CJK Compatibility Ideographs.
     * 0xF900 - 0xFAFF.
     */
    public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS
      = new UnicodeBlock(0xF900, 0xFAFF,
                         "CJK_COMPATIBILITY_IDEOGRAPHS", 
                         "CJK Compatibility Ideographs");

    /**
     * Alphabetic Presentation Forms.
     * 0xFB00 - 0xFB4F.
     */
    public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS
      = new UnicodeBlock(0xFB00, 0xFB4F,
                         "ALPHABETIC_PRESENTATION_FORMS", 
                         "Alphabetic Presentation Forms");

    /**
     * Arabic Presentation Forms-A.
     * 0xFB50 - 0xFDFF.
     */
    public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A
      = new UnicodeBlock(0xFB50, 0xFDFF,
                         "ARABIC_PRESENTATION_FORMS_A", 
                         "Arabic Presentation Forms-A");

    /**
     * Variation Selectors.
     * 0xFE00 - 0xFE0F.
     * @since 1.5
     */
    public static final UnicodeBlock VARIATION_SELECTORS
      = new UnicodeBlock(0xFE00, 0xFE0F,
                         "VARIATION_SELECTORS", 
                         "Variation Selectors");

    /**
     * Combining Half Marks.
     * 0xFE20 - 0xFE2F.
     */
    public static final UnicodeBlock COMBINING_HALF_MARKS
      = new UnicodeBlock(0xFE20, 0xFE2F,
                         "COMBINING_HALF_MARKS", 
                         "Combining Half Marks");

    /**
     * CJK Compatibility Forms.
     * 0xFE30 - 0xFE4F.
     */
    public static final UnicodeBlock CJK_COMPATIBILITY_FORMS
      = new UnicodeBlock(0xFE30, 0xFE4F,
                         "CJK_COMPATIBILITY_FORMS", 
                         "CJK Compatibility Forms");

    /**
     * Small Form Variants.
     * 0xFE50 - 0xFE6F.
     */
    public static final UnicodeBlock SMALL_FORM_VARIANTS
      = new UnicodeBlock(0xFE50, 0xFE6F,
                         "SMALL_FORM_VARIANTS", 
                         "Small Form Variants");

    /**
     * Arabic Presentation Forms-B.
     * 0xFE70 - 0xFEFF.
     */
    public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B
      = new UnicodeBlock(0xFE70, 0xFEFF,
                         "ARABIC_PRESENTATION_FORMS_B", 
                         "Arabic Presentation Forms-B");

    /**
     * Halfwidth and Fullwidth Forms.
     * 0xFF00 - 0xFFEF.
     */
    public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS
      = new UnicodeBlock(0xFF00, 0xFFEF,
                         "HALFWIDTH_AND_FULLWIDTH_FORMS", 
                         "Halfwidth and Fullwidth Forms");

    /**
     * Specials.
     * 0xFFF0 - 0xFFFF.
     */
    public static final UnicodeBlock SPECIALS
      = new UnicodeBlock(0xFFF0, 0xFFFF,
                         "SPECIALS", 
                         "Specials");

    /**
     * Linear B Syllabary.
     * 0x10000 - 0x1007F.
     * @since 1.5
     */
    public static final UnicodeBlock LINEAR_B_SYLLABARY
      = new UnicodeBlock(0x10000, 0x1007F,
                         "LINEAR_B_SYLLABARY", 
                         "Linear B Syllabary");

    /**
     * Linear B Ideograms.
     * 0x10080 - 0x100FF.
     * @since 1.5
     */
    public static final UnicodeBlock LINEAR_B_IDEOGRAMS
      = new UnicodeBlock(0x10080, 0x100FF,
                         "LINEAR_B_IDEOGRAMS", 
                         "Linear B Ideograms");

    /**
     * Aegean Numbers.
     * 0x10100 - 0x1013F.
     * @since 1.5
     */
    public static final UnicodeBlock AEGEAN_NUMBERS
      = new UnicodeBlock(0x10100, 0x1013F,
                         "AEGEAN_NUMBERS", 
                         "Aegean Numbers");

    /**
     * Old Italic.
     * 0x10300 - 0x1032F.
     * @since 1.5
     */
    public static final UnicodeBlock OLD_ITALIC
      = new UnicodeBlock(0x10300, 0x1032F,
                         "OLD_ITALIC", 
                         "Old Italic");

    /**
     * Gothic.
     * 0x10330 - 0x1034F.
     * @since 1.5
     */
    public static final UnicodeBlock GOTHIC
      = new UnicodeBlock(0x10330, 0x1034F,
                         "GOTHIC", 
                         "Gothic");

    /**
     * Ugaritic.
     * 0x10380 - 0x1039F.
     * @since 1.5
     */
    public static final UnicodeBlock UGARITIC
      = new UnicodeBlock(0x10380, 0x1039F,
                         "UGARITIC", 
                         "Ugaritic");

    /**
     * Deseret.
     * 0x10400 - 0x1044F.
     * @since 1.5
     */
    public static final UnicodeBlock DESERET
      = new UnicodeBlock(0x10400, 0x1044F,
                         "DESERET", 
                         "Deseret");

    /**
     * Shavian.
     * 0x10450 - 0x1047F.
     * @since 1.5
     */
    public static final UnicodeBlock SHAVIAN
      = new UnicodeBlock(0x10450, 0x1047F,
                         "SHAVIAN", 
                         "Shavian");

    /**
     * Osmanya.
     * 0x10480 - 0x104AF.
     * @since 1.5
     */
    public static final UnicodeBlock OSMANYA
      = new UnicodeBlock(0x10480, 0x104AF,
                         "OSMANYA", 
                         "Osmanya");

    /**
     * Cypriot Syllabary.
     * 0x10800 - 0x1083F.
     * @since 1.5
     */
    public static final UnicodeBlock CYPRIOT_SYLLABARY
      = new UnicodeBlock(0x10800, 0x1083F,
                         "CYPRIOT_SYLLABARY", 
                         "Cypriot Syllabary");

    /**
     * Byzantine Musical Symbols.
     * 0x1D000 - 0x1D0FF.
     * @since 1.5
     */
    public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS
      = new UnicodeBlock(0x1D000, 0x1D0FF,
                         "BYZANTINE_MUSICAL_SYMBOLS", 
                         "Byzantine Musical Symbols");

    /**
     * Musical Symbols.
     * 0x1D100 - 0x1D1FF.
     * @since 1.5
     */
    public static final UnicodeBlock MUSICAL_SYMBOLS
      = new UnicodeBlock(0x1D100, 0x1D1FF,
                         "MUSICAL_SYMBOLS", 
                         "Musical Symbols");

    /**
     * Tai Xuan Jing Symbols.
     * 0x1D300 - 0x1D35F.
     * @since 1.5
     */
    public static final UnicodeBlock TAI_XUAN_JING_SYMBOLS
      = new UnicodeBlock(0x1D300, 0x1D35F,
                         "TAI_XUAN_JING_SYMBOLS", 
                         "Tai Xuan Jing Symbols");

    /**
     * Mathematical Alphanumeric Symbols.
     * 0x1D400 - 0x1D7FF.
     * @since 1.5
     */
    public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS
      = new UnicodeBlock(0x1D400, 0x1D7FF,
                         "MATHEMATICAL_ALPHANUMERIC_SYMBOLS", 
                         "Mathematical Alphanumeric Symbols");

    /**
     * CJK Unified Ideographs Extension B.
     * 0x20000 - 0x2A6DF.
     * @since 1.5
     */
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
      = new UnicodeBlock(0x20000, 0x2A6DF,
                         "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B", 
                         "CJK Unified Ideographs Extension B");

    /**
     * CJK Compatibility Ideographs Supplement.
     * 0x2F800 - 0x2FA1F.
     * @since 1.5
     */
    public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT
      = new UnicodeBlock(0x2F800, 0x2FA1F,
                         "CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT", 
                         "CJK Compatibility Ideographs Supplement");

    /**
     * Tags.
     * 0xE0000 - 0xE007F.
     * @since 1.5
     */
    public static final UnicodeBlock TAGS
      = new UnicodeBlock(0xE0000, 0xE007F,
                         "TAGS", 
                         "Tags");

    /**
     * Variation Selectors Supplement.
     * 0xE0100 - 0xE01EF.
     * @since 1.5
     */
    public static final UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT
      = new UnicodeBlock(0xE0100, 0xE01EF,
                         "VARIATION_SELECTORS_SUPPLEMENT", 
                         "Variation Selectors Supplement");

    /**
     * Supplementary Private Use Area-A.
     * 0xF0000 - 0xFFFFF.
     * @since 1.5
     */
    public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A
      = new UnicodeBlock(0xF0000, 0xFFFFF,
                         "SUPPLEMENTARY_PRIVATE_USE_AREA_A", 
                         "Supplementary Private Use Area-A");

    /**
     * Supplementary Private Use Area-B.
     * 0x100000 - 0x10FFFF.
     * @since 1.5
     */
    public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B
      = new UnicodeBlock(0x100000, 0x10FFFF,
                         "SUPPLEMENTARY_PRIVATE_USE_AREA_B", 
                         "Supplementary Private Use Area-B");

    /**
     * Surrogates Area.
     * 'D800' - 'DFFF'.
     * @deprecated As of 1.5, the three areas, 
     * <a href="#HIGH_SURROGATES">HIGH_SURROGATES</a>,
     * <a href="#HIGH_PRIVATE_USE_SURROGATES">HIGH_PRIVATE_USE_SURROGATES</a>
     * and <a href="#LOW_SURROGATES">LOW_SURROGATES</a>, as defined
     * by the Unicode standard, should be used in preference to
     * this.  These are also returned from calls to <code>of(int)</code>
     * and <code>of(char)</code>.
     */
    @Deprecated
    public static final UnicodeBlock SURROGATES_AREA
      = new UnicodeBlock(0xD800, 0xDFFF,
                         "SURROGATES_AREA",
			 "Surrogates Area");

    /**
     * The defined subsets.
     */
    private static final UnicodeBlock sets[] = {
      BASIC_LATIN,
      LATIN_1_SUPPLEMENT,
      LATIN_EXTENDED_A,
      LATIN_EXTENDED_B,
      IPA_EXTENSIONS,
      SPACING_MODIFIER_LETTERS,
      COMBINING_DIACRITICAL_MARKS,
      GREEK,
      CYRILLIC,
      CYRILLIC_SUPPLEMENTARY,
      ARMENIAN,
      HEBREW,
      ARABIC,
      SYRIAC,
      THAANA,
      DEVANAGARI,
      BENGALI,
      GURMUKHI,
      GUJARATI,
      ORIYA,
      TAMIL,
      TELUGU,
      KANNADA,
      MALAYALAM,
      SINHALA,
      THAI,
      LAO,
      TIBETAN,
      MYANMAR,
      GEORGIAN,
      HANGUL_JAMO,
      ETHIOPIC,
      CHEROKEE,
      UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS,
      OGHAM,
      RUNIC,
      TAGALOG,
      HANUNOO,
      BUHID,
      TAGBANWA,
      KHMER,
      MONGOLIAN,
      LIMBU,
      TAI_LE,
      KHMER_SYMBOLS,
      PHONETIC_EXTENSIONS,
      LATIN_EXTENDED_ADDITIONAL,
      GREEK_EXTENDED,
      GENERAL_PUNCTUATION,
      SUPERSCRIPTS_AND_SUBSCRIPTS,
      CURRENCY_SYMBOLS,
      COMBINING_MARKS_FOR_SYMBOLS,
      LETTERLIKE_SYMBOLS,
      NUMBER_FORMS,
      ARROWS,
      MATHEMATICAL_OPERATORS,
      MISCELLANEOUS_TECHNICAL,
      CONTROL_PICTURES,
      OPTICAL_CHARACTER_RECOGNITION,
      ENCLOSED_ALPHANUMERICS,
      BOX_DRAWING,
      BLOCK_ELEMENTS,
      GEOMETRIC_SHAPES,
      MISCELLANEOUS_SYMBOLS,
      DINGBATS,
      MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A,
      SUPPLEMENTAL_ARROWS_A,
      BRAILLE_PATTERNS,
      SUPPLEMENTAL_ARROWS_B,
      MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B,
      SUPPLEMENTAL_MATHEMATICAL_OPERATORS,
      MISCELLANEOUS_SYMBOLS_AND_ARROWS,
      CJK_RADICALS_SUPPLEMENT,
      KANGXI_RADICALS,
      IDEOGRAPHIC_DESCRIPTION_CHARACTERS,
      CJK_SYMBOLS_AND_PUNCTUATION,
      HIRAGANA,
      KATAKANA,
      BOPOMOFO,
      HANGUL_COMPATIBILITY_JAMO,
      KANBUN,
      BOPOMOFO_EXTENDED,
      KATAKANA_PHONETIC_EXTENSIONS,
      ENCLOSED_CJK_LETTERS_AND_MONTHS,
      CJK_COMPATIBILITY,
      CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
      YIJING_HEXAGRAM_SYMBOLS,
      CJK_UNIFIED_IDEOGRAPHS,
      YI_SYLLABLES,
      YI_RADICALS,
      HANGUL_SYLLABLES,
      HIGH_SURROGATES,
      HIGH_PRIVATE_USE_SURROGATES,
      LOW_SURROGATES,
      PRIVATE_USE_AREA,
      CJK_COMPATIBILITY_IDEOGRAPHS,
      ALPHABETIC_PRESENTATION_FORMS,
      ARABIC_PRESENTATION_FORMS_A,
      VARIATION_SELECTORS,
      COMBINING_HALF_MARKS,
      CJK_COMPATIBILITY_FORMS,
      SMALL_FORM_VARIANTS,
      ARABIC_PRESENTATION_FORMS_B,
      HALFWIDTH_AND_FULLWIDTH_FORMS,
      SPECIALS,
      LINEAR_B_SYLLABARY,
      LINEAR_B_IDEOGRAMS,
      AEGEAN_NUMBERS,
      OLD_ITALIC,
      GOTHIC,
      UGARITIC,
      DESERET,
      SHAVIAN,
      OSMANYA,
      CYPRIOT_SYLLABARY,
      BYZANTINE_MUSICAL_SYMBOLS,
      MUSICAL_SYMBOLS,
      TAI_XUAN_JING_SYMBOLS,
      MATHEMATICAL_ALPHANUMERIC_SYMBOLS,
      CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
      CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT,
      TAGS,
      VARIATION_SELECTORS_SUPPLEMENT,
      SUPPLEMENTARY_PRIVATE_USE_AREA_A,
      SUPPLEMENTARY_PRIVATE_USE_AREA_B,
    };
  } // class UnicodeBlock

  /**
   * The immutable value of this Character.
   *
   * @serial the value of this Character
   */
  private final char value;

  /**
   * Compatible with JDK 1.0+.
   */
  private static final long serialVersionUID = 3786198910865385080L;

  /**
   * Smallest value allowed for radix arguments in Java. This value is 2.
   *
   * @see #digit(char, int)
   * @see #forDigit(int, int)
   * @see Integer#toString(int, int)
   * @see Integer#valueOf(String)
   */
  public static final int MIN_RADIX = 2;

  /**
   * Largest value allowed for radix arguments in Java. This value is 36.
   *
   * @see #digit(char, int)
   * @see #forDigit(int, int)
   * @see Integer#toString(int, int)
   * @see Integer#valueOf(String)
   */
  public static final int MAX_RADIX = 36;

  /**
   * The minimum value the char data type can hold.
   * This value is <code>'\\u0000'</code>.
   */
  public static final char MIN_VALUE = '\u0000';

  /**
   * The maximum value the char data type can hold.
   * This value is <code>'\\uFFFF'</code>.
   */
  public static final char MAX_VALUE = '\uFFFF';

  /**
   * The minimum Unicode 4.0 code point.  This value is <code>0</code>.
   */
  public static final int MIN_CODE_POINT = 0;

  /**
   * The maximum Unicode 4.0 code point, which is greater than the range
   * of the char data type.
   * This value is <code>0x10FFFF</code>.
   */
  public static final int MAX_CODE_POINT = 0x10FFFF;

  /**
   * The minimum Unicode high surrogate code unit, or
   * <emph>leading-surrogate</emph>, in the UTF-16 character encoding.
   * This value is <code>'\uD800'</code>.
   */
  public static final char MIN_HIGH_SURROGATE = '\uD800';

  /**
   * The maximum Unicode high surrogate code unit, or
   * <emph>leading-surrogate</emph>, in the UTF-16 character encoding.
   * This value is <code>'\uDBFF'</code>.
   */
  public static final char MAX_HIGH_SURROGATE = '\uDBFF';

  /**
   * The minimum Unicode low surrogate code unit, or
   * <emph>trailing-surrogate</emph>, in the UTF-16 character encoding.
   * This value is <code>'\uDC00'</code>.
   */
  public static final char MIN_LOW_SURROGATE = '\uDC00';

  /**
   * The maximum Unicode low surrogate code unit, or
   * <emph>trailing-surrogate</emph>, in the UTF-16 character encoding.
   * This value is <code>'\uDFFF'</code>.
   */
  public static final char MAX_LOW_SURROGATE = '\uDFFF';  

  /**
   * The minimum Unicode surrogate code unit in the UTF-16 character encoding.
   * This value is <code>'\uD800'</code>.
   */
  public static final char MIN_SURROGATE = '\uD800';

  /**
   * The maximum Unicode surrogate code unit in the UTF-16 character encoding.
   * This value is <code>'\uDFFF'</code>.
   */
  public static final char MAX_SURROGATE = '\uDFFF';

  /**
   * The lowest possible supplementary Unicode code point (the first code
   * point outside the basic multilingual plane (BMP)).
   * This value is <code>0x10000</code>.
   */ 
  public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x10000;

  /**
   * Class object representing the primitive char data type.
   *
   * @since 1.1
   */
  public static final Class<Character> TYPE = (Class<Character>) VMClassLoader.getPrimitiveClass('C');

  /**
   * The number of bits needed to represent a <code>char</code>.
   * @since 1.5
   */
  public static final int SIZE = 16;

  // This caches some Character values, and is used by boxing
  // conversions via valueOf().  We must cache at least 0..127;
  // this constant controls how much we actually cache.
  private static final int MAX_CACHE = 127;
  private static Character[] charCache = new Character[MAX_CACHE + 1];

  /**
   * Lu = Letter, Uppercase (Informative).
   *
   * @since 1.1
   */
  public static final byte UPPERCASE_LETTER = 1;

  /**
   * Ll = Letter, Lowercase (Informative).
   *
   * @since 1.1
   */
  public static final byte LOWERCASE_LETTER = 2;

  /**
   * Lt = Letter, Titlecase (Informative).
   *
   * @since 1.1
   */
  public static final byte TITLECASE_LETTER = 3;

  /**
   * Mn = Mark, Non-Spacing (Normative).
   *
   * @since 1.1
   */
  public static final byte NON_SPACING_MARK = 6;

  /**
   * Mc = Mark, Spacing Combining (Normative).
   *
   * @since 1.1
   */
  public static final byte COMBINING_SPACING_MARK = 8;

  /**
   * Me = Mark, Enclosing (Normative).
   *
   * @since 1.1
   */
  public static final byte ENCLOSING_MARK = 7;

  /**
   * Nd = Number, Decimal Digit (Normative).
   *
   * @since 1.1
   */
  public static final byte DECIMAL_DIGIT_NUMBER = 9;

  /**
   * Nl = Number, Letter (Normative).
   *
   * @since 1.1
   */
  public static final byte LETTER_NUMBER = 10;

  /**
   * No = Number, Other (Normative).
   *
   * @since 1.1
   */
  public static final byte OTHER_NUMBER = 11;

  /**
   * Zs = Separator, Space (Normative).
   *
   * @since 1.1
   */
  public static final byte SPACE_SEPARATOR = 12;

  /**
   * Zl = Separator, Line (Normative).
   *
   * @since 1.1
   */
  public static final byte LINE_SEPARATOR = 13;

  /**
   * Zp = Separator, Paragraph (Normative).
   *
   * @since 1.1
   */
  public static final byte PARAGRAPH_SEPARATOR = 14;

  /**
   * Cc = Other, Control (Normative).
   *
   * @since 1.1
   */
  public static final byte CONTROL = 15;

  /**
   * Cf = Other, Format (Normative).
   *
   * @since 1.1
   */
  public static final byte FORMAT = 16;

  /**
   * Cs = Other, Surrogate (Normative).
   *
   * @since 1.1
   */
  public static final byte SURROGATE = 19;

  /**
   * Co = Other, Private Use (Normative).
   *
   * @since 1.1
   */
  public static final byte PRIVATE_USE = 18;

  /**
   * Cn = Other, Not Assigned (Normative).
   *
   * @since 1.1
   */
  public static final byte UNASSIGNED = 0;

  /**
   * Lm = Letter, Modifier (Informative).
   *
   * @since 1.1
   */
  public static final byte MODIFIER_LETTER = 4;

  /**
   * Lo = Letter, Other (Informative).
   *
   * @since 1.1
   */
  public static final byte OTHER_LETTER = 5;

  /**
   * Pc = Punctuation, Connector (Informative).
   *
   * @since 1.1
   */
  public static final byte CONNECTOR_PUNCTUATION = 23;

  /**
   * Pd = Punctuation, Dash (Informative).
   *
   * @since 1.1
   */
  public static final byte DASH_PUNCTUATION = 20;

  /**
   * Ps = Punctuation, Open (Informative).
   *
   * @since 1.1
   */
  public static final byte START_PUNCTUATION = 21;

  /**
   * Pe = Punctuation, Close (Informative).
   *
   * @since 1.1
   */
  public static final byte END_PUNCTUATION = 22;

  /**
   * Pi = Punctuation, Initial Quote (Informative).
   *
   * @since 1.4
   */
  public static final byte INITIAL_QUOTE_PUNCTUATION = 29;

  /**
   * Pf = Punctuation, Final Quote (Informative).
   *
   * @since 1.4
   */
  public static final byte FINAL_QUOTE_PUNCTUATION = 30;

  /**
   * Po = Punctuation, Other (Informative).
   *
   * @since 1.1
   */
  public static final byte OTHER_PUNCTUATION = 24;

  /**
   * Sm = Symbol, Math (Informative).
   *
   * @since 1.1
   */
  public static final byte MATH_SYMBOL = 25;

  /**
   * Sc = Symbol, Currency (Informative).
   *
   * @since 1.1
   */
  public static final byte CURRENCY_SYMBOL = 26;

  /**
   * Sk = Symbol, Modifier (Informative).
   *
   * @since 1.1
   */
  public static final byte MODIFIER_SYMBOL = 27;

  /**
   * So = Symbol, Other (Informative).
   *
   * @since 1.1
   */
  public static final byte OTHER_SYMBOL = 28;

  /**
   * Undefined bidirectional character type. Undefined char values have
   * undefined directionality in the Unicode specification.
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_UNDEFINED = -1;

  /**
   * Strong bidirectional character type "L".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = 0;

  /**
   * Strong bidirectional character type "R".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = 1;

  /**
   * Strong bidirectional character type "AL".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;

  /**
   * Weak bidirectional character type "EN".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = 3;

  /**
   * Weak bidirectional character type "ES".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;

  /**
   * Weak bidirectional character type "ET".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;

  /**
   * Weak bidirectional character type "AN".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_ARABIC_NUMBER = 6;

  /**
   * Weak bidirectional character type "CS".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;

  /**
   * Weak bidirectional character type "NSM".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_NONSPACING_MARK = 8;

  /**
   * Weak bidirectional character type "BN".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;

  /**
   * Neutral bidirectional character type "B".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;

  /**
   * Neutral bidirectional character type "S".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = 11;

  /**
   * Strong bidirectional character type "WS".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_WHITESPACE = 12;

  /**
   * Neutral bidirectional character type "ON".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_OTHER_NEUTRALS = 13;

  /**
   * Strong bidirectional character type "LRE".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;

  /**
   * Strong bidirectional character type "LRO".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;

  /**
   * Strong bidirectional character type "RLE".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;

  /**
   * Strong bidirectional character type "RLO".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;

  /**
   * Weak bidirectional character type "PDF".
   *
   * @since 1.4
   */
  public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;

  /**
   * Stores unicode block offset lookup table. Exploit package visibility of
   * String.value to avoid copying the array.
   * @see #readChar(char)
   * @see CharData#BLOCKS
   */
  private static final char[] blocks = String.zeroBasedStringValue(CharData.BLOCKS);

  /**
   * Stores unicode attribute offset lookup table. Exploit package visibility
   * of String.value to avoid copying the array.
   * @see CharData#DATA
   */
  private static final char[] data = String.zeroBasedStringValue(CharData.DATA);

  /**
   * Stores unicode numeric value attribute table. Exploit package visibility
   * of String.value to avoid copying the array.
   * @see CharData#NUM_VALUE
   */
  private static final char[] numValue
	  = String.zeroBasedStringValue(CharData.NUM_VALUE);

  /**
   * Stores unicode uppercase attribute table. Exploit package visibility
   * of String.value to avoid copying the array.
   * @see CharData#UPPER
   */
  private static final char[] upper = String.zeroBasedStringValue(CharData.UPPER);

  /**
   * Stores unicode lowercase attribute table. Exploit package visibility
   * of String.value to avoid copying the array.
   * @see CharData#LOWER
   */
  private static final char[] lower = String.zeroBasedStringValue(CharData.LOWER);

  /**
   * Stores unicode direction attribute table. Exploit package visibility
   * of String.value to avoid copying the array.
   * @see CharData#DIRECTION
   */
  // Package visible for use by String.
  static final char[] direction = String.zeroBasedStringValue(CharData.DIRECTION);

  /**
   * Stores unicode titlecase table. Exploit package visibility of
   * String.value to avoid copying the array.
   * @see CharData#TITLE
   */
  private static final char[] title = String.zeroBasedStringValue(CharData.TITLE);

  /**
   * Mask for grabbing the type out of the contents of data.
   * @see CharData#DATA
   */
  private static final int TYPE_MASK = 0x1F;

  /**
   * Mask for grabbing the non-breaking space flag out of the contents of
   * data.
   * @see CharData#DATA
   */
  private static final int NO_BREAK_MASK = 0x20;

  /**
   * Mask for grabbing the mirrored directionality flag out of the contents
   * of data.
   * @see CharData#DATA
   */
  private static final int MIRROR_MASK = 0x40;

  /**
   * Grabs an attribute offset from the Unicode attribute database. The lower
   * 5 bits are the character type, the next 2 bits are flags, and the top
   * 9 bits are the offset into the attribute tables.
   *
   * @param ch the character to look up
   * @return the character's attribute offset and type
   * @see #TYPE_MASK
   * @see #NO_BREAK_MASK
   * @see #MIRROR_MASK
   * @see CharData#DATA
   * @see CharData#SHIFT
   */
  // Package visible for use in String.
  static char readChar(char ch)
  {
    // Perform 16-bit addition to find the correct entry in data.
    return data[(char) (blocks[ch >> CharData.SHIFT] + ch)];
  }

  /**
   * Wraps up a character.
   *
   * @param value the character to wrap
   */
  public Character(char value)
  {
    this.value = value;
  }

  /**
   * Returns the character which has been wrapped by this class.
   *
   * @return the character wrapped
   */
  public char charValue()
  {
    return value;
  }

  /**
   * Returns the numerical value (unsigned) of the wrapped character.
   * Range of returned values: 0x0000-0xFFFF.
   *
   * @return the value of the wrapped character
   */
  public int hashCode()
  {
    return value;
  }

  /**
   * Determines if an object is equal to this object. This is only true for
   * another Character object wrapping the same value.
   *
   * @param o object to compare
   * @return true if o is a Character with the same value
   */
  public boolean equals(Object o)
  {
    return o instanceof Character && value == ((Character) o).value;
  }

  /**
   * Converts the wrapped character into a String.
   *
   * @return a String containing one character -- the wrapped character
   *         of this instance
   */
  public String toString()
  {
    // Package constructor avoids an array copy.
    return new String(new char[] { value }, 0, 1, true);
  }

  /**
   * Returns a String of length 1 representing the specified character.
   *
   * @param ch the character to convert
   * @return a String containing the character
   * @since 1.4
   */
  public static String toString(char ch)
  {
    // Package constructor avoids an array copy.
    return new String(new char[] { ch }, 0, 1, true);
  }

  /**
   * Determines if a character is a Unicode lowercase letter. For example,
   * <code>'a'</code> is lowercase.
   * <br>
   * lowercase = [Ll]
   *
   * @param ch character to test
   * @return true if ch is a Unicode lowercase letter, else false
   * @see #isUpperCase(char)
   * @see #isTitleCase(char)
   * @see #toLowerCase(char)
   * @see #getType(char)
   */
  public static boolean isLowerCase(char ch)
  {
    return getType(ch) == LOWERCASE_LETTER;
  }

  /**
   * Determines if a character is a Unicode uppercase letter. For example,
   * <code>'A'</code> is uppercase.
   * <br>
   * uppercase = [Lu]
   *
   * @param ch character to test
   * @return true if ch is a Unicode uppercase letter, else false
   * @see #isLowerCase(char)
   * @see #isTitleCase(char)
   * @see #toUpperCase(char)
   * @see #getType(char)
   */
  public static boolean isUpperCase(char ch)
  {
    return getType(ch) == UPPERCASE_LETTER;
  }

  /**
   * Determines if a character is a Unicode titlecase letter. For example,
   * the character "Lj" (Latin capital L with small letter j) is titlecase.
   * <br>
   * titlecase = [Lt]
   *
   * @param ch character to test
   * @return true if ch is a Unicode titlecase letter, else false
   * @see #isLowerCase(char)
   * @see #isUpperCase(char)
   * @see #toTitleCase(char)
   * @see #getType(char)
   */
  public static boolean isTitleCase(char ch)
  {
    return getType(ch) == TITLECASE_LETTER;
  }

  /**
   * Determines if a character is a Unicode decimal digit. For example,
   * <code>'0'</code> is a digit.
   * <br>
   * Unicode decimal digit = [Nd]
   *
   * @param ch character to test
   * @return true if ch is a Unicode decimal digit, else false
   * @see #digit(char, int)
   * @see #forDigit(int, int)
   * @see #getType(char)
   */
  public static boolean isDigit(char ch)
  {
    return getType(ch) == DECIMAL_DIGIT_NUMBER;
  }

  /**
   * Determines if a character is part of the Unicode Standard. This is an
   * evolving standard, but covers every character in the data file.
   * <br>
   * defined = not [Cn]
   *
   * @param ch character to test
   * @return true if ch is a Unicode character, else false
   * @see #isDigit(char)
   * @see #isLetter(char)
   * @see #isLetterOrDigit(char)
   * @see #isLowerCase(char)
   * @see #isTitleCase(char)
   * @see #isUpperCase(char)
   */
  public static boolean isDefined(char ch)
  {
    return getType(ch) != UNASSIGNED;
  }

  /**
   * Determines if a character is a Unicode letter. Not all letters have case,
   * so this may return true when isLowerCase and isUpperCase return false.
   * <br>
   * letter = [Lu]|[Ll]|[Lt]|[Lm]|[Lo]
   *
   * @param ch character to test
   * @return true if ch is a Unicode letter, else false
   * @see #isDigit(char)
   * @see #isJavaIdentifierStart(char)
   * @see #isJavaLetter(char)
   * @see #isJavaLetterOrDigit(char)
   * @see #isLetterOrDigit(char)
   * @see #isLowerCase(char)
   * @see #isTitleCase(char)
   * @see #isUnicodeIdentifierStart(char)
   * @see #isUpperCase(char)
   */
  public static boolean isLetter(char ch)
  {
    return ((1 << getType(ch))
            & ((1 << UPPERCASE_LETTER)
               | (1 << LOWERCASE_LETTER)
               | (1 << TITLECASE_LETTER)
               | (1 << MODIFIER_LETTER)
               | (1 << OTHER_LETTER))) != 0;
  }

  /**
   * Determines if a character is a Unicode letter or a Unicode digit. This
   * is the combination of isLetter and isDigit.
   * <br>
   * letter or digit = [Lu]|[Ll]|[Lt]|[Lm]|[Lo]|[Nd]
   *
   * @param ch character to test
   * @return true if ch is a Unicode letter or a Unicode digit, else false
   * @see #isDigit(char)
   * @see #isJavaIdentifierPart(char)
   * @see #isJavaLetter(char)
   * @see #isJavaLetterOrDigit(char)
   * @see #isLetter(char)
   * @see #isUnicodeIdentifierPart(char)
   */
  public static boolean isLetterOrDigit(char ch)
  {
    return ((1 << getType(ch))
            & ((1 << UPPERCASE_LETTER)
               | (1 << LOWERCASE_LETTER)
               | (1 << TITLECASE_LETTER)
               | (1 << MODIFIER_LETTER)
               | (1 << OTHER_LETTER)
               | (1 << DECIMAL_DIGIT_NUMBER))) != 0;
  }

  /**
   * Determines if a character can start a Java identifier. This is the
   * combination of isLetter, any character where getType returns
   * LETTER_NUMBER, currency symbols (like '$'), and connecting punctuation
   * (like '_').
   *
   * @param ch character to test
   * @return true if ch can start a Java identifier, else false
   * @deprecated Replaced by {@link #isJavaIdentifierStart(char)}
   * @see #isJavaLetterOrDigit(char)
   * @see #isJavaIdentifierStart(char)
   * @see #isJavaIdentifierPart(char)
   * @see #isLetter(char)
   * @see #isLetterOrDigit(char)
   * @see #isUnicodeIdentifierStart(char)
   */
  public static boolean isJavaLetter(char ch)
  {
    return isJavaIdentifierStart(ch);
  }

  /**
   * Determines if a character can follow the first letter in
   * a Java identifier.  This is the combination of isJavaLetter (isLetter,
   * type of LETTER_NUMBER, currency, connecting punctuation) and digit,
   * numeric letter (like Roman numerals), combining marks, non-spacing marks,
   * or isIdentifierIgnorable.
   *
   * @param ch character to test
   * @return true if ch can follow the first letter in a Java identifier
   * @deprecated Replaced by {@link #isJavaIdentifierPart(char)}
   * @see #isJavaLetter(char)
   * @see #isJavaIdentifierStart(char)
   * @see #isJavaIdentifierPart(char)
   * @see #isLetter(char)
   * @see #isLetterOrDigit(char)
   * @see #isUnicodeIdentifierPart(char)
   * @see #isIdentifierIgnorable(char)
   */
  public static boolean isJavaLetterOrDigit(char ch)
  {
    return isJavaIdentifierPart(ch);
  }

  /**
   * Determines if a character can start a Java identifier. This is the
   * combination of isLetter, any character where getType returns
   * LETTER_NUMBER, currency symbols (like '$'), and connecting punctuation
   * (like '_').
   * <br>
   * Java identifier start = [Lu]|[Ll]|[Lt]|[Lm]|[Lo]|[Nl]|[Sc]|[Pc]
   *
   * @param ch character to test
   * @return true if ch can start a Java identifier, else false
   * @see #isJavaIdentifierPart(char)
   * @see #isLetter(char)
   * @see #isUnicodeIdentifierStart(char)
   * @since 1.1
   */
  public static boolean isJavaIdentifierStart(char ch)
  {
    return ((1 << getType(ch))
            & ((1 << UPPERCASE_LETTER)
               | (1 << LOWERCASE_LETTER)
               | (1 << TITLECASE_LETTER)
               | (1 << MODIFIER_LETTER)
               | (1 << OTHER_LETTER)
               | (1 << LETTER_NUMBER)
               | (1 << CURRENCY_SYMBOL)
               | (1 << CONNECTOR_PUNCTUATION))) != 0;
  }

  /**
   * Determines if a character can follow the first letter in
   * a Java identifier.  This is the combination of isJavaLetter (isLetter,
   * type of LETTER_NUMBER, currency, connecting punctuation) and digit,
   * numeric letter (like Roman numerals), combining marks, non-spacing marks,
   * or isIdentifierIgnorable.
   * <br>
   * Java identifier extender =
   *   [Lu]|[Ll]|[Lt]|[Lm]|[Lo]|[Nl]|[Sc]|[Pc]|[Mn]|[Mc]|[Nd]|[Cf]
   *   |U+0000-U+0008|U+000E-U+001B|U+007F-U+009F
   *
   * @param ch character to test
   * @return true if ch can follow the first letter in a Java identifier
   * @see #isIdentifierIgnorable(char)
   * @see #isJavaIdentifierStart(char)
   * @see #isLetterOrDigit(char)
   * @see #isUnicodeIdentifierPart(char)
   * @since 1.1
   */
  public static boolean isJavaIdentifierPart(char ch)
  {
    int category = getType(ch);
    return ((1 << category)
            & ((1 << UPPERCASE_LETTER)
               | (1 << LOWERCASE_LETTER)
               | (1 << TITLECASE_LETTER)
               | (1 << MODIFIER_LETTER)
               | (1 << OTHER_LETTER)
               | (1 << NON_SPACING_MARK)
               | (1 << COMBINING_SPACING_MARK)
               | (1 << DECIMAL_DIGIT_NUMBER)
               | (1 << LETTER_NUMBER)
               | (1 << CURRENCY_SYMBOL)
               | (1 << CONNECTOR_PUNCTUATION)
               | (1 << FORMAT))) != 0
      || (category == CONTROL && isIdentifierIgnorable(ch));
  }

  /**
   * Determines if a character can start a Unicode identifier.  Only
   * letters can start a Unicode identifier, but this includes characters
   * in LETTER_NUMBER.
   * <br>
   * Unicode identifier start = [Lu]|[Ll]|[Lt]|[Lm]|[Lo]|[Nl]
   *
   * @param ch character to test
   * @return true if ch can start a Unicode identifier, else false
   * @see #isJavaIdentifierStart(char)
   * @see #isLetter(char)
   * @see #isUnicodeIdentifierPart(char)
   * @since 1.1
   */
  public static boolean isUnicodeIdentifierStart(char ch)
  {
    return ((1 << getType(ch))
            & ((1 << UPPERCASE_LETTER)
               | (1 << LOWERCASE_LETTER)
               | (1 << TITLECASE_LETTER)
               | (1 << MODIFIER_LETTER)
               | (1 << OTHER_LETTER)
               | (1 << LETTER_NUMBER))) != 0;
  }

  /**
   * Determines if a character can follow the first letter in
   * a Unicode identifier. This includes letters, connecting punctuation,
   * digits, numeric letters, combining marks, non-spacing marks, and
   * isIdentifierIgnorable.
   * <br>
   * Unicode identifier extender =
   *   [Lu]|[Ll]|[Lt]|[Lm]|[Lo]|[Nl]|[Mn]|[Mc]|[Nd]|[Pc]|[Cf]|
   *   |U+0000-U+0008|U+000E-U+001B|U+007F-U+009F
   *
   * @param ch character to test
   * @return true if ch can follow the first letter in a Unicode identifier
   * @see #isIdentifierIgnorable(char)
   * @see #isJavaIdentifierPart(char)
   * @see #isLetterOrDigit(char)
   * @see #isUnicodeIdentifierStart(char)
   * @since 1.1
   */
  public static boolean isUnicodeIdentifierPart(char ch)
  {
    int category = getType(ch);
    return ((1 << category)
            & ((1 << UPPERCASE_LETTER)
               | (1 << LOWERCASE_LETTER)
               | (1 << TITLECASE_LETTER)
               | (1 << MODIFIER_LETTER)
               | (1 << OTHER_LETTER)
               | (1 << NON_SPACING_MARK)
               | (1 << COMBINING_SPACING_MARK)
               | (1 << DECIMAL_DIGIT_NUMBER)
               | (1 << LETTER_NUMBER)
               | (1 << CONNECTOR_PUNCTUATION)
               | (1 << FORMAT))) != 0
      || (category == CONTROL && isIdentifierIgnorable(ch));
  }

  /**
   * Determines if a character is ignorable in a Unicode identifier. This
   * includes the non-whitespace ISO control characters (<code>'\u0000'</code>
   * through <code>'\u0008'</code>, <code>'\u000E'</code> through
   * <code>'\u001B'</code>, and <code>'\u007F'</code> through
   * <code>'\u009F'</code>), and FORMAT characters.
   * <br>
   * Unicode identifier ignorable = [Cf]|U+0000-U+0008|U+000E-U+001B
   *    |U+007F-U+009F
   *
   * @param ch character to test
   * @return true if ch is ignorable in a Unicode or Java identifier
   * @see #isJavaIdentifierPart(char)
   * @see #isUnicodeIdentifierPart(char)
   * @since 1.1
   */
  public static boolean isIdentifierIgnorable(char ch)
  {
    return (ch <= '\u009F' && (ch < '\t' || ch >= '\u007F'
                               || (ch <= '\u001B' && ch >= '\u000E')))
      || getType(ch) == FORMAT;
  }

  /**
   * Converts a Unicode character into its lowercase equivalent mapping.
   * If a mapping does not exist, then the character passed is returned.
   * Note that isLowerCase(toLowerCase(ch)) does not always return true.
   *
   * @param ch character to convert to lowercase
   * @return lowercase mapping of ch, or ch if lowercase mapping does
   *         not exist
   * @see #isLowerCase(char)
   * @see #isUpperCase(char)
   * @see #toTitleCase(char)
   * @see #toUpperCase(char)
   */
  public static char toLowerCase(char ch)
  {
    // Signedness doesn't matter, as result is cast back to char.
    return (char) (ch + lower[readChar(ch) >> 7]);
  }

  /**
   * Converts a Unicode character into its uppercase equivalent mapping.
   * If a mapping does not exist, then the character passed is returned.
   * Note that isUpperCase(toUpperCase(ch)) does not always return true.
   *
   * @param ch character to convert to uppercase
   * @return uppercase mapping of ch, or ch if uppercase mapping does
   *         not exist
   * @see #isLowerCase(char)
   * @see #isUpperCase(char)
   * @see #toLowerCase(char)
   * @see #toTitleCase(char)
   */
  public static char toUpperCase(char ch)
  {
    // Signedness doesn't matter, as result is cast back to char.
    return (char) (ch + upper[readChar(ch) >> 7]);
  }

  /**
   * Converts a Unicode character into its titlecase equivalent mapping.
   * If a mapping does not exist, then the character passed is returned.
   * Note that isTitleCase(toTitleCase(ch)) does not always return true.
   *
   * @param ch character to convert to titlecase
   * @return titlecase mapping of ch, or ch if titlecase mapping does
   *         not exist
   * @see #isTitleCase(char)
   * @see #toLowerCase(char)
   * @see #toUpperCase(char)
   */
  public static char toTitleCase(char ch)
  {
    // As title is short, it doesn't hurt to exhaustively iterate over it.
    for (int i = title.length - 2; i >= 0; i -= 2)
      if (title[i] == ch)
        return title[i + 1];
    return toUpperCase(ch);
  }

  /**
   * Converts a character into a digit of the specified radix. If the radix
   * exceeds MIN_RADIX or MAX_RADIX, or if the result of getNumericValue(ch)
   * exceeds the radix, or if ch is not a decimal digit or in the case
   * insensitive set of 'a'-'z', the result is -1.
   * <br>
   * character argument boundary = [Nd]|U+0041-U+005A|U+0061-U+007A
   *    |U+FF21-U+FF3A|U+FF41-U+FF5A
   *
   * @param ch character to convert into a digit
   * @param radix radix in which ch is a digit
   * @return digit which ch represents in radix, or -1 not a valid digit
   * @see #MIN_RADIX
   * @see #MAX_RADIX
   * @see #forDigit(int, int)
   * @see #isDigit(char)
   * @see #getNumericValue(char)
   */
  public static int digit(char ch, int radix)
  {
    if (radix < MIN_RADIX || radix > MAX_RADIX)
      return -1;
    char attr = readChar(ch);
    if (((1 << (attr & TYPE_MASK))
         & ((1 << UPPERCASE_LETTER)
            | (1 << LOWERCASE_LETTER)
            | (1 << DECIMAL_DIGIT_NUMBER))) != 0)
      {
        // Signedness doesn't matter; 0xffff vs. -1 are both rejected.
        int digit = numValue[attr >> 7];
        return (digit < radix) ? digit : -1;
      }
    return -1;
  }

  /**
   * Returns the Unicode numeric value property of a character. For example,
   * <code>'\\u216C'</code> (the Roman numeral fifty) returns 50.
   *
   * <p>This method also returns values for the letters A through Z, (not
   * specified by Unicode), in these ranges: <code>'\u0041'</code>
   * through <code>'\u005A'</code> (uppercase); <code>'\u0061'</code>
   * through <code>'\u007A'</code> (lowercase); and <code>'\uFF21'</code>
   * through <code>'\uFF3A'</code>, <code>'\uFF41'</code> through
   * <code>'\uFF5A'</code> (full width variants).
   *
   * <p>If the character lacks a numeric value property, -1 is returned.
   * If the character has a numeric value property which is not representable
   * as a nonnegative integer, such as a fraction, -2 is returned.
   *
   * character argument boundary = [Nd]|[Nl]|[No]|U+0041-U+005A|U+0061-U+007A
   *    |U+FF21-U+FF3A|U+FF41-U+FF5A
   *
   * @param ch character from which the numeric value property will
   *        be retrieved
   * @return the numeric value property of ch, or -1 if it does not exist, or
   *         -2 if it is not representable as a nonnegative integer
   * @see #forDigit(int, int)
   * @see #digit(char, int)
   * @see #isDigit(char)
   * @since 1.1
   */
  public static int getNumericValue(char ch)
  {
    // Treat numValue as signed.
    return (short) numValue[readChar(ch) >> 7];
  }

  /**
   * Determines if a character is a ISO-LATIN-1 space. This is only the five
   * characters <code>'\t'</code>, <code>'\n'</code>, <code>'\f'</code>,
   * <code>'\r'</code>, and <code>' '</code>.
   * <br>
   * Java space = U+0020|U+0009|U+000A|U+000C|U+000D
   *
   * @param ch character to test
   * @return true if ch is a space, else false
   * @deprecated Replaced by {@link #isWhitespace(char)}
   * @see #isSpaceChar(char)
   * @see #isWhitespace(char)
   */
  public static boolean isSpace(char ch)
  {
    // Performing the subtraction up front alleviates need to compare longs.
    return ch-- <= ' ' && ((1 << ch)
                           & ((1 << (' ' - 1))
                              | (1 << ('\t' - 1))
                              | (1 << ('\n' - 1))
                              | (1 << ('\r' - 1))
                              | (1 << ('\f' - 1)))) != 0;
  }

  /**
   * Determines if a character is a Unicode space character. This includes
   * SPACE_SEPARATOR, LINE_SEPARATOR, and PARAGRAPH_SEPARATOR.
   * <br>
   * Unicode space = [Zs]|[Zp]|[Zl]
   *
   * @param ch character to test
   * @return true if ch is a Unicode space, else false
   * @see #isWhitespace(char)
   * @since 1.1
   */
  public static boolean isSpaceChar(char ch)
  {
    return ((1 << getType(ch))
            & ((1 << SPACE_SEPARATOR)
               | (1 << LINE_SEPARATOR)
               | (1 << PARAGRAPH_SEPARATOR))) != 0;
  }

  /**
   * Determines if a character is Java whitespace. This includes Unicode
   * space characters (SPACE_SEPARATOR, LINE_SEPARATOR, and
   * PARAGRAPH_SEPARATOR) except the non-breaking spaces
   * (<code>'\u00A0'</code>, <code>'\u2007'</code>, and <code>'\u202F'</code>);
   * and these characters: <code>'\u0009'</code>, <code>'\u000A'</code>,
   * <code>'\u000B'</code>, <code>'\u000C'</code>, <code>'\u000D'</code>,
   * <code>'\u001C'</code>, <code>'\u001D'</code>, <code>'\u001E'</code>,
   * and <code>'\u001F'</code>.
   * <br>
   * Java whitespace = ([Zs] not Nb)|[Zl]|[Zp]|U+0009-U+000D|U+001C-U+001F
   *
   * @param ch character to test
   * @return true if ch is Java whitespace, else false
   * @see #isSpaceChar(char)
   * @since 1.1
   */
  public static boolean isWhitespace(char ch)
  {
    int attr = readChar(ch);
    return ((((1 << (attr & TYPE_MASK))
              & ((1 << SPACE_SEPARATOR)
                 | (1 << LINE_SEPARATOR)
                 | (1 << PARAGRAPH_SEPARATOR))) != 0)
            && (attr & NO_BREAK_MASK) == 0)
      || (ch <= '\u001F' && ((1 << ch)
                             & ((1 << '\t')
                                | (1 << '\n')
                                | (1 << '\u000B')
                                | (1 << '\u000C')
                                | (1 << '\r')
                                | (1 << '\u001C')
                                | (1 << '\u001D')
                                | (1 << '\u001E')
                                | (1 << '\u001F'))) != 0);
  }

  /**
   * Determines if a character has the ISO Control property.
   * <br>
   * ISO Control = [Cc]
   *
   * @param ch character to test
   * @return true if ch is an ISO Control character, else false
   * @see #isSpaceChar(char)
   * @see #isWhitespace(char)
   * @since 1.1
   */
  public static boolean isISOControl(char ch)
  {
    return getType(ch) == CONTROL;
  }

  /**
   * Returns the Unicode general category property of a character.
   *
   * @param ch character from which the general category property will
   *        be retrieved
   * @return the character category property of ch as an integer
   * @see #UNASSIGNED
   * @see #UPPERCASE_LETTER
   * @see #LOWERCASE_LETTER
   * @see #TITLECASE_LETTER
   * @see #MODIFIER_LETTER
   * @see #OTHER_LETTER
   * @see #NON_SPACING_MARK
   * @see #ENCLOSING_MARK
   * @see #COMBINING_SPACING_MARK
   * @see #DECIMAL_DIGIT_NUMBER
   * @see #LETTER_NUMBER
   * @see #OTHER_NUMBER
   * @see #SPACE_SEPARATOR
   * @see #LINE_SEPARATOR
   * @see #PARAGRAPH_SEPARATOR
   * @see #CONTROL
   * @see #FORMAT
   * @see #PRIVATE_USE
   * @see #SURROGATE
   * @see #DASH_PUNCTUATION
   * @see #START_PUNCTUATION
   * @see #END_PUNCTUATION
   * @see #CONNECTOR_PUNCTUATION
   * @see #OTHER_PUNCTUATION
   * @see #MATH_SYMBOL
   * @see #CURRENCY_SYMBOL
   * @see #MODIFIER_SYMBOL
   * @see #INITIAL_QUOTE_PUNCTUATION
   * @see #FINAL_QUOTE_PUNCTUATION
   * @since 1.1
   */
  public static int getType(char ch)
  {
    return readChar(ch) & TYPE_MASK;
  }

  /**
   * Converts a digit into a character which represents that digit
   * in a specified radix. If the radix exceeds MIN_RADIX or MAX_RADIX,
   * or the digit exceeds the radix, then the null character <code>'\0'</code>
   * is returned.  Otherwise the return value is in '0'-'9' and 'a'-'z'.
   * <br>
   * return value boundary = U+0030-U+0039|U+0061-U+007A
   *
   * @param digit digit to be converted into a character
   * @param radix radix of digit
   * @return character representing digit in radix, or '\0'
   * @see #MIN_RADIX
   * @see #MAX_RADIX
   * @see #digit(char, int)
   */
  public static char forDigit(int digit, int radix)
  {
    if (radix < MIN_RADIX || radix > MAX_RADIX
        || digit < 0 || digit >= radix)
      return '\0';
    return Number.digits[digit];
  }

  /**
   * Returns the Unicode directionality property of the character. This
   * is used in the visual ordering of text.
   *
   * @param ch the character to look up
   * @return the directionality constant, or DIRECTIONALITY_UNDEFINED
   * @see #DIRECTIONALITY_UNDEFINED
   * @see #DIRECTIONALITY_LEFT_TO_RIGHT
   * @see #DIRECTIONALITY_RIGHT_TO_LEFT
   * @see #DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
   * @see #DIRECTIONALITY_EUROPEAN_NUMBER
   * @see #DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR
   * @see #DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR
   * @see #DIRECTIONALITY_ARABIC_NUMBER
   * @see #DIRECTIONALITY_COMMON_NUMBER_SEPARATOR
   * @see #DIRECTIONALITY_NONSPACING_MARK
   * @see #DIRECTIONALITY_BOUNDARY_NEUTRAL
   * @see #DIRECTIONALITY_PARAGRAPH_SEPARATOR
   * @see #DIRECTIONALITY_SEGMENT_SEPARATOR
   * @see #DIRECTIONALITY_WHITESPACE
   * @see #DIRECTIONALITY_OTHER_NEUTRALS
   * @see #DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING
   * @see #DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE
   * @see #DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
   * @see #DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE
   * @see #DIRECTIONALITY_POP_DIRECTIONAL_FORMAT
   * @since 1.4
   */
  public static byte getDirectionality(char ch)
  {
    // The result will correctly be signed.
    return (byte) (direction[readChar(ch) >> 7] >> 2);
  }

  /**
   * Determines whether the character is mirrored according to Unicode. For
   * example, <code>\u0028</code> (LEFT PARENTHESIS) appears as '(' in
   * left-to-right text, but ')' in right-to-left text.
   *
   * @param ch the character to look up
   * @return true if the character is mirrored
   * @since 1.4
   */
  public static boolean isMirrored(char ch)
  {
    return (readChar(ch) & MIRROR_MASK) != 0;
  }

  /**
   * Compares another Character to this Character, numerically.
   *
   * @param anotherCharacter Character to compare with this Character
   * @return a negative integer if this Character is less than
   *         anotherCharacter, zero if this Character is equal, and
   *         a positive integer if this Character is greater
   * @throws NullPointerException if anotherCharacter is null
   * @since 1.2
   */
  public int compareTo(Character anotherCharacter)
  {
    return value - anotherCharacter.value;
  }

  /**
   * Returns an <code>Character</code> object wrapping the value.
   * In contrast to the <code>Character</code> constructor, this method
   * will cache some values.  It is used by boxing conversion.
   *
   * @param val the value to wrap
   * @return the <code>Character</code>
   */
  public static Character valueOf(char val)
  {
    if (val > MAX_CACHE)
      return new Character(val);
    synchronized (charCache)
      {
	if (charCache[val - MIN_VALUE] == null)
	  charCache[val - MIN_VALUE] = new Character(val);
	return charCache[val - MIN_VALUE];
      }
  }

  /**
   * Reverse the bytes in val.
   * @since 1.5
   */
  public static char reverseBytes(char val)
  {
    return (char) (((val >> 8) & 0xff) | ((val << 8) & 0xff00));
  }
} // class Character
