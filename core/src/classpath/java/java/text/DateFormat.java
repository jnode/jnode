/* DateFormat.java -- Class for formatting/parsing date/times
   Copyright (C) 1998, 1999, 2000, 2001 Free Software Foundation, Inc.

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


package java.text;

import java.util.*;

/**
 * @author Per Bothner <bothner@cygnus.com>
 * @date October 25, 1998.
 */
/* Written using "Java Class Libraries", 2nd edition, plus online
 * API docs for JDK 1.2 beta from http://www.javasoft.com.
 * Status:  Mostly complete; search for FIXME to see omissions.
 */

public abstract class DateFormat extends Format implements Cloneable
{
  protected Calendar calendar;
  protected NumberFormat numberFormat;

  // (Values determined using a test program.)
  public static final int FULL = 0;
  public static final int LONG = 1;
  public static final int MEDIUM = 2;
  public static final int SHORT = 3;
  public static final int DEFAULT = MEDIUM;

  /* These constants need to have these exact values.  They
   * correspond to index positions within the localPatternChars
   * string for a given locale.  For example, the US locale uses
   * the string "GyMdkHmsSEDFwWahKz", where 'G' is the character
   * for era, 'y' for year, and so on down to 'z' for time zone.
   */
  public static final int ERA_FIELD = 0;
  public static final int YEAR_FIELD = 1;
  public static final int MONTH_FIELD = 2;
  public static final int DATE_FIELD = 3;
  public static final int HOUR_OF_DAY1_FIELD = 4;
  public static final int HOUR_OF_DAY0_FIELD = 5;
  public static final int MINUTE_FIELD = 6;
  public static final int SECOND_FIELD = 7;
  public static final int MILLISECOND_FIELD = 8;
  public static final int DAY_OF_WEEK_FIELD = 9;
  public static final int DAY_OF_YEAR_FIELD = 10;
  public static final int DAY_OF_WEEK_IN_MONTH_FIELD = 11;
  public static final int WEEK_OF_YEAR_FIELD = 12;
  public static final int WEEK_OF_MONTH_FIELD = 13;
  public static final int AM_PM_FIELD = 14;
  public static final int HOUR1_FIELD = 15;
  public static final int HOUR0_FIELD = 16;
  public static final int TIMEZONE_FIELD = 17;

  /**
   * This method initializes a new instance of <code>DateFormat</code>.
   */
  protected DateFormat ()
  {
  }

  /**
   * This method tests this object for equality against the specified object.
   * The two objects will be considered equal if an only if the specified
   * object:
   * <P>
   * <ul>
   * <li>Is not <code>null</code>.
   * <li>Is an instance of <code>DateFormat</code>.
   * <li>Has the same calendar and numberFormat field values as this object.
   * </ul>
   *
   * @param obj The object to test for equality against.
   * 
   * @return <code>true</code> if the specified object is equal to this object,
   * <code>false</code> otherwise.
   */
  public boolean equals (Object obj)
  {
    if (! (obj instanceof DateFormat))
      return false;
    DateFormat d = (DateFormat) obj;
    return calendar.equals(d.calendar) && numberFormat.equals(d.numberFormat);
  }

  /**
   * This method returns a copy of this object.
   *
   * @return A copy of this object.
   */
  public Object clone ()
  {
    // We know the superclass just call's Object's generic cloner.
    return super.clone ();
  }

  /**
   * This method formats the specified <code>Object</code> into a date string
   * and appends it to the specified <code>StringBuffer</code>.
   * The specified object must be an instance of <code>Number</code> or
   * <code>Date</code> or an <code>IllegalArgumentException</code> will be
   * thrown.
   *
   * @param obj The <code>Object</code> to format.
   * @param toAppendTo The <code>StringBuffer</code> to append the resultant
   * <code>String</code> to.
   * @param fieldPosition Is updated to the start and end index of the
   * specified field.
   *
   * @return The <code>StringBuffer</code> supplied on input, with the
   * formatted date/time appended.
   */
  public final StringBuffer format (Object obj,
				    StringBuffer buf, FieldPosition pos)
  {
    if (obj instanceof Number)
      obj = new Date(((Number) obj).longValue());
    return format ((Date) obj, buf, pos);
  }

  /**  
    * Formats the date argument according to the pattern specified. 
    *
    * @param date The formatted date.
    */
  public final String format (Date date)
  {
    StringBuffer sb = new StringBuffer ();
    format (date, sb, new FieldPosition (MONTH_FIELD));
    return sb.toString();
  }

  /**
   * This method formats a <code>Date</code> into a string and appends it
   * to the specified <code>StringBuffer</code>.
   *
   * @param date The <code>Date</code> value to format.
   * @param toAppendTo The <code>StringBuffer</code> to append the resultant
   * <code>String</code> to.
   * @param fieldPosition Is updated to the start and end index of the
   * specified field.
   *
   * @return The <code>StringBuffer</code> supplied on input, with the
   * formatted date/time appended.
   */
  public abstract StringBuffer format (Date date,
				       StringBuffer buf, FieldPosition pos);

