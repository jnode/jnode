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
 
package java.security;

import gnu.classpath.SystemProperties;
import gnu.java.security.action.GetSecurityPropertyAction;

import java.net.URL;

/**
 * VM-specific methods for generating real (or almost real) random
 * seeds. VM implementors should write a version of this class that
 * reads random bytes from some system source.
 *
 * <p>The default implementation of this class runs eight threads that
 * increment counters in a tight loop, and XORs each counter to
 * produce one byte of seed data. This is not very efficient, and is
 * not guaranteed to be random (the thread scheduler is probably
 * deterministic, after all). If possible, VM implementors should
 * reimplement this class so it obtains a random seed from a system
 * facility, such as a system entropy gathering device or hardware
 * random number generator.
 */
final class VMSecureRandom
{

  /**
   * Generate a random seed. Implementations are free to generate
   * fewer random bytes than are requested, and leave the remaining
   * bytes of the destination buffer as zeros. Implementations SHOULD,
   * however, make a best-effort attempt to satisfy the request.
   *
   * @param buffer The destination buffer.
   * @param offset The offset in the buffer to start putting bytes.
   * @param length The number of random bytes to generate.
   */
  static int generateSeed(byte[] buffer, int offset, int length)
  {
    if (length < 0)
      throw new IllegalArgumentException("length must be nonnegative");
    if (offset < 0 || offset + length > buffer.length)
      throw new IndexOutOfBoundsException();

    Spinner[] spinners = new Spinner[8];
    int n = 0x1;
    for (int i = 0; i < spinners.length; i++)
      {
        spinners[i] = new Spinner((byte) n);
        Thread t = new Thread(spinners[i]);
        t.start();
        n <<= 1;
      }

    // Wait until at least one spinner has started.
    while (!(spinners[0].running || spinners[1].running || spinners[2].running
             || spinners[3].running || spinners[4].running || spinners[5].running
             || spinners[6].running || spinners[7].running))
      {
        Thread.yield();
      }

    for (int i = offset; i < length; i++)
      {
        buffer[i] = (byte) (spinners[0].value ^ spinners[1].value ^ spinners[2].value
                            ^ spinners[3].value ^ spinners[4].value ^ spinners[5].value
                            ^ spinners[6].value ^ spinners[7].value);
        Thread.yield();
      }

    for (int i = 0; i < spinners.length; i++)
      spinners[i].stop();

    return length;
  }

  static class Spinner implements Runnable
  {
    volatile byte value;
    volatile boolean running;

    Spinner(final byte initial)
    {
      value = initial;
    }

    public void run()
    {
      running = true;
      while (running)
        value++;
    }

    private void stop()
    {
      running = false;
    }
  }
}
