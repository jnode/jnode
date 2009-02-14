/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.crypto.provider;

import java.io.UnsupportedEncodingException;
import java.security.KeyRep;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;

/**
 * This class represents a PBE key.
 *
 * @author Jan Luehe
 *
 */
final class PBEKey implements SecretKey {

    static final long serialVersionUID = -2234768909660948176L;

    private byte[] key;

    private String type;

    /**
     * Creates a PBE key from a given PBE key specification.
     *
     * @param key the given PBE key specification
     */
    PBEKey(PBEKeySpec keySpec, String keytype) throws InvalidKeySpecException {
        char[] passwd = keySpec.getPassword();
        if (passwd == null) {
            // Should allow an empty password.
            passwd = new char[0];
        }
        for (int i=0; i<passwd.length; i++) {
            if ((passwd[i] < '\u0020') || (passwd[i] > '\u007E')) {
                throw new InvalidKeySpecException("Password is not ASCII");
            }
        }
        this.key = new byte[passwd.length];
        for (int i=0; i<passwd.length; i++)
            this.key[i] = (byte) (passwd[i] & 0x7f);
        java.util.Arrays.fill(passwd, ' ');
        type = keytype;
    }

    public byte[] getEncoded() {
        return (byte[])this.key.clone();
    }

    public String getAlgorithm() {
        return type;
    }

    public String getFormat() {
        return "RAW";
    }

    /**
     * Calculates a hash code value for the object.
     * Objects that are equal will also have the same hashcode.
     */
    public int hashCode() {
        int retval = 0;
        for (int i = 1; i < this.key.length; i++) {
            retval += this.key[i] * i;
        }
        return(retval ^= getAlgorithm().toLowerCase().hashCode());
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof SecretKey))
            return false;

        SecretKey that = (SecretKey)obj;

        if (!(that.getAlgorithm().equalsIgnoreCase(type)))
            return false;

        byte[] thatEncoded = that.getEncoded();
        boolean ret = java.util.Arrays.equals(this.key, thatEncoded);
        java.util.Arrays.fill(thatEncoded, (byte)0x00);
        return ret;
    }

    /**
     * readObject is called to restore the state of this key from
     * a stream.
     */
    private void readObject(java.io.ObjectInputStream s)
         throws java.io.IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        key = (byte[])key.clone();
    }


    /**
     * Replace the PBE key to be serialized.
     *
     * @return the standard KeyRep object to be serialized
     *
     * @throws java.io.ObjectStreamException if a new object representing
     * this PBE key could not be created
     */
    private Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.SECRET,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }

    /**
     * Ensures that the password bytes of this key are
     * set to zero when there are no more references to it.
     */
    protected void finalize() throws Throwable {
        try {
            if (this.key != null) {
                java.util.Arrays.fill(this.key, (byte)0x00);
                this.key = null;
            }
        } finally {
            super.finalize();
        }
    }
}