  /**
   * This method returns a list of available locales supported by this
   * class.
   */
  public static Locale[] getAvailableLocales ()
  {
    // FIXME
    Locale[] l = new Locale[1];
    l[0] = Locale.US;
    return l;
  }

  /**
    * This method returns the <code>Calendar</code> object being used by
    * this object to parse/format datetimes.
    *
    * @return The <code>Calendar</code> being used by this object.
    *
    * @see java.util.Calendar
    */
  public Calendar getCalendar ()
  {
    return calendar;
  }

  private static final DateFormat computeInstance (int style, Locale loc,
						   boolean use_date,
						   boolean use_time)
  {
    return computeInstance (style, style, loc, use_date, use_time);
  }

  private static final DateFormat computeInstance (int dateStyle, 
						   int timeStyle,
						   Locale loc,
						   boolean use_date,
						   boolean use_time)
  {
    ResourceBundle res;
    try
      {
	res = ResourceBundle.getBundle("gnu.java.locale.LocaleInformation",
				       loc);
      }
    catch (MissingResourceException x)
      {
	res = null;
      }

    String pattern = null;
    if (use_date)
      {
	String name, def;
	switch (dateStyle)
	  {
	  case FULL:
	    name = "fullDateFormat";
	    def = "EEEE MMMM d, yyyy G";
	    break;
	  case LONG:
	    name = "longDateFormat";
	    def = "MMMM d, yyyy";
	    break;
	  case MEDIUM:
	    name = "mediumDateFormat";
	    def = "d-MMM-yy";
	    break;
	  case SHORT:
	    name = "shortDateFormat";
	    def = "M/d/yy";
	    break;
	  default:
	    throw new IllegalArgumentException ();
	  }
	try
	  {
	    pattern = res == null ? def : res.getString(name);
	  }
	catch (MissingResourceException x)
	  {
	    pattern = def;
	  }
      }

    if (use_time)
      {
	if (pattern == null)
	  pattern = "";
	else
	  pattern += " ";

	String name, def;
	switch (timeStyle)
	  {
	  case FULL:
	    name = "fullTimeFormat";
	    def = "h:mm:ss;S 'o''clock' a z";
	    break;
	  case LONG:
	    name = "longTimeFormat";
	    def = "h:mm:ss a z";
	    break;
	  case MEDIUM:
	    name = "mediumTimeFormat";
	    def = "h:mm:ss a";
	    break;
	  case SHORT:
	    name = "shortTimeFormat";
	    def = "h:mm a";
	    break;
	  default:
	    throw new IllegalArgumentException ();
	  }

	String s;
	try
	  {
	    s = res == null ? def : res.getString(name);
	  }
	catch (MissingResourceException x)
	  {
	    s = def;
	  }
	pattern += s;
      }

    return new SimpleDateFormat (pattern, loc);
  }

 /**
   * This method returns an instance of <code>DateFormat</code> that will
   * format using the default formatting style for dates.
   *
   * @return A new <code>DateFormat</code> instance.
   */
  public static final DateFormat getDateInstance ()
  {
    return getDateInstance (DEFAULT, Locale.getDefault());
  }

  /**
   * This method returns an instance of <code>DateFormat</code> that will
   * format using the specified formatting style for dates.
   *
   * @param style The type of formatting to perform. 
   * 
   * @return A new <code>DateFormat</code> instance.
   */
  public static final DateFormat getDateInstance (int style)
  {
    return getDateInstance (style, Locale.getDefault());
  }

  /**
   * This method returns an instance of <code>DateFormat</code> that will
   * format using the specified formatting style for dates.  The specified
   * localed will be used in place of the default.
   *
   * @param style The type of formatting to perform. 
   * @param aLocale The desired locale.
   * 
   * @return A new <code>DateFormat</code> instance.
   */
  public static final DateFormat getDateInstance (int style, Locale loc)
  {
    return computeInstance (style, loc, true, false);
  }

  /**
   * This method returns a new instance of <code>DateFormat</code> that
   * formats both dates and times using the <code>SHORT</code> style.
   *
   * @return A new <code>DateFormat</code>instance.
   */
  public static final DateFormat getDateTimeInstance ()
  {
    return getDateTimeInstance (DEFAULT, DEFAULT, Locale.getDefault());
  }

  /**
   * This method returns a new instance of <code>DateFormat</code> that
   * formats both dates and times using the <code>DEFAULT</code> style.
   *
   * @return A new <code>DateFormat</code>instance.
   */
  public static final DateFormat getDateTimeInstance (int dateStyle, 
						      int timeStyle)
  {
    return getDateTimeInstance (dateStyle, timeStyle, Locale.getDefault());
  }

  /**
   * This method returns a new instance of <code>DateFormat</code> that
   * formats both dates and times using the specified styles.
   * 
   * @param dateStyle The desired style for date formatting.
   * @param timeStyle The desired style for time formatting
   *
   * @return A new <code>DateFormat</code>instance.
   */
  public static final DateFormat getDateTimeInstance (int dateStyle, 
						      int timeStyle, 
						      Locale loc)
  {
    return computeInstance (dateStyle, timeStyle, loc, true, true);
  }

