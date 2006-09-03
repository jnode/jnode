/* Channels.java -- 
   Copyright (C) 2002, 2003, 2004, 2005  Free Software Foundation, Inc.

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


package java.nio.channels;

import gnu.java.nio.ChannelReader;
import gnu.java.nio.ChannelWriter;
import gnu.java.nio.InputStreamChannel;
import gnu.java.nio.OutputStreamChannel;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;


/**
 * @since 1.4
 */
public final class Channels
{
	/**
   * This class isn't intended to be instantiated.
   */
  private Channels()
  {
    // Do nothing here.
  }

  /**
	 * Constructs a stream that reads bytes from the given channel.
	 */
  public static InputStream newInputStream(ReadableByteChannel ch)
  {
    return VMChannels.newInputStream(ch);
	}

	/**
	 * Constructs a stream that writes bytes to the given channel.
	 */
  public static OutputStream newOutputStream(WritableByteChannel ch)
  {
    return VMChannels.newOutputStream(ch);
	}

	/**
	 * Constructs a channel that reads bytes from the given stream.
	 */
  public static ReadableByteChannel newChannel(InputStream in)
  {
    return new InputStreamChannel(in);
	}

	/**
	 * Constructs a channel that writes bytes to the given stream.
	 */
  public static WritableByteChannel newChannel(OutputStream out)
  {
    return new OutputStreamChannel(out);
	}

	/**
	 * Constructs a reader that decodes bytes from the given channel using the
	 * given decoder.
	 */
  public static Reader newReader(ReadableByteChannel ch, CharsetDecoder dec,
                                 int minBufferCap)
  {
    return new ChannelReader(ch, dec, minBufferCap);
	}

	/**
	 * Constructs a reader that decodes bytes from the given channel according to
	 * the named charset.
	 *
	 * @exception UnsupportedCharsetException If no support for the named charset
	 * is available in this instance of the Java virtual machine.
	 */
  public static Reader newReader(ReadableByteChannel ch, String csName)
  {
		return newReader(ch, Charset.forName(csName).newDecoder(), -1);
	}

	/**
	 * Constructs a writer that encodes characters using the given encoder and
	 * writes the resulting bytes to the given channel.
	 */
  public static Writer newWriter(WritableByteChannel ch, CharsetEncoder enc,
                                 int minBufferCap)
  {
    return new ChannelWriter(ch, enc, minBufferCap);
	}

	/**
	 * Constructs a writer that encodes characters according to the named charset
	 * and writes the resulting bytes to the given channel.
	 *
	 * @exception UnsupportedCharsetException If no support for the named charset
	 * is available in this instance of the Java virtual machine.
	 */
  public static Writer newWriter(WritableByteChannel ch, String csName)
  {
		return newWriter(ch, Charset.forName(csName).newEncoder(), -1);
	}
}
