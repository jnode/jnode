
// (C) 1996 Glynn Clements <glynn@sensei.co.uk> - Freely Redistributable

package java.util;

//import kore.util.DateParser;

public class Date implements Cloneable {
	// Constants

	private final static String[] days = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

	private final static String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	// Private Class Methods

	private static String fixed_int(int i) {
		return (i > 10) ? ("" + i) : ("0" + i);
	}

	// Static Fields

	private static int timezone; // timezone offset (excluding DST)
	private static String[] tzname; // timezone names

	static {
		timezone = 0;
		tzname = new String[2];
	}

	// Fields

	private long millis; // milliseconds since 1970-1-1
	private boolean isGMT; // true if this Date represents GMT

	private int year; // tm_year
	private int month; // tm_mon
	private int date; // tm_mday
	private int hrs; // tm_hour
	private int min; // tm_min
	private int sec; // tm_sec
	private int day; // tm_wday
	private int yday; // tm_yday
	private boolean isdst; // tm_isdst

	private boolean valid; // broken-down time is valid
	private boolean mvalid; // millisecond count is valid

	// Private Methods

	private final static int daysIn1970Years = 719528;
	private final static int daysIn400Years = 146097;
	private final static int daysIn100Years = 36524;
	private final static int daysIn4Years = 1460;
	private final static int daysInYear = 365;

	private final static int[] daysInMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	private void refresh_millis() {
		if (mvalid)
			return;

		int year = this.year + 1900;

		int c400 = year / 400;
		int c100 = (year % 400) / 100;
		int c4 = (year % 100) / 4;
		int c1 = year % 4;

		boolean isLeapCentury = (c100 == 0);
		boolean isLeapCycle = (isLeapCentury || c4 > 0);
		boolean isLeapYear = (isLeapCycle && c1 == 0);

		int days =
			c400 * daysIn400Years
				+ c100 * daysIn100Years
				+ (!isLeapCentury ? 1 : 0)
				+ c4 * (daysIn4Years + 1)
				- ((!isLeapCentury && c4 > 0) ? 1 : 0)
				+ c1 * daysInYear
				+ ((isLeapCycle && c1 > 0) ? 1 : 0);

		for (int i = 0; i < this.month; i++)
			days += daysInMonth[i] + (isLeapYear && i == 1 ? 1 : 0);

		days += this.date - 1;

		this.yday = days;

		this.day = (days + 6) % 7;

		int hours = days * 24 + this.hrs;
		int minutes = hours * 60 + this.min;
		int seconds = minutes * 60 + this.sec;

		this.isdst = !isGMT /*&& NativeUtil.getTZIsDST(seconds)*/;

		seconds -= timezone * 60 + (isdst ? 60 * 60 : 0);

		this.millis = seconds * 1000;

		mvalid = true;
	}

	private void refresh_components() {
		if (valid)
			return;

		refresh_millis();

		int n = (int) (this.millis / 1000) + (getTimezoneOffset() * 60);

		this.sec = n % 60;
		n /= 60;

		this.min = n % 60;
		n /= 60;

		this.hrs = n % 24;
		n /= 24;

		// n == days since 1970-01-01

		int days = n + daysIn1970Years;
		// days == days since 0000-01-01

		this.day = (days + 6) % 7;
		// 0000-01-01 is a Saturday => 6

		int c400 = days / daysIn400Years;
		// c400 == 400YrCycle

		days -= c400 * daysIn400Years;
		// days == days % 400 years ( == leap year cycle)

		int c100 = (days == 0) ? 0 : ((days - 1) / daysIn100Years);
		// c100 == century % 4

		boolean isLeapCentury = (c100 == 0);

		days -= (c100 * daysIn100Years + (isLeapCentury ? 0 : 1));
		// days == days % 100 years

		int c4 = (days + (isLeapCentury ? 0 : 1)) / (daysIn4Years + 1);
		// c4 == 4YrCycles % 25

		boolean isLeapCycle = (isLeapCentury || c4 > 0);

		days -= (c4 * (daysIn4Years + 1) - (isLeapCycle ? 0 : 1));
		// days == days % 4 years

		int c1 = (days == 0) ? 0 : ((days - (isLeapCycle ? 1 : 0)) / daysInYear);
		// c1 == Years % 4

		boolean isLeapYear = (isLeapCycle && c1 == 0);

		days -= (c1 * daysInYear + ((isLeapCycle && c1 > 0) ? 1 : 0));
		// days == days % 365/366

		this.year = (400 * c400) + (c100 * 100) + (c4 * 4) + c1 - 1900;

		this.yday = days;

		for (int i = 0; i < 12; i++) {
			n = daysInMonth[i];
			if (isLeapYear && i == 1)
				n++;
			if (days < n) {
				this.month = i;
				break;
			}
			days -= n;
		}

		this.date = ++days;

		valid = true;
	}

