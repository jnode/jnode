/* java.util.GregorianCalendar
   Copyright (C) 1998, 1999, 2001, 2002, 2003, 2004
   Free Software Foundation, Inc.

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


package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * <p>
 * This class represents the Gregorian calendar, that is used in most
 * countries all over the world.  It does also handle the Julian calendar
 * for dates smaller than the date of the change to the Gregorian calendar.
 * The Gregorian calendar differs from the Julian calendar by a different
 * leap year rule (no leap year every 100 years, except if year is divisible
 * by 400).  
 * </p>
 * <p>
 * This change date is different from country to country, and can be changed with
 * <code>setGregorianChange</code>.  The first countries to adopt the Gregorian
 * calendar did so on the 15th of October, 1582.  This date followed October
 * the 4th, 1582 in the Julian calendar system.  The non-existant days that were
 * omitted when the change took place are interpreted as Gregorian dates.
 * </p>
 * <p>
 * Prior to the changeover date, New Year's Day occurred on the 25th of March.
 * However, this class always takes New Year's Day as being the 1st of January.
 * Client code should manually adapt the year value, if required, for dates
 * between January the 1st and March the 24th in years prior to the changeover.
 * </p>
 * <p>
 * Any date infinitely forwards or backwards in time can be represented by
 * this class.  A <em>proleptic</em> calendar system is used, which allows
 * future dates to be created via the existing rules.  This allows meaningful
 * and consistent dates to be produced for all years.  However, dates are only
 * historically accurate following March the 1st, 4AD when the Julian calendar
 * system was adopted.  Prior to this, leap year rules were applied erraticly.
 * </p>
 * <p>
 * There are two eras available for the Gregorian calendar, namely BC and AD.
 * </p>
 * <p>
 * Weeks are defined as a period of seven days, beginning on the first day
 * of the week, as returned by <code>getFirstDayOfWeek()</code>, and ending
 * on the day prior to this.
 * </p>
 * <p>
 * The weeks of the year are numbered from 1 to a possible 53.  The first week
 * of the year is defined as the first week that contains at least the minimum
 * number of days of the first week in the new year (retrieved via
 * <code>getMinimalDaysInFirstWeek()</code>).  All weeks after this are numbered
 * from 2 onwards.
 * </p>
 * <p>
 * For example, take the year 2004.  It began on a Thursday.  The first week
 * of 2004 depends both on where a week begins and how long it must minimally
 * last.  Let's say that the week begins on a Monday and must have a minimum
 * of 5 days.  In this case, the first week begins on Monday, the 5th of January.
 * The first 4 days (Thursday to Sunday) are not eligible, as they are too few
 * to make up the minimum number of days of the first week which must be in
 * the new year.  If the minimum was lowered to 4 days, then the first week
 * would instead begin on Monday, the 29th of December, 2003.  This first week
 * has 4 of its days in the new year, and is now eligible.
 * </p>
 * <p>
 * The weeks of the month are numbered from 0 to a possible 6.  The first week
 * of the month (numbered 1) is a set of days, prior to the first day of the week,
 * which number at least the minimum number of days in a week.  Unlike the first
 * week of the year, the first week of the month only uses days from that particular
 * month.  As a consequence, it may have a variable number of days (from the minimum
 * number required up to a full week of 7) and it need not start on the first day of
 * the week.  It must, however, be following by the first day of the week, as this
 * marks the beginning of week 2.  Any days of the month which occur prior to the
 * first week (because the first day of the week occurs before the minimum number
 * of days is met) are seen as week 0.
 * </p>
 * <p>
 * Again, we will take the example of the year 2004 to demonstrate this.  September
 * 2004 begins on a Wednesday.  Taking our first day of the week as Monday, and the
 * minimum length of the first week as 6, we find that week 1 runs from Monday,
 * the 6th of September to Sunday the 12th.  Prior to the 6th, there are only
 * 5 days (Wednesday through to Sunday).  This is too small a number to meet the
 * minimum, so these are classed as being days in week 0.  Week 2 begins on the
 * 13th, and so on.  This changes if we reduce the minimum to 5.  In this case,
 * week 1 is a truncated week from Wednesday the 1st to Sunday the 5th, and week
 * 0 doesn't exist.  The first seven day week is week 2, starting on the 6th.
 * </p>
 * <p>
 * On using the <code>clear()</code> method, the Gregorian calendar returns
 * to its default value of the 1st of January, 1970 AD 00:00:00 (the epoch).
 * The day of the week is set to the correct day for that particular time.
 * The day is also the first of the month, and the date is in week 0.
 * </p>
 *
 * @see Calendar
 * @see TimeZone
 * @see Calendar#getFirstDayOfWeek()
 * @see Calendar#getMinimalDaysInFirstWeek()
 */
public class GregorianCalendar extends Calendar
{
	/**
   * Constant representing the era BC (Before Christ).
	 */
	public static final int BC = 0;

	/**
	 * Constant representing the era AD (Anno Domini).
	 */
	public static final int AD = 1;

	/**
	 * The point at which the Gregorian calendar rules were used.
	 * This is locale dependent; the default for most catholic
	 * countries is midnight (UTC) on October 5, 1582 (Julian),
	 * or October 15, 1582 (Gregorian).
   *
   * @serial the changeover point from the Julian calendar
   *         system to the Gregorian.
	 */
	private long gregorianCutover;

  /**
   * For compatability with Sun's JDK.
   */
	static final long serialVersionUID = -8125100834729963327L;

	/**
   * The name of the resource bundle. Used only by getBundle()
	 */
	private static final String bundleName = "gnu.java.locale.Calendar";

