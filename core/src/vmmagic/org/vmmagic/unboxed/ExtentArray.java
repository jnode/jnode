/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
 
package org.vmmagic.unboxed;

/**
 * <u>JNode specific notes</u> : This class contains some "magic"
 * methods that are interpreted by the VM itself, instead of being executed
 * as normal java methods.  <b>The actual method bodies are never used</b>.
 * @see {@link org.jnode.classmgr.VmType VmType} to get the list of "magic" classes
 * @see {@link org.jnode.vm.compiler.BaseMagicHelper.MagicMethod MagicMethod}
 * to get the list of "magic" methods
 * @author Daniel Frampton
 */
final public class ExtentArray {

    public static ExtentArray create(int size) {
        return null;
    }

    public Extent get(int index) {
        return null;
    }

    public void set(int index, Extent v) {
    }

    public int length() {
        return 0;
    }
}
