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
 
package java.net;

import gnu.classpath.Configuration;

import java.io.IOException;
import java.io.InputStream;

final class VMURLConnection
{
  public static final int LENGTH = 1024;

  static
  {
    init();
  }

  private static void init(){
      //todo anything to do here?
  };

  private static String guessContentTypeFromBuffer(byte[] b, int valid){
      //todo implement it
      return null;
  }

  /**
   * This is called from URLConnection to guess the mime type of a
   * stream.  This method may return null to indicate that it could
   * not guess a type.
   */
  static String guessContentTypeFromStream(InputStream is)
    throws IOException
  {
    if (! is.markSupported())
      return null;
    is.mark(LENGTH);
    byte[] bytes = new byte[LENGTH];
    int r = is.read(bytes);
    if (r < 0)
      return null;
    is.reset();
    return guessContentTypeFromBuffer(bytes, r);
  }
}