	/**
   * Retrieves the resource bundle.  The resources should be loaded
   * via this method only. Iff an application uses this method, the
   * resourcebundle is required.
   *
   * @param locale the locale in use for this calendar.
   * @return A resource bundle for the calendar for the specified locale.
   */
  private static ResourceBundle getBundle(Locale locale) 
  {
    return ResourceBundle.getBundle(bundleName, locale,
      ClassLoader.getSystemClassLoader());
  }

  /**
	 * Constructs a new GregorianCalender representing the current
	 * time, using the default time zone and the default locale.  
	 */
  public GregorianCalendar()
  {
		this(TimeZone.getDefault(), Locale.getDefault());
	}

	/**
	 * Constructs a new GregorianCalender representing the current
	 * time, using the specified time zone and the default locale.  
   * 
	 * @param zone a time zone.
	 */
  public GregorianCalendar(TimeZone zone)
  {
		this(zone, Locale.getDefault());
	}

	/**
	 * Constructs a new GregorianCalender representing the current
	 * time, using the default time zone and the specified locale.  
   *  
	 * @param locale a locale.
	 */
  public GregorianCalendar(Locale locale)
  {
		this(TimeZone.getDefault(), locale);
	}

	/**
	 * Constructs a new GregorianCalender representing the current
	 * time with the given time zone and the given locale.
   *
	 * @param zone a time zone.  
	 * @param locale a locale.  
	 */
  public GregorianCalendar(TimeZone zone, Locale locale)
  {
    this(zone, locale, false);
    setTimeInMillis(System.currentTimeMillis());
  }

  /**
   * Common constructor that all constructors should call.
   * @param zone a time zone.  
   * @param locale a locale.  
   * @param unused unused parameter to make the signature differ from
   * the public constructor (TimeZone, Locale).
   */
  private GregorianCalendar(TimeZone zone, Locale locale, boolean unused)
  {
		super(zone, locale);
    ResourceBundle rb = getBundle(locale);
    gregorianCutover = ((Date) rb.getObject("gregorianCutOver")).getTime();
	}

	/**
	 * Constructs a new GregorianCalendar representing midnight on the
	 * given date with the default time zone and locale.
   *
	 * @param year corresponds to the YEAR time field.
	 * @param month corresponds to the MONTH time field.
	 * @param day corresponds to the DAY time field.
	 */
  public GregorianCalendar(int year, int month, int day)
  {
    this(TimeZone.getDefault(), Locale.getDefault(), false);
		set(year, month, day);
	}

	/**
	 * Constructs a new GregorianCalendar representing midnight on the
	 * given date with the default time zone and locale.
   *
	 * @param year corresponds to the YEAR time field.
	 * @param month corresponds to the MONTH time field.
	 * @param day corresponds to the DAY time field.
	 * @param hour corresponds to the HOUR_OF_DAY time field.
	 * @param minute corresponds to the MINUTE time field.
	 */
  public GregorianCalendar(int year, int month, int day, int hour, int minute)
  {
    this(TimeZone.getDefault(), Locale.getDefault(), false);
		set(year, month, day, hour, minute);
	}

	/**
	 * Constructs a new GregorianCalendar representing midnight on the
	 * given date with the default time zone and locale.
   *
	 * @param year corresponds to the YEAR time field.
	 * @param month corresponds to the MONTH time field.
	 * @param day corresponds to the DAY time field.
	 * @param hour corresponds to the HOUR_OF_DAY time field.
	 * @param minute corresponds to the MINUTE time field.
	 * @param second corresponds to the SECOND time field.
	 */
  public GregorianCalendar(int year, int month, int day,
			   int hour, int minute, int second)
  {
    this(TimeZone.getDefault(), Locale.getDefault(), false);
		set(year, month, day, hour, minute, second);
	}

	/**
	 * Sets the date of the switch from Julian dates to Gregorian dates.
	 * You can use <code>new Date(Long.MAX_VALUE)</code> to use a pure
	 * Julian calendar, or <code>Long.MIN_VALUE</code> for a pure Gregorian
	 * calendar.
   *
	 * @param date the date of the change.
	 */
  public void setGregorianChange(Date date)
  {
		gregorianCutover = date.getTime();
	}

	/**
	 * Gets the date of the switch from Julian dates to Gregorian dates.
   *
	 * @return the date of the change.
	 */
  public final Date getGregorianChange()
  {
		return new Date(gregorianCutover);
	}

	/**
   * <p>
	 * Determines if the given year is a leap year.  The result is
   * undefined if the Gregorian change took place in 1800, so that
   * the end of February is skipped, and that year is specified.
   * (well...).
   * </p>
   * <p>
   * To specify a year in the BC era, use a negative value calculated
   * as 1 - y, where y is the required year in BC.  So, 1 BC is 0,
   * 2 BC is -1, 3 BC is -2, etc.
   * </p>
	 *
   * @param year a year (use a negative value for BC).
   * @return true, if the given year is a leap year, false otherwise.  
   */
  public boolean isLeapYear(int year)
  {
		if ((year & 3) != 0)
			// Only years divisible by 4 can be leap years
			return false;

		// compute the linear day of the 29. February of that year.
		// The 13 is the number of days, that were omitted in the Gregorian
		// Calender until the epoch.
    int julianDay = (((year-1) * (365*4+1)) >> 2) + (31+29 - 
        (((1970-1) * (365*4+1)) / 4 + 1 - 13));

		// If that day is smaller than the gregorianChange the julian
		// rule applies:  This is a leap year since it is divisible by 4.
		if (julianDay * (24 * 60 * 60 * 1000L) < gregorianCutover)
			return true;

		return ((year % 100) != 0 || (year % 400) == 0);
	}

