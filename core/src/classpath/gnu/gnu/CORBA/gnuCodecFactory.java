/* gnuCodecFactory.java --
   Copyright (C) 2005 Free Software Foundation, Inc.

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


package gnu.CORBA;

import org.omg.CORBA.*;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.*;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.Encoding;

/**
 * A simple implementation of the Codec factory, able to return the
 * standard Codec's. Only ENCODING_CDR_ENCAPS encoding is supported.
 *
 * @author Audrius Meskauskas, Lithuania (AudriusA@Bioinformatics.org)
 */
public class gnuCodecFactory
  extends LocalObject
  implements CodecFactory
{
  /**
   * The associated ORB.
   */
  private final ORB orb;

  /**
   * Create a new instance of the this factory, associated with the given ORB.
   */
  public gnuCodecFactory(ORB an_orb)
  {
    orb = an_orb;
  }

  /**
   * Creates the Codec for the given encoding.
   *
   * @param for_encoding the encoding for that the Codec must be created.
   *
   * @return the suitable Codec.
   *
   * @throws UnknownEncoding if the encoding is not a ENCODING_CDR_ENCAPS.
   */
  public Codec create_codec(Encoding for_encoding)
                     throws UnknownEncoding
  {
    if (for_encoding.format != ENCODING_CDR_ENCAPS.value)
      throw new UnknownEncoding("Only ENCODING_CDR_ENCAPS is " +
                                "supported by this factory."
                               );

    return new cdrEncapsCodec(orb,
                              new Version(for_encoding.major_version,
                                          for_encoding.minor_version
                                         )
                             );
  }
}