	private void invalidate() {
		mvalid = false;
		valid = false;
	}

	// Public Class Methods

	public synchronized static long parse(String string) {
		return new DateParser(string).parse();
	}

	public static long UTC(int year, int month, int date, int hrs, int min, int sec) {
		Date d = new Date(year, month, date, hrs, min, sec);
		d.isGMT = true;
		return d.getTime();
	}

	// Public Instance Methods

	public int hashCode() {
		refresh_millis();
		return (int) (millis ^ (millis >>> 32));
	}

	public int getTimezoneOffset() {
		if (isGMT)
			return 0;

		refresh_millis();

		return timezone + (isdst ? 60 : 0);
	}

	public String toString() {
		refresh_components();

		return days[day]
			+ " "
			+ months[month]
			+ " "
			+ fixed_int(date)
			+ " "
			+ fixed_int(hrs)
			+ ":"
			+ fixed_int(min)
			+ ":"
			+ fixed_int(sec)
			+ " "
			+ tzname[isdst
			? 1
			: 0] + " " + (1900 + year);
	}

	public String toGMTString() {
		if (!isGMT) {
			Date d = new Date(this.millis);
			d.isGMT = true;
			return d.toGMTString();
		}

		refresh_components();

		return (date) + " " + months[month] + " " + (1900 + year) + " " + fixed_int(hrs) + ":" + fixed_int(min) + ":" + fixed_int(sec) + " " + "GMT";
	}

	public String toLocaleString() {
		return toString();
	}

	public boolean after(Date when) {
		return getTime() > when.getTime();
	}

	public boolean before(Date when) {
		return getTime() < when.getTime();
	}

	/**
	 * Compares two dates.
	 * @param when the other date.
	 * @return 0, if the date represented
	 * by obj is exactly the same as the time represented by this
	 * object, a negative if this Date is before the other Date, and
	 * a positive value otherwise.  
	 */
	public int compareTo(Date when) {
		return (getTime() < when.getTime()) ? -1 : (getTime() == when.getTime()) ? 0 : 1;
	}

	/**
	 * Compares this Date to another.  This behaves like
	 * <code>compareTo(Date)</code>, but it may throw a
	 * <code>ClassCastException</code>
	 * @param obj the other date.
	 * @return 0, if the date represented
	 * by obj is exactly the same as the time represented by this
	 * object, a negative if this Date is before the other Date, and
	 * a positive value otherwise.  
	 * @exception ClassCastException if obj is not of type Date.
	 */
	public int compareTo(Object obj) {
		return compareTo((Date) obj);
	}

	public boolean equals(Object obj) {
		return getTime() == ((Date) obj).getTime();
	}

	public long getTime() {
		refresh_millis();
		return millis;
	}

	public int getYear() {
		refresh_components();
		return year;
	}

	public int getMonth() {
		refresh_components();
		return month;
	}

	public int getDate() {
		refresh_components();
		return date;
	}