	/**
	 * Get the linear time in milliseconds since the epoch.  If you
	 * specify a nonpositive year it is interpreted as BC as
	 * following: 0 is 1 BC, -1 is 2 BC and so on.  The date is
	 * interpreted as gregorian if the change occurred before that date.
	 *
	 * @param year the year of the date.
	 * @param dayOfYear the day of year of the date; 1 based.
	 * @param millis the millisecond in that day.
   * @return the days since the epoch, may be negative.  
   */
  private long getLinearTime(int year, int dayOfYear, int millis)
  {
		// The 13 is the number of days, that were omitted in the Gregorian
    // Calendar until the epoch.
		// We shift right by 2 instead of dividing by 4, to get correct
		// results for negative years (and this is even more efficient).
    int julianDay = ((year * (365 * 4 + 1)) >> 2) + dayOfYear -
      ((1970 * (365 * 4 + 1)) / 4 + 1 - 13);
		long time = julianDay * (24 * 60 * 60 * 1000L) + millis;

    if (time >= gregorianCutover)
      {
			// subtract the days that are missing in gregorian calendar
			// with respect to julian calendar.
			//
			// Okay, here we rely on the fact that the gregorian
			// calendar was introduced in the AD era.  This doesn't work
			// with negative years.
			//
			// The additional leap year factor accounts for the fact that
			// a leap day is not seen on Jan 1 of the leap year.
	// And on and after the leap day, the leap day has already been
	// included in dayOfYear. 
			int gregOffset = (year / 400) - (year / 100) + 2;
	if (isLeapYear (year, true))
				--gregOffset;
			time += gregOffset * (24 * 60 * 60 * 1000L);
		}
		return time;
	}

  /**
   * Retrieves the day of the week corresponding to the specified
   * day of the specified year.
   *
   * @param year the year in which the dayOfYear occurs.
   * @param dayOfYear the day of the year (an integer between 0 and
   *        and 366)
   */
  private int getWeekDay(int year, int dayOfYear)
  {
    int day =
      (int) (getLinearTime(year, dayOfYear, 0) / (24 * 60 * 60 * 1000L));

		// The epoch was a thursday.
		int weekday = (day + THURSDAY) % 7;
		if (weekday <= 0)
			weekday += 7;
		return weekday;
	}

	/**
   * <p>
	 * Calculate the dayOfYear from the fields array.  
	 * The relativeDays is used, to account for weeks that begin before
   * the Gregorian change and end after it.
   * </p>
   * <p>
   * We return two values.  The first is used to determine, if we
   * should use the Gregorian calendar or the Julian calendar, in order
   * to handle the change year. The second is a relative day after the given
	 * day.  This is necessary for week calculation in the year in
   * which the Gregorian change occurs. 
   * </p>
	 *
	 * @param year the year, negative for BC.
   * @return an array of two integer values, the first containing a reference
   * day in the current year, the second a relative count since this reference
   * day.  
   */
  private int[] getDayOfYear(int year)
  {
    if (isSet[MONTH])
      {
			int dayOfYear;
	if (fields[MONTH] > FEBRUARY)
	  {

				// The months after February are regular:
				// 9 is an offset found by try and error.
				dayOfYear = (fields[MONTH] * (31 + 30 + 31 + 30 + 31) - 9) / 5;
				if (isLeapYear(year))
					dayOfYear++;
	  }
	else
				dayOfYear = 31 * fields[MONTH];

	if (isSet[DAY_OF_MONTH])
	  {
	    return new int[]
	    {
	    dayOfYear + fields[DAY_OF_MONTH], 0};
			}
	if (isSet[WEEK_OF_MONTH] && isSet[DAY_OF_WEEK])
	  {
				// the weekday of the first day in that month is:
				int weekday = getWeekDay(year, ++dayOfYear);

	    return new int[]
	    {
	      dayOfYear,
					// the day of week in the first week
					// (weeks starting on sunday) is:
					fields[DAY_OF_WEEK] - weekday +
					// Now jump to the right week and correct the possible
					// error made by assuming sunday is the first week day.
	      7 * (fields[WEEK_OF_MONTH]
		   + (fields[DAY_OF_WEEK] < getFirstDayOfWeek()? 0 : -1)
		   + (weekday < getFirstDayOfWeek()? -1 : 0))};
			}
	if (isSet[DAY_OF_WEEK] && isSet[DAY_OF_WEEK_IN_MONTH])
	  {
				// the weekday of the first day in that month is:
				int weekday = getWeekDay(year, ++dayOfYear);
	    return new int[] { 
		  dayOfYear,
		  fields[DAY_OF_WEEK] - weekday +
		  7 * (fields[DAY_OF_WEEK_IN_MONTH]
		       + (fields[DAY_OF_WEEK] < weekday ? 0 : -1))};
			}
		}

		// MONTH + something did not succeed.
    if (isSet[DAY_OF_YEAR])
      {
	return new int[] {0, fields[DAY_OF_YEAR]};
		}

    if (isSet[DAY_OF_WEEK] && isSet[WEEK_OF_YEAR])
      {
			int dayOfYear = getMinimalDaysInFirstWeek();
			// the weekday of the day, that begins the first week 
			// in that year is:
			int weekday = getWeekDay(year, dayOfYear);

	return new int[] { 
	    dayOfYear,
				// the day of week in the first week
				// (weeks starting on sunday) is:
				fields[DAY_OF_WEEK] - weekday
				// Now jump to the right week and correct the possible
				// error made by assuming sunday is the first week day.
	    + 7 * (fields[WEEK_OF_YEAR]
		   + (fields[DAY_OF_WEEK] < getFirstDayOfWeek()? 0 : -1)
		   + (weekday < getFirstDayOfWeek()? -1 : 0))};
		}

		// As last resort return Jan, 1st.
    return new int[] {1, 0};
	}

