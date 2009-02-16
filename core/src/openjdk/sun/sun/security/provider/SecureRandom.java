/*
 * Copyright 1998-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.security.provider;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandomSpi;
import java.security.NoSuchAlgorithmException;

/**
 * <p>This class provides a crytpographically strong pseudo-random number
 * generator based on the SHA-1 hash algorithm.
 *
 * <p>Note that if a seed is not provided, we attempt to provide sufficient
 * seed bytes to completely randomize the internal state of the generator
 * (20 bytes).  However, our seed generation algorithm has not been thoroughly
 * studied or widely deployed.
 *
 * <p>Also note that when a random object is deserialized,
 * <a href="#engineNextBytes(byte[])">engineNextBytes</a> invoked on the
 * restored random object will yield the exact same (random) bytes as the
 * original object.  If this behaviour is not desired, the restored random
 * object should be seeded, using
 * <a href="#engineSetSeed(byte[])">engineSetSeed</a>.
 *
 * @author Benjamin Renaud
 * @author Josh Bloch
 * @author Gadi Guy
 */

public final class SecureRandom extends SecureRandomSpi
implements java.io.Serializable {

    private static final long serialVersionUID = 3581829991155417889L;

    /**
     * This static object will be seeded by SeedGenerator, and used
     * to seed future instances of SecureRandom
     */
    private static SecureRandom seeder;

    private static final int DIGEST_SIZE = 20;
    private transient MessageDigest digest;
    private byte[] state;
    private byte[] remainder;
    private int remCount;

    /**
     * This empty constructor automatically seeds the generator.  We attempt
     * to provide sufficient seed bytes to completely randomize the internal
     * state of the generator (20 bytes).  Note, however, that our seed
     * generation algorithm has not been thoroughly studied or widely deployed.
     *
     * <p>The first time this constructor is called in a given Virtual Machine,
     * it may take several seconds of CPU time to seed the generator, depending
     * on the underlying hardware.  Successive calls run quickly because they
     * rely on the same (internal) pseudo-random number generator for their
     * seed bits.
     */
    public SecureRandom() {
        init(null);
    }

    /**
     * This constructor is used to instatiate the private seeder object
     * with a given seed from the SeedGenerator.
     *
     * @param seed the seed.
     */
    private SecureRandom(byte seed[]) {
        init(seed);
    }

    /**
     * This call, used by the constructors, instantiates the SHA digest
     * and sets the seed, if given.
     */
    private void init(byte[] seed) {
        try {
            digest = MessageDigest.getInstance ("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("internal error: SHA-1 not available.");
        }

        if (seed != null) {
           engineSetSeed(seed);
        }
    }

    /**
     * Returns the given number of seed bytes, computed using the seed
     * generation algorithm that this class uses to seed itself.  This
     * call may be used to seed other random number generators.  While
     * we attempt to return a "truly random" sequence of bytes, we do not
     * know exactly how random the bytes returned by this call are.  (See
     * the empty constructor <a href = "#SecureRandom">SecureRandom</a>
     * for a brief description of the underlying algorithm.)
     * The prudent user will err on the side of caution and get extra
     * seed bytes, although it should be noted that seed generation is
     * somewhat costly.
     *
     * @param numBytes the number of seed bytes to generate.
     *
     * @return the seed bytes.
     */
    public byte[] engineGenerateSeed(int numBytes) {
        byte[] b = new byte[numBytes];
        SeedGenerator.generateSeed(b);
        return b;
    }

    /**
     * Reseeds this random object. The given seed supplements, rather than
     * replaces, the existing seed. Thus, repeated calls are guaranteed
     * never to reduce randomness.
     *
     * @param seed the seed.
     */
    synchronized public void engineSetSeed(byte[] seed) {
        if (state != null) {
            digest.update(state);
            for (int i = 0; i < state.length; i++)
                state[i] = 0;
        }
        state = digest.digest(seed);
    }

    private static void updateState(byte[] state, byte[] output) {
        int last = 1;
        int v = 0;
        byte t = 0;
        boolean zf = false;

        // state(n + 1) = (state(n) + output(n) + 1) % 2^160;
        for (int i = 0; i < state.length; i++) {
            // Add two bytes
            v = (int)state[i] + (int)output[i] + last;
            // Result is lower 8 bits
            t = (byte)v;
            // Store result. Check for state collision.
            zf = zf | (state[i] != t);
            state[i] = t;
            // High 8 bits are carry. Store for next iteration.
            last = v >> 8;
        }

        // Make sure at least one bit changes!
        if (!zf)
           state[0]++;
    }

    /**
     * Generates a user-specified number of random bytes.
     *
     * @param bytes the array to be filled in with random bytes.
     */
    public synchronized void engineNextBytes(byte[] result) {
        int index = 0;
        int todo;
        byte[] output = remainder;

        if (state == null) {
            if (seeder == null) {
                seeder = new SecureRandom(SeedGenerator.getSystemEntropy());
                seeder.engineSetSeed(engineGenerateSeed(DIGEST_SIZE));
            }

            byte[] seed = new byte[DIGEST_SIZE];
            seeder.engineNextBytes(seed);
            state = digest.digest(seed);
        }

        // Use remainder from last time
        int r = remCount;
        if (r > 0) {
            // How many bytes?
            todo = (result.length - index) < (DIGEST_SIZE - r) ?
                        (result.length - index) : (DIGEST_SIZE - r);
            // Copy the bytes, zero the buffer
            for (int i = 0; i < todo; i++) {
                result[i] = output[r];
                output[r++] = 0;
            }
            remCount += todo;
            index += todo;
        }

        // If we need more bytes, make them.
        while (index < result.length) {
            // Step the state
            digest.update(state);
            output = digest.digest();
            updateState(state, output);

            // How many bytes?
            todo = (result.length - index) > DIGEST_SIZE ?
                DIGEST_SIZE : result.length - index;
            // Copy the bytes, zero the buffer
            for (int i = 0; i < todo; i++) {
                result[index++] = output[i];
                output[i] = 0;
            }
            remCount += todo;
        }

        // Store remainder for next time
        remainder = output;
        remCount %= DIGEST_SIZE;
    }

    /*
     * readObject is called to restore the state of the random object from
     * a stream.  We have to create a new instance of MessageDigest, because
     * it is not included in the stream (it is marked "transient").
     *
     * Note that the engineNextBytes() method invoked on the restored random
     * object will yield the exact same (random) bytes as the original.
     * If you do not want this behaviour, you should re-seed the restored
     * random object, using engineSetSeed().
     */
    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {

        s.defaultReadObject ();

        try {
            digest = MessageDigest.getInstance ("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("internal error: SHA-1 not available.");
        }
    }
}