	public int getHours() {
		refresh_components();
		return hrs;
	}

	public int getMinutes() {
		refresh_components();
		return min;
	}

	public int getSeconds() {
		refresh_components();
		return sec;
	}

	public int getDay() {
		refresh_components();
		return day;
	}

	public void setTime(long millis) {
		this.millis = millis;
		valid = false;
		mvalid = true;
	}

	public void setYear(int year) {
		this.year = year;
		invalidate();
	}

	public void setMonth(int month) {
		this.month = month;
		invalidate();
	}

	public void setDate(int date) {
		this.date = date;
		invalidate();
	}

	public void setHours(int hours) {
		this.hrs = hours;
		invalidate();
	}

	public void setMinutes(int minutes) {
		this.min = minutes;
		invalidate();
	}

	public void setSeconds(int seconds) {
		this.sec = seconds;
		invalidate();
	}

	// Constructors

	public Date(int year, int month, int date, int hrs, int min, int sec) {
		this.year = year;
		this.month = month;
		this.date = date;
		this.hrs = hrs;
		this.min = min;
		this.sec = sec;
	}

	public Date(int year, int month, int date, int hrs, int min) {
		this(year, month, date, hrs, min, 0);
	}

	public Date(int year, int month, int date) {
		this(year, month, date, 0, 0, 0);
	}

	public Date(long millis) {
		this.millis = millis;
		this.mvalid = true;
	}

	public Date(String s) {
		this(parse(s));
	}

	public Date() {
		this(System.currentTimeMillis());
	}

	final static class DateParser {
		private final static String EOF = "EOF".intern();
		private final static String space = " ".intern();
		private final static String plus = "+".intern();
		private final static String minus = "-".intern();
		private final static String comma = ",".intern();
		private final static String slash = "/".intern();
		private final static String colon = ":".intern();

		private final static String GMT = "GMT".intern();
		private final static String UTC = "UTC".intern();
		private final static String UT = "UT".intern();

		private final static String EST = "EST".intern();
		private final static String CST = "CST".intern();
		private final static String MST = "MST".intern();
		private final static String PST = "PST".intern();

		private final static String EDT = "EST".intern();
		private final static String CDT = "CST".intern();
		private final static String MDT = "MST".intern();
		private final static String PDT = "PST".intern();

		private final static String AM = "AM".intern();
		private final static String PM = "PM".intern();

		private final static int TZ_UNKNOWN = Integer.MAX_VALUE;

		private String string;
		private int length;
		private int index = 0;

		private int tz = TZ_UNKNOWN;
		private int year = -1;
		private int month = -1;
		private int day = -1;
		private int hour = -1;
		private int minute = -1;
		private int second = -1;

		private int read0() {
			return (index >= length) ? -1 : (int) string.charAt(index++);
		}

		private void unread0() {
			index--;
		}

		private String getParentheses() {
			while (true) {
				int c = read0();
				if (c == -1)
					return EOF;
				if (c == ')')
					return space;
				if (c == '(')
					getParentheses();
			}
		}

		private String getSpace() {
			while (Character.isSpaceChar((char) read0()));
			unread0();
			return space;
		}

		private Integer getInt(int initial) {
			int value = initial - '0';

			while (true) {
				int c = read0();
				if (c < '0' || c > '9') {
					unread0();
					return new Integer(value);
				}
				value *= 10;
				value += c - '0';
			}
		}

		private String getWord(int initial) {
			StringBuffer buff = new StringBuffer();
			buff.append((char) initial);

			while (true) {
				int c = read0();
				if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z')) {
					unread0();
					return buff.toString().toUpperCase().intern();
				}
				buff.append((char) c);
			}
		}

