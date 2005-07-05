/* ServiceRequestConverter.java --
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


package gnu.CORBA;

import gnu.CORBA.CDR.cdrBufOutput;

import org.omg.CORBA.ARG_IN;
import org.omg.CORBA.ARG_INOUT;
import org.omg.CORBA.ARG_OUT;
import org.omg.CORBA.Any;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.ServerRequest;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.Streamable;

/**
 * This class exists to handle obsolete invocation style using
 * ServerRequest.
 *
 * @deprecated The method {@link ObjectImpl#_invoke} is much faster.
 *
 * @author Audrius Meskauskas, Lithuania (AudriusA@Bioinformatics.org)
 */
public class ServiceRequestAdapter
  implements ResponseHandler
{
  /**
   * A buffer for writing the response.
   */
  cdrBufOutput reply = new cdrBufOutput();

  /**
   * If set to true, an exception has been thrown during the invocation.
   */
  boolean isException;

  public OutputStream createExceptionReply()
  {
    isException = true;
    return reply;
  }

  public OutputStream createReply()
  {
    isException = false;
    return reply;
  }

  /**
   * The old style invocation using the currently deprecated server
   * request class.
   *
   * @param request a server request, containg the invocation information.
   * @param target the invocation target
   * @param result the result holder with the set suitable streamable to read
   * the result or null for void.
   */
  public static void invoke(ServerRequest request, InvokeHandler target,
                            Streamable result
                           )
  {
    try
      {
        int IN = ARG_IN.value;
        int OUT = ARG_OUT.value;

        // Write all arguments to the buffer output stream.
        cdrBufOutput buffer = new cdrBufOutput();
        gnuNVList args = new gnuNVList();
        request.arguments(args);

        for (int i = 0; i < args.count(); i++)
          {
            if ((args.item(i).flags() & IN) != 0)
              {
                args.item(i).value().write_value(buffer);
              }
          }

        ServiceRequestAdapter h = new ServiceRequestAdapter();

        target._invoke(request.operation(), buffer.create_input_stream(), h);

        InputStream in = h.reply.create_input_stream();

        if (h.isException)
          {
            // Write the exception information
            gnuAny exc = new gnuAny();
            universalHolder uku = new universalHolder(h.reply);
            exc.insert_Streamable(uku);
            request.set_exception(exc);
          }
        else
          {
            if (result != null)
            {
              result._read(in);
              gnuAny r = new gnuAny();
              r.insert_Streamable(result);
              request.set_result(r);
            };

            // Unpack the arguments
            for (int i = 0; i < args.count(); i++)
              {
                if ((args.item(i).flags() & OUT) != 0)
                  {
                    Any a = args.item(i).value();
                    a.read_value(in, a.type());
                  }
              }
          }
      }
    catch (Bounds ex)
      {
        throw new InternalError();
      }
  }
}