	/**
	 * Converts the time field values (<code>fields</code>) to
	 * milliseconds since the epoch UTC (<code>time</code>). 
   *
   * @throws IllegalArgumentException if any calendar fields
   *         are invalid.
	 */
  protected synchronized void computeTime()
  {
		int era = isSet[ERA] ? fields[ERA] : AD;
		int year = isSet[YEAR] ? fields[YEAR] : 1970;
    if (isLenient() && isSet[MONTH])
      {
	int month = fields[MONTH];
	year += month / 12;
	month %= 12;
	if (month < 0)
	  {
	    month += 12;
	    year--;
	  }
	fields[MONTH] = month;
	isSet[YEAR] = true;
	fields[YEAR] = year;
      }

		if (era == BC)
			year = 1 - year;

		int[] daysOfYear = getDayOfYear(year);

		int hour = 0;
		if (isSet[HOUR_OF_DAY])
			hour = fields[HOUR_OF_DAY];
    else if (isSet[HOUR])
      {
			hour = fields[HOUR];
			if (isSet[AM_PM] && fields[AM_PM] == PM)
	  if (hour != 12) /* not Noon */
				hour += 12;
	/* Fix the problem of the status of 12:00 AM (midnight). */
	if (isSet[AM_PM] && fields[AM_PM] == AM && hour == 12)
	  hour = 0;
		}

		int minute = isSet[MINUTE] ? fields[MINUTE] : 0;
		int second = isSet[SECOND] ? fields[SECOND] : 0;
		int millis = isSet[MILLISECOND] ? fields[MILLISECOND] : 0;
		int millisInDay;

    if (isLenient())
      {
			// prevent overflow
	long allMillis = (((hour * 60L) + minute) * 60L + second) * 1000L
	  + millis;
			daysOfYear[1] += allMillis / (24 * 60 * 60 * 1000L);
			millisInDay = (int) (allMillis % (24 * 60 * 60 * 1000L));
      }
    else
      {
	if (hour < 0 || hour >= 24 || minute < 0 || minute > 59
	    || second < 0 || second > 59 || millis < 0 || millis >= 1000)
				throw new IllegalArgumentException();
			millisInDay = (((hour * 60) + minute) * 60 + second) * 1000 + millis;
		}
		time = getLinearTime(year, daysOfYear[0], millisInDay);

		// Add the relative days after calculating the linear time, to
		// get right behaviour when jumping over the gregorianCutover.
		time += daysOfYear[1] * (24 * 60 * 60 * 1000L);


		TimeZone zone = getTimeZone();
    int rawOffset = isSet[ZONE_OFFSET]
      ? fields[ZONE_OFFSET] : zone.getRawOffset();

    int day = (int) (time / (24 * 60 * 60 * 1000L));
    millisInDay = (int) (time % (24 * 60 * 60 * 1000L));
    if (millisInDay < 0)
      {
	millisInDay += (24 * 60 * 60 * 1000);
	day--;
      }

    int[] f = new int[FIELD_COUNT];
    calculateDay(f, day, time - rawOffset >= gregorianCutover);
    year = f[YEAR];
    int month = f[MONTH];
    day = f[DAY_OF_MONTH];
    int weekday = f[DAY_OF_WEEK];
    int dstOffset = isSet[DST_OFFSET]
      ? fields[DST_OFFSET] : (zone.getOffset((year < 0) ? BC : AD,
					     (year < 0) ? 1 - year : year,
					     month, day, weekday, millisInDay)
			      - zone.getRawOffset());
		time -= rawOffset + dstOffset;
		isTimeSet = true;
	}

	/**
   * <p>
	 * Determines if the given year is a leap year.  
   * </p>
   * <p>
   * To specify a year in the BC era, use a negative value calculated
   * as 1 - y, where y is the required year in BC.  So, 1 BC is 0,
   * 2 BC is -1, 3 BC is -2, etc.
   * </p>
	 *
   * @param year a year (use a negative value for BC).
   * @param gregorian if true, use the gregorian leap year rule.
   * @return true, if the given year is a leap year, false otherwise.  
   */
  private boolean isLeapYear(int year, boolean gregorian)
  {
		if ((year & 3) != 0)
			// Only years divisible by 4 can be leap years
			return false;

		if (!gregorian)
			return true;

		// We rely on AD area here.
		return ((year % 100) != 0 || (year % 400) == 0);
	}

	/**
	 * Get the linear day in days since the epoch, using the
	 * Julian or Gregorian calendar as specified.  If you specify a
	 * nonpositive year it is interpreted as BC as following: 0 is 1
	 * BC, -1 is 2 BC and so on.  
	 *
	 * @param year the year of the date.
	 * @param dayOfYear the day of year of the date; 1 based.
   * @param gregorian <code>true</code>, if we should use the Gregorian rules.
   * @return the days since the epoch, may be negative.  
   */
  private long getLinearDay(int year, int dayOfYear, boolean gregorian)
  {
		// The 13 is the number of days, that were omitted in the Gregorian
		// Calender until the epoch.
		// We shift right by 2 instead of dividing by 4, to get correct
		// results for negative years (and this is even more efficient).
    long julianDay = ((year * (365L * 4 + 1)) >> 2) + dayOfYear -
      ((1970 * (365 * 4 + 1)) / 4 + 1 - 13);

    if (gregorian)
      {
			// subtract the days that are missing in gregorian calendar
			// with respect to julian calendar.
			//
			// Okay, here we rely on the fact that the gregorian
			// calendar was introduced in the AD era.  This doesn't work
			// with negative years.
			//
			// The additional leap year factor accounts for the fact that
			// a leap day is not seen on Jan 1 of the leap year.
			int gregOffset = (year / 400) - (year / 100) + 2;
	if (isLeapYear (year, true) && dayOfYear < 31 + 29)
				--gregOffset;
			julianDay += gregOffset;
		}
		return julianDay;
	}

