/* TimeType.java -- 
   Copyright (C) 2006  Free Software Foundation, Inc.

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

package gnu.xml.validation.datatype;

import java.util.TimeZone;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

/**
 * The XML Schema time type.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
final class TimeType
  extends AtomicSimpleType
{

  static class Time
    implements Comparable
  {
    int minutes;
    float seconds;

    public int hashCode()
    {
      return minutes * 31 + new Float(seconds).hashCode();
    }

    public boolean equals(Object other)
    {
      if (other instanceof Time)
        {
          Time time = (Time) other;
          return time.minutes == minutes && time.seconds == seconds;
        }
      return false;
    }

    public int compareTo(Object other)
    {
      if (other instanceof Time)
        {
          Time time = (Time) other;
          if (time.minutes != minutes)
            return minutes - time.minutes;
          if (time.seconds == seconds)
            return 0;
          return (seconds < time.seconds) ? -1 : 1;
        }
      return 0;
    }
    
  }

  static final int[] CONSTRAINING_FACETS = {
    Facet.PATTERN,
    Facet.ENUMERATION,
    Facet.WHITESPACE,
    Facet.MAX_INCLUSIVE,
    Facet.MAX_EXCLUSIVE,
    Facet.MIN_INCLUSIVE,
    Facet.MIN_EXCLUSIVE
  };

  TimeType()
  {
    super(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "time"),
          TypeLibrary.ANY_SIMPLE_TYPE);
  }

  public int[] getConstrainingFacets()
  {
    return CONSTRAINING_FACETS;
  }

  public void checkValid(String value, ValidationContext context)
    throws DatatypeException
  {
    super.checkValid(value, context);
    int len = value.length();
    int state = 3;
    int start = 0;
    for (int i = 0; i < len; i++)
      {
        char c = value.charAt(i);
        if (c == '-' && state == 0)
          {
            start++;
            continue;
          }
        if (c >= 0x30 && c <= 0x39)
          continue;
        switch (state)
          {
          case 3: // hour
            if (c == ':')
              {
                if (i - start != 2)
                  throw new DatatypeException(i, "invalid time value");
                state = 4;
                start = i + 1;
                continue;
              }
            break;
          case 4: // minute
            if (c == ':')
              {
                if (i - start != 2)
                  throw new DatatypeException(i, "invalid time value");
                state = 5;
                start = i + 1;
                continue;
              }
            break;
          case 5: // second
            if (c == '.')
              {
                if (i - start != 2)
                  throw new DatatypeException(i, "invalid time value");
                state = 6;
                start = i + 1;
                continue;
              }
            else if (c == ' ')
              {
                if (i - start != 2)
                  throw new DatatypeException(i, "invalid time value");
                state = 7;
                start = i + 1;
                continue;
              }
            break;
          case 6: // second fraction
            if (c == ' ')
              {
                state = 7;
                start = i + 1;
                continue;
              }
            break;
          case 7: // timezone 1
            if (start == i)
              {
                if (c == '+' || c == '-')
                  continue;
                else if (c == 'Z')
                  {
                    state = 9;
                    start = i + 1;
                    continue;
                  }
              }
            if (c == ':')
              {
                if (i - start != 2)
                  throw new DatatypeException(i, "invalid time value");
                state = 8;
                start = i + 1;
                continue;
              }
            break;
          }
        throw new DatatypeException(i, "invalid time value");
      }
    switch (state)
      {
      case 5: // second
        if (len - start != 2)
          throw new DatatypeException(len, "invalid time value");
        break;
      case 6: // second fraction
        break;
      case 8: // timezone 2
        if (len - start != 2)
          throw new DatatypeException(len, "invalid time value");
        break;
      case 9: // post Z
        break;
      default:
        throw new DatatypeException(len, "invalid time value");
      }
  }
  
  public Object createValue(String value, ValidationContext context) {
    int len = value.length();
    int state = 3;
    int start = 0;
    Time time = new Time();
    try
      {
        for (int i = 0; i < len; i++)
          {
            char c = value.charAt(i);
            if (c >= 0x30 && c <= 0x39)
              continue;
            switch (state)
              {
              case 3: // hour
                if (c == ':')
                  {
                    time.minutes =
                      Integer.parseInt(value.substring(start, i)) * 60;
                    state = 4;
                    start = i + 1;
                    continue;
                  }
                break;
              case 4: // minute
                if (c == ':')
                  {
                    time.minutes +=
                      Integer.parseInt(value.substring(start, i));
                    state = 5;
                    start = i + 1;
                    continue;
                  }
                break;
              case 5: // second
                if (c == ' ')
                  {
                    time.seconds =
                      Float.parseFloat(value.substring(start, i));
                    state = 7;
                    start = i + 1;
                    continue;
                  }
                break;
              }
          }
        // end of input
        if (len - start > 0 && state == 7)
          {
            // Timezone
            String timezone = value.substring(len - start);
            int i = timezone.indexOf(':');
            if (i == -1)
              {
                if ("Z".equals(timezone))
                  timezone = "UTC";
                TimeZone tz = TimeZone.getTimeZone(timezone);
                if (tz == null)
                  return null;
                time.minutes += tz.getRawOffset();
              }
            else
              {
                String tzh = timezone.substring(0, i);
                String tzm = timezone.substring(i + 1);
                int offset = Integer.parseInt(tzh) * 60;
                if (offset < 0)
                  offset -= Integer.parseInt(tzm);
                else
                  offset += Integer.parseInt(tzm);
                time.minutes += offset;
              }
          }
        return time;
      }
    catch (NumberFormatException e)
      {
        return null;
      }
  }
  
}