		private Object getToken() {
			while (true) {
				int c = read0();
				switch (c) {
					case -1 :
						return EOF;
					case '+' :
						return plus;
					case '-' :
						return minus;
					case ',' :
						return comma;
					case '/' :
						return slash;
					case ':' :
						return colon;
					case '(' :
						return getParentheses();
					default :
						if (c >= '0' && c <= '9')
							return getInt(c);
						if (c >= 'A' && c <= 'Z')
							return getWord(c);
						if (c >= 'a' && c <= 'z')
							return getWord(c);
						if (Character.isSpaceChar((char) c))
							return getSpace();
						throw new IllegalArgumentException("Invalid character in date:" + new Character((char) c));
				}
			}
		}

		private String[] days = { "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", };

		private boolean isDay(String s) {
			for (int i = 0; i < 7; i++)
				if (days[i].startsWith(s))
					return true;
			return false;
		}

		private String[] months = { "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER" };

		private int parseMonth(String s) {
			for (int i = 0; i < 12; i++)
				if (months[i].startsWith(s))
					return i;
			return -1;
		}

		public long parse() {
			Vector tokens = new Vector();
			while (true) {
				Object token = getToken();
				tokens.addElement(token);
				if (token == EOF)
					break;
			}

			int length = tokens.size();
			for (int i = 0; i < length; i++) {
				Object token = tokens.elementAt(i);
				if (token == EOF)
					break;

				Object next = tokens.elementAt(i + 1);

				if (token == GMT || token == UTC || token == UT) {
					tz = 0;
					continue;
				}

				if (token == plus || token == minus) {
					if (next instanceof Integer) {
						int n = ((Integer) next).intValue();
						tz = n;
						if (tz < 24)
							tz *= 60;
						else
							tz = (tz / 100) * 60 + (tz % 100);
						if (token == minus)
							tz = -tz;
						i++;
					}
					continue;
				}

				if (token instanceof Integer) {
					int n = ((Integer) token).intValue();

					if (n > 70 && next == space || next == comma || next == slash || next == EOF) {
						if (n > 1900)
							n -= 1900;
						year = n;
					} else if (next == colon) {
						if (hour < 0)
							hour = n;
						else
							minute = n;
						i++;
					} else if (next == slash) {
						if (month < 0)
							month = n - 1;
						else
							day = n;
						i++;
					} else if (next == space || next == comma || next == minus || next == EOF) {
						if (hour >= 0 && minute < 0)
							minute = n;
						else if (minute >= 0 && second < 0)
							second = n;
						else
							day = n;
					}
					continue;
				}

				if (token instanceof String) {
					String word = (String) token;

					if (word == AM) {
						if (hour < 1 || hour > 12)
							throw new IllegalArgumentException("AM with invalid hour in date");
						continue;
					}

					if (word == PM) {
						if (hour < 1 || hour > 12)
							throw new IllegalArgumentException("PM with invalid hour in date");
						hour += 12;
						continue;
					}

					if (word == EDT) {
						tz = -240;
						continue;
					}

					if (word == EST || word == CDT) {
						tz = -300;
						continue;
					}

					if (word == CST || word == MDT) {
						tz = -360;
						continue;
					}

					if (word == MST || word == PDT) {
						tz = -420;
						continue;
					}

					if (word == PST) {
						tz = -480;
						continue;
					}

					if (isDay(word))
						continue;

					int m = parseMonth(word);
					if (m >= 0)
						month = m;
				}
			}

			if (year < 0)
				throw new IllegalArgumentException("no year in date");

			if (month < 0)
				throw new IllegalArgumentException("no month in date");

			if (day < 0)
				throw new IllegalArgumentException("no day of month in date");

			if (hour < 0)
				hour = 0;

			if (minute < 0)
				minute = 0;

			if (second < 0)
				second = 0;

			return (tz == TZ_UNKNOWN)
				? new Date(year, month, day, hour, minute, second).getTime()
				: (Date.UTC(year, month, day, hour, minute, second) - 60000 * tz);
		}

		public DateParser(String string) {
			this.string = string;
		}
	}
	
	public Object clone() {
	    try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
	}
}