	/**
	 * Converts the given linear day into era, year, month,
	 * day_of_year, day_of_month, day_of_week, and writes the result
	 * into the fields array.
   *
	 * @param day the linear day.  
   * @param gregorian true, if we should use Gregorian rules.
	 */
  private void calculateDay(int[] fields, long day, boolean gregorian)
  {
		// the epoch is a Thursday.
    int weekday = (int)(day + THURSDAY) % 7;
		if (weekday <= 0)
			weekday += 7;
		fields[DAY_OF_WEEK] = weekday;

		// get a first approximation of the year.  This may be one 
    // year too big.
    int year = 1970 + (int)(gregorian
		       ? ((day - 100) * 400) / (365 * 400 + 100 - 4 + 1)
		       : ((day - 100) * 4) / (365 * 4 + 1));
		if (day >= 0)
			year++;

    long firstDayOfYear = getLinearDay(year, 1, gregorian);

		// Now look in which year day really lies.
    if (day < firstDayOfYear)
      {
			year--;
			firstDayOfYear = getLinearDay(year, 1, gregorian);
		}

		day -= firstDayOfYear - 1; // day of year,  one based.

    fields[DAY_OF_YEAR] = (int)day;
    if (year <= 0)
      {
			fields[ERA] = BC;
			fields[YEAR] = 1 - year;
      }
    else
      {
			fields[ERA] = AD;
			fields[YEAR] = year;
		}

		int leapday = isLeapYear(year, gregorian) ? 1 : 0;
    if (day <= 31 + 28 + leapday)
      {
	fields[MONTH] = (int)day / 32; // 31->JANUARY, 32->FEBRUARY
	fields[DAY_OF_MONTH] = (int)day - 31 * fields[MONTH];
      }
    else
      {
			// A few more magic formulas
	int scaledDay = ((int)day - leapday) * 5 + 8;
			fields[MONTH] = scaledDay / (31 + 30 + 31 + 30 + 31);
			fields[DAY_OF_MONTH] = (scaledDay % (31 + 30 + 31 + 30 + 31)) / 5 + 1;
		}
	}

	/**
	 * Converts the milliseconds since the epoch UTC
	 * (<code>time</code>) to time fields
	 * (<code>fields</code>). 
	 */
  protected synchronized void computeFields()
  {
		boolean gregorian = (time >= gregorianCutover);

		TimeZone zone = getTimeZone();
		fields[ZONE_OFFSET] = zone.getRawOffset();
		long localTime = time + fields[ZONE_OFFSET];

    long day = localTime / (24 * 60 * 60 * 1000L);
		int millisInDay = (int) (localTime % (24 * 60 * 60 * 1000L));
    if (millisInDay < 0)
      {
			millisInDay += (24 * 60 * 60 * 1000);
			day--;
		}

    calculateDay(fields, day, gregorian);
    fields[DST_OFFSET] =
      zone.getOffset(fields[ERA], fields[YEAR], fields[MONTH],
		     fields[DAY_OF_MONTH], fields[DAY_OF_WEEK],
		     millisInDay) - fields[ZONE_OFFSET];

		millisInDay += fields[DST_OFFSET];
    if (millisInDay >= 24 * 60 * 60 * 1000)
      {
			millisInDay -= 24 * 60 * 60 * 1000;
	calculateDay(fields, ++day, gregorian);
		}

		fields[DAY_OF_WEEK_IN_MONTH] = (fields[DAY_OF_MONTH] + 6) / 7;

		// which day of the week are we (0..6), relative to getFirstDayOfWeek
		int relativeWeekday = (7 + fields[DAY_OF_WEEK] - getFirstDayOfWeek()) % 7;

    fields[WEEK_OF_MONTH] = (fields[DAY_OF_MONTH] - relativeWeekday + 12) / 7;

		int weekOfYear = (fields[DAY_OF_YEAR] - relativeWeekday + 6) / 7;

		// Do the Correction: getMinimalDaysInFirstWeek() is always in the 
		// first week.
		int minDays = getMinimalDaysInFirstWeek();
    int firstWeekday =
      (7 + getWeekDay(fields[YEAR], minDays) - getFirstDayOfWeek()) % 7;
		if (minDays - firstWeekday < 1)
			weekOfYear++;
		fields[WEEK_OF_YEAR] = weekOfYear;


		int hourOfDay = millisInDay / (60 * 60 * 1000);
		fields[AM_PM] = (hourOfDay < 12) ? AM : PM;
		int hour = hourOfDay % 12;
		fields[HOUR] = (hour == 0) ? 12 : hour;
		fields[HOUR_OF_DAY] = hourOfDay;
		millisInDay %= (60 * 60 * 1000);
		fields[MINUTE] = millisInDay / (60 * 1000);
		millisInDay %= (60 * 1000);
		fields[SECOND] = millisInDay / (1000);
		fields[MILLISECOND] = millisInDay % 1000;


    areFieldsSet = isSet[ERA] = isSet[YEAR] = isSet[MONTH] =
      isSet[WEEK_OF_YEAR] = isSet[WEEK_OF_MONTH] =
      isSet[DAY_OF_MONTH] = isSet[DAY_OF_YEAR] = isSet[DAY_OF_WEEK] =
      isSet[DAY_OF_WEEK_IN_MONTH] = isSet[AM_PM] = isSet[HOUR] =
      isSet[HOUR_OF_DAY] = isSet[MINUTE] = isSet[SECOND] =
      isSet[MILLISECOND] = isSet[ZONE_OFFSET] = isSet[DST_OFFSET] = true;

	}