  /**
   * This method returns a new instance of <code>DateFormat</code> that
   * formats both dates and times using the <code>SHORT</code> style.
   *
   * @return A new <code>DateFormat</code>instance.
   */
  public static final DateFormat getInstance ()
  {
    // JCL book says SHORT.
    return getDateTimeInstance (SHORT, SHORT, Locale.getDefault());
  }

  /**
   * This method returns the <code>NumberFormat</code> object being used
   * by this object to parse/format time values.
   *
   * @return The <code>NumberFormat</code> in use by this object.
   */
  public NumberFormat getNumberFormat ()
  {
    return numberFormat;
  }

 /**
   * This method returns an instance of <code>DateFormat</code> that will
   * format using the default formatting style for times.
   *
   * @return A new <code>DateFormat</code> instance.
   */
  public static final DateFormat getTimeInstance ()
  {
    return getTimeInstance (DEFAULT, Locale.getDefault());
  }

  /**
   * This method returns an instance of <code>DateFormat</code> that will
   * format using the specified formatting style for times.
   *
   * @param style The type of formatting to perform. 
   * 
   * @return A new <code>DateFormat</code> instance.
   */
  public static final DateFormat getTimeInstance (int style)
  {
    return getTimeInstance (style, Locale.getDefault());
  }

  /**
   * This method returns an instance of <code>DateFormat</code> that will
   * format using the specified formatting style for times.  The specified
   * localed will be used in place of the default.
   *
   * @param style The type of formatting to perform. 
   * @param aLocale The desired locale.
   * 
   * @return A new <code>DateFormat</code> instance.
   */
  public static final DateFormat getTimeInstance (int style, Locale loc)
  {
    return computeInstance (style, loc, false, true);
  }

  /**
   * This method returns the <code>TimeZone</code> object being used by
   * this instance.
   *
   * @return The time zone in use.
   */
  public TimeZone getTimeZone ()
  {
    return calendar.getTimeZone();
  }

  /**
   * This method returns a hash value for this object.
   * 
   * @return A hash value for this object.
   */
  public int hashCode ()
  {
    int hash = calendar.hashCode();
    if (numberFormat != null)
      hash ^= numberFormat.hashCode();
    return hash;
  }

  /**
   * This method indicates whether or not the parsing of date and time
   * values should be done in a lenient value.
   *
   * @return <code>true</code> if date/time parsing is lenient,
   * <code>false</code> otherwise.
   */
  public boolean isLenient ()
  {
    return calendar.isLenient();
  }

  /**
   * This method parses the specified date/time string.
   *
   * @return The resultant date.
   *
   * @exception ParseException If the specified string cannot be parsed.
   */
  public Date parse (String source) throws ParseException
  {
    ParsePosition pos = new ParsePosition(0);
    Date result = parse (source, pos);
    if (result == null)
      {
	int index = pos.getErrorIndex();
	if (index < 0)
	  index = pos.getIndex();
	throw new ParseException("invalid Date syntax", index);
      }
    return result;
  }

  /** 
   * This method parses the specified <code>String</code> into a 
   * <code>Date</code>.  The <code>pos</code> argument contains the
   * starting parse position on method entry and the ending parse
   * position on method exit.
   *
   * @param text The string to parse.
   * @param pos The starting parse position in entry, the ending parse
   * position on exit.
   *
   * @return The parsed date, or <code>null</code> if the string cannot
   * be parsed.
   */
  public abstract Date parse (String source, ParsePosition pos);

  /**
   * This method is identical to <code>parse(String, ParsePosition)</code>,
   * but returns its result as an <code>Object</code> instead of a
   * <code>Date</code>.
   * 
   * @param source The string to parse.
   * @param pos The starting parse position in entry, the ending parse
   * position on exit.
   *
   * @return The parsed date, or <code>null</code> if the string cannot
   * be parsed.
   */
  public Object parseObject (String source, ParsePosition pos)
  {
    return parse(source, pos);
  }

  /**
   * This method specified the <code>Calendar</code> that should be used 
   * by this object to parse/format datetimes.
   *
   * @param The new <code>Calendar</code> for this object.
   *
   * @see java.util.Calendar
   */
  public void setCalendar (Calendar calendar)
  {
    this.calendar = calendar;
  }

  /**
   * This method specifies whether or not this object should be lenient in 
   * the syntax it accepts while parsing date/time values.
   *
   * @param lenient <code>true</code> if parsing should be lenient,
   * <code>false</code> otherwise.
   */
  public void setLenient (boolean lenient)
  {
    calendar.setLenient(lenient);
  }

  /**
   * This method specifies the <code>NumberFormat</code> object that should
   * be used by this object to parse/format times.
   *
   * @param The <code>NumberFormat</code> in use by this object.
   */
  public void setNumberFormat (NumberFormat numberFormat)
  {
    this.numberFormat = numberFormat;
  }

  /**
   * This method sets the time zone that should be used by this object.
   *
   * @param The new time zone.
   */
  public void setTimeZone (TimeZone timeZone)
  {
    calendar.setTimeZone(timeZone);
  }
}
