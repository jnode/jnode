/* CompressedEntry.java -- 
   Copyright (C) 2003, 2006 Free Software Foundation, Inc.

This file is a part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
USA

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
exception statement from your version.  */


package gnu.javax.crypto.keyring;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class CompressedEntry extends EnvelopeEntry
{

  // Constants and fields.
  // ------------------------------------------------------------------------

  public static final int TYPE = 4;

  // Constructor.
  // ------------------------------------------------------------------------

  public CompressedEntry(Properties properties)
  {
    super(TYPE, properties);
    this.properties.put("algorithm", "DEFLATE");
  }

  private CompressedEntry()
  {
    this(new Properties());
  }

  // Class methods.
  // ------------------------------------------------------------------------

  public static CompressedEntry decode(DataInputStream in) throws IOException
  {
    CompressedEntry entry = new CompressedEntry();
    entry.properties.decode(in);
    String alg = entry.properties.get("algorithm");
    if (alg == null)
      {
        throw new MalformedKeyringException("no compression algorithm");
      }
    if (!alg.equalsIgnoreCase("DEFLATE"))
      {
        throw new MalformedKeyringException(
                                            "unsupported compression algorithm: "
                                                + alg);
      }
    int len = in.readInt();
    MeteredInputStream min = new MeteredInputStream(in, len);
    InflaterInputStream infin = new InflaterInputStream(min);
    DataInputStream in2 = new DataInputStream(infin);
    entry.decodeEnvelope(in2);
    return entry;
  }

  // Instance methods.
  // ------------------------------------------------------------------------

  protected void encodePayload() throws IOException
  {
    ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);
    DeflaterOutputStream dout = new DeflaterOutputStream(buf);
    DataOutputStream out2 = new DataOutputStream(dout);
    for (Iterator it = entries.iterator(); it.hasNext();)
      {
        ((Entry) it.next()).encode(out2);
      }
    dout.finish();
    payload = buf.toByteArray();
  }
}