	/**
   * Compares the given calendar with this.  An object, o, is
   * equivalent to this if it is also a <code>GregorianCalendar</code>
   * with the same time since the epoch under the same conditions
   * (same change date and same time zone).
   *  
	 * @param o the object to that we should compare.
	 * @return true, if the given object is a calendar, that represents
   * the same time (but doesn't necessarily have the same fields).
   * @throws IllegalArgumentException if one of the fields
   *         <code>ZONE_OFFSET</code> or <code>DST_OFFSET</code> is
   *         specified, if an unknown field is specified or if one
   *         of the calendar fields receives an illegal value when
   *         leniancy is not enabled.
	 */
  public boolean equals(Object o)
  {
		if (!(o instanceof GregorianCalendar))
			return false;

    GregorianCalendar cal = (GregorianCalendar) o;
		return (cal.getTimeInMillis() == getTimeInMillis());
	}

//     /**
//      * Compares the given calender with this.  
//      * @param o the object to that we should compare.
//      * @return true, if the given object is a calendar, and this calendar
//      * represents a smaller time than the calender o.
//      */
//     public boolean before(Object o) {
//         if (!(o instanceof GregorianCalendar))
//             return false;

//         GregorianCalendar cal = (GregorianCalendar) o;
//         return (cal.getTimeInMillis() < getTimeInMillis());
//     }

//     /**
//      * Compares the given calender with this.  
//      * @param o the object to that we should compare.
//      * @return true, if the given object is a calendar, and this calendar
//      * represents a bigger time than the calender o.
//      */
//     public boolean after(Object o) {
//         if (!(o instanceof GregorianCalendar))
//             return false;

//         GregorianCalendar cal = (GregorianCalendar) o;
//         return (cal.getTimeInMillis() > getTimeInMillis());
//     }

	/**
	 * Adds the specified amount of time to the given time field.  The
	 * amount may be negative to subtract the time.  If the field overflows
	 * it does what you expect: Jan, 25 + 10 Days is Feb, 4.
   * @param field one of the time field constants.
   * @param amount the amount of time to add.
   * @exception IllegalArgumentException if <code>field</code> is 
   *   <code>ZONE_OFFSET</code>, <code>DST_OFFSET</code>, or invalid; or
   *   if <code>amount</code> contains an out-of-range value and the calendar
   *   is not in lenient mode.
	 */
  public void add(int field, int amount)
  {
    switch (field)
      {
      case YEAR:
				complete();
				fields[YEAR] += amount;
				isTimeSet = false;
				break;
      case MONTH:
				complete();
				int months = fields[MONTH] + amount;
				fields[YEAR] += months / 12;
				fields[MONTH] = months % 12;
	if (fields[MONTH] < 0)
	  {
					fields[MONTH] += 12;
					fields[YEAR]--;
				}
				isTimeSet = false;
				int maxDay = getActualMaximum(DAY_OF_MONTH);
	if (fields[DAY_OF_MONTH] > maxDay)
	  {
					fields[DAY_OF_MONTH] = maxDay;
					isTimeSet = false;
				}
				break;
      case DAY_OF_MONTH:
      case DAY_OF_YEAR:
      case DAY_OF_WEEK:
				if (!isTimeSet)
					computeTime();
				time += amount * (24 * 60 * 60 * 1000L);
				areFieldsSet = false;
				break;
      case WEEK_OF_YEAR:
      case WEEK_OF_MONTH:
      case DAY_OF_WEEK_IN_MONTH:
				if (!isTimeSet)
					computeTime();
				time += amount * (7 * 24 * 60 * 60 * 1000L);
				areFieldsSet = false;
				break;
      case AM_PM:
				if (!isTimeSet)
					computeTime();
				time += amount * (12 * 60 * 60 * 1000L);
				areFieldsSet = false;
				break;
      case HOUR:
      case HOUR_OF_DAY:
				if (!isTimeSet)
					computeTime();
				time += amount * (60 * 60 * 1000L);
				areFieldsSet = false;
				break;
      case MINUTE:
				if (!isTimeSet)
					computeTime();
				time += amount * (60 * 1000L);
				areFieldsSet = false;
				break;
      case SECOND:
				if (!isTimeSet)
					computeTime();
				time += amount * (1000L);
				areFieldsSet = false;
				break;
      case MILLISECOND:
				if (!isTimeSet)
					computeTime();
				time += amount;
				areFieldsSet = false;
				break;
      case ZONE_OFFSET:
      case DST_OFFSET:
      default:
	throw new IllegalArgumentException("Invalid or unknown field");
		}
	}


	/**
	 * Rolls the specified time field up or down.  This means add one
	 * to the specified field, but don't change the other fields.  If
	 * the maximum for this field is reached, start over with the 
	 * minimum value.  
	 *
	 * <strong>Note:</strong> There may be situation, where the other
	 * fields must be changed, e.g rolling the month on May, 31. 
	 * The date June, 31 is automatically converted to July, 1. 
	 * This requires lenient settings.
	 *
	 * @param field the time field. One of the time field constants.
	 * @param up the direction, true for up, false for down.
   * @throws IllegalArgumentException if one of the fields
   *         <code>ZONE_OFFSET</code> or <code>DST_OFFSET</code> is
   *         specified, if an unknown field is specified or if one
   *         of the calendar fields receives an illegal value when
   *         leniancy is not enabled.
	 */
  public void roll(int field, boolean up)
  {
		roll(field, up ? 1 : -1);
	}

  /**
   * Checks that the fields are still within their legal bounds,
   * following use of the <code>roll()</code> method.
   *
   * @param field the field to check.
   * @param delta multipler for alterations to the <code>time</code>.
   * @see #roll(int, boolean)
   * @see #roll(int, int)
   */
  private void cleanUpAfterRoll(int field, int delta)
  {
    switch (field)
      {
      case ERA:
      case YEAR:
      case MONTH:
				// check that day of month is still in correct range
				if (fields[DAY_OF_MONTH] > getActualMaximum(DAY_OF_MONTH))
					fields[DAY_OF_MONTH] = getActualMaximum(DAY_OF_MONTH);
				isTimeSet = false;
				isSet[WEEK_OF_MONTH] = false;
				isSet[DAY_OF_WEEK] = false;
				isSet[DAY_OF_WEEK_IN_MONTH] = false;
				isSet[DAY_OF_YEAR] = false;
				isSet[WEEK_OF_YEAR] = false;
				break;

      case DAY_OF_MONTH:
				isSet[WEEK_OF_MONTH] = false;
				isSet[DAY_OF_WEEK] = false;
				isSet[DAY_OF_WEEK_IN_MONTH] = false;
				isSet[DAY_OF_YEAR] = false;
				isSet[WEEK_OF_YEAR] = false;
				time += delta * (24 * 60 * 60 * 1000L);
				break;

      case WEEK_OF_MONTH:
				isSet[DAY_OF_MONTH] = false;
				isSet[DAY_OF_WEEK_IN_MONTH] = false;
				isSet[DAY_OF_YEAR] = false;
				isSet[WEEK_OF_YEAR] = false;
				time += delta * (7 * 24 * 60 * 60 * 1000L);
				break;
      case DAY_OF_WEEK_IN_MONTH:
				isSet[DAY_OF_MONTH] = false;
				isSet[WEEK_OF_MONTH] = false;
				isSet[DAY_OF_YEAR] = false;
				isSet[WEEK_OF_YEAR] = false;
				time += delta * (7 * 24 * 60 * 60 * 1000L);
				break;
      case DAY_OF_YEAR:
				isSet[MONTH] = false;
				isSet[DAY_OF_MONTH] = false;
				isSet[WEEK_OF_MONTH] = false;
				isSet[DAY_OF_WEEK_IN_MONTH] = false;
				isSet[DAY_OF_WEEK] = false;
				isSet[WEEK_OF_YEAR] = false;
				time += delta * (24 * 60 * 60 * 1000L);
				break;
      case WEEK_OF_YEAR:
				isSet[MONTH] = false;
				isSet[DAY_OF_MONTH] = false;
				isSet[WEEK_OF_MONTH] = false;
				isSet[DAY_OF_WEEK_IN_MONTH] = false;
				isSet[DAY_OF_YEAR] = false;
				time += delta * (7 * 24 * 60 * 60 * 1000L);
				break;

      case AM_PM:
				isSet[HOUR_OF_DAY] = false;
				time += delta * (12 * 60 * 60 * 1000L);
				break;
      case HOUR:
				isSet[HOUR_OF_DAY] = false;
				time += delta * (60 * 60 * 1000L);
				break;
      case HOUR_OF_DAY:
				isSet[HOUR] = false;
				isSet[AM_PM] = false;
				time += delta * (60 * 60 * 1000L);
				break;

      case MINUTE:
				time += delta * (60 * 1000L);
				break;
      case SECOND:
				time += delta * (1000L);
				break;
      case MILLISECOND:
				time += delta;
				break;
		}
	}

	/**
	 * Rolls the specified time field by the given amount.  This means
	 * add amount to the specified field, but don't change the other
	 * fields.  If the maximum for this field is reached, start over
	 * with the minimum value and vice versa for negative amounts.
	 *
	 * <strong>Note:</strong> There may be situation, where the other
	 * fields must be changed, e.g rolling the month on May, 31. 
	 * The date June, 31 is automatically corrected to June, 30.
	 *
	 * @param field the time field. One of the time field constants.
	 * @param amount the amount by which we should roll.
   * @throws IllegalArgumentException if one of the fields
   *         <code>ZONE_OFFSET</code> or <code>DST_OFFSET</code> is
   *         specified, if an unknown field is specified or if one
   *         of the calendar fields receives an illegal value when
   *         leniancy is not enabled.
	 */
  public void roll(int field, int amount)
  {
    switch (field)
      {
      case DAY_OF_WEEK:
				// day of week is special: it rolls automatically
				add(field, amount);
				return;
      case ZONE_OFFSET:
      case DST_OFFSET:
				throw new IllegalArgumentException("Can't roll time zone");
		}
		complete();
		int min = getActualMinimum(field);
		int range = getActualMaximum(field) - min + 1;
		int oldval = fields[field];
		int newval = (oldval - min + range + amount) % range + min;
		if (newval < min)
			newval += range;
		fields[field] = newval;
		cleanUpAfterRoll(field, newval - oldval);
	}

  /**
   * The minimum values for the calendar fields.
   */
  private static final int[] minimums =
      { BC,       1,  0,  0, 1,  1,   1,   SUNDAY, 1, 
        AM,  1,  0,  1,  1,   1, -(12*60*60*1000),               0 };

  /**
   * The maximum values for the calendar fields.
   */
  private static final int[] maximums =
      { AD, 5000000, 11, 53, 5, 31, 366, SATURDAY, 5, 
        PM, 12, 23, 59, 59, 999, +(12*60*60*1000), (12*60*60*1000) };

	/**
	 * Gets the smallest value that is allowed for the specified field.
   *
   * @param field one of the time field constants.
   * @return the smallest value for the specified field.
	 */
  public int getMinimum(int field)
  {
		return minimums[field];
	}

	/**
	 * Gets the biggest value that is allowed for the specified field.
   *
   * @param field one of the time field constants.
	 * @return the biggest value.
	 */
  public int getMaximum(int field)
  {
		return maximums[field];
	}


	/**
	 * Gets the greatest minimum value that is allowed for the specified field.
   * This is the largest value returned by the <code>getActualMinimum(int)</code>
   * method.
   *
	 * @param field the time field. One of the time field constants.
	 * @return the greatest minimum value.
   * @see #getActualMinimum(int)
	 */
  public int getGreatestMinimum(int field)
  {
		if (field == WEEK_OF_YEAR)
			return 1;
		return minimums[field];
	}

	/**
	 * Gets the smallest maximum value that is allowed for the
   * specified field.  This is the smallest value returned
   * by the <code>getActualMaximum(int)</code>.  For example,
   * this is 28 for DAY_OF_MONTH (as all months have at least
   * 28 days).
   *
	 * @param field the time field. One of the time field constants.
	 * @return the least maximum value.  
   * @see #getActualMaximum(int)
   * @since 1.2
	 */
  public int getLeastMaximum(int field)
  {
    switch (field)
      {
      case WEEK_OF_YEAR:
				return 52;
      case DAY_OF_MONTH:
				return 28;
      case DAY_OF_YEAR:
				return 365;
      case DAY_OF_WEEK_IN_MONTH:
      case WEEK_OF_MONTH:
				return 4;
      default:
				return maximums[field];
		}
	}

	/**
	 * Gets the actual minimum value that is allowed for the specified field.
	 * This value is dependent on the values of the other fields.  Note that
	 * this calls <code>complete()</code> if not enough fields are set.  This
   * can have ugly side effects.  The value given depends on the current
   * time used by this instance.
   *
	 * @param field the time field. One of the time field constants.
	 * @return the actual minimum value.
   * @since 1.2
	 */
  public int getActualMinimum(int field)
  {
    if (field == WEEK_OF_YEAR)
      {
			int min = getMinimalDaysInFirstWeek();
			if (min == 0)
				return 1;
			if (!areFieldsSet || !isSet[ERA] || !isSet[YEAR])
				complete();

			int year = fields[ERA] == AD ? fields[YEAR] : 1 - fields[YEAR];
			int weekday = getWeekDay(year, min);
			if ((7 + weekday - getFirstDayOfWeek()) % 7 >= min - 1)
				return 1;
			return 0;
		}
		return minimums[field];
	}

	/**
	 * Gets the actual maximum value that is allowed for the specified field.
	 * This value is dependent on the values of the other fields.  Note that
	 * this calls <code>complete()</code> if not enough fields are set.  This
   * can have ugly side effects.  The value given depends on the current time
   * used by this instance; thus, leap years have a maximum day of month value of
   * 29, rather than 28.
   *
	 * @param field the time field. One of the time field constants.
	 * @return the actual maximum value.  
	 */
  public int getActualMaximum(int field)
  {
    switch (field)
      {
      case WEEK_OF_YEAR:
				{
					if (!areFieldsSet || !isSet[ERA] || !isSet[YEAR])
						complete();
					// This is wrong for the year that contains the gregorian change.
					// I.e it gives the weeks in the julian year or in the gregorian
					// year in that case.
					int year = fields[ERA] == AD ? fields[YEAR] : 1 - fields[YEAR];
					int lastDay = isLeapYear(year) ? 366 : 365;
					int weekday = getWeekDay(year, lastDay);
	  int week = (lastDay + 6
		      - (7 + weekday - getFirstDayOfWeek()) % 7) / 7;

					int minimalDays = getMinimalDaysInFirstWeek();
					int firstWeekday = getWeekDay(year, minimalDays);
	  /* 
	   * Is there a set of days at the beginning of the year, before the
	   * first day of the week, equal to or greater than the minimum number
	   * of days required in the first week?
	   */
					if (minimalDays - (7 + firstWeekday - getFirstDayOfWeek()) % 7 < 1)
	    return week + 1; /* Add week 1: firstWeekday through to firstDayOfWeek */
				}
	case DAY_OF_MONTH:
				{
					if (!areFieldsSet || !isSet[MONTH])
						complete();
					int month = fields[MONTH];
					// If you change this, you should also change 
					// SimpleTimeZone.getDaysInMonth();
	  if (month == FEBRUARY)
	    {
						if (!isSet[YEAR] || !isSet[ERA])
							complete();
						int year = fields[ERA] == AD ? fields[YEAR] : 1 - fields[YEAR];
						return isLeapYear(year) ? 29 : 28;
	    }
	  else if (month < AUGUST)
						return 31 - (month & 1);
					else
						return 30 + (month & 1);
				}
      case DAY_OF_YEAR:
				{
					if (!areFieldsSet || !isSet[ERA] || !isSet[YEAR])
						complete();
					int year = fields[ERA] == AD ? fields[YEAR] : 1 - fields[YEAR];
					return isLeapYear(year) ? 366 : 365;
				}
      case DAY_OF_WEEK_IN_MONTH:
				{
					// This is wrong for the month that contains the gregorian change.
					int daysInMonth = getActualMaximum(DAY_OF_MONTH);
					// That's black magic, I know
					return (daysInMonth - (fields[DAY_OF_MONTH] - 1) % 7 + 6) / 7;
				}
      case WEEK_OF_MONTH:
				{
					int daysInMonth = getActualMaximum(DAY_OF_MONTH);
	  int weekday = (daysInMonth - fields[DAY_OF_MONTH]
			 + fields[DAY_OF_WEEK] - SUNDAY) % 7 + SUNDAY;
	  return (daysInMonth + 6
		  - (7 + weekday - getFirstDayOfWeek()) % 7) / 7;
				}
      default:
				return maximums[field];
		}
	}


}
