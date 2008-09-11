/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.compiler;

import java.util.Map;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BaseMagicHelper {

    /**
     * Enum of all magic classes.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public enum MagicClass {
        ADDRESS("org.vmmagic.unboxed.Address"),
        EXTENT("org.vmmagic.unboxed.Extent"),
        OBJECTREFERENCE("org.vmmagic.unboxed.ObjectReference"),
        OFFSET("org.vmmagic.unboxed.Offset"),
        WORD("org.vmmagic.unboxed.Word"),
        ADDRESSARRAY("org.vmmagic.unboxed.AddressArray"),
        EXTENTARRAY("org.vmmagic.unboxed.ExtentArray"),
        OBJECTREFERENCEARRAY("org.vmmagic.unboxed.ObjectReferenceArray"),
        OFFSETARRAY("org.vmmagic.unboxed.OffsetArray"),
        WORDARRAY("org.vmmagic.unboxed.WordArray"),
        VMMAGIC("org.jnode.vm.VmMagic");

        /**
         * Name of the class
         */
        private final String name;

        /**
         * Lookup table
         */
        private static final Map<String, MagicClass> nameToClass;

        /**
         * Initialize this instance.
         *
         * @param name
         */
        private MagicClass(String name) {
            this.name = name;
        }

        /**
         * Initialize the lookup table
         */
        static {
            nameToClass = new BootableHashMap<String, MagicClass>();
            for (MagicClass mc : values()) {
                nameToClass.put(mc.name, mc);
            }
        }

        /**
         * Gets the MagicClass instance for the given type.
         *
         * @param type
         * @return
         * @throws InternalError When type is no magic type.
         */
        public static MagicClass get(VmType<?> type) {
            MagicClass mc = nameToClass.get(type.getName());
            if (mc == null) {
                throw new InternalError("Unknown magic type " + type.getName());
            } else {
                return mc;
            }
        }
    }

    /**
     * Enum of all methods in all magic classes.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public enum MagicMethod {
        ADD("add", false),
        AND("and", false),
        OR("or", false),
        NOT("not", false),
        SUB("sub", false),
        XOR("xor", false),
        ZERO("zero", false),
        MAX("max", false),
        ONE("one", false),
        TOINT("toInt", false),
        TOLONG("toLong", false),
        TOWORD("toWord", false),
        TOADDRESS("toAddress", true),
        TOEXTENT("toExtent", false),
        TOOFFSET("toOffset", false),
        TOOBJECTREFERENCE("toObjectReference", true),
        EQUALS("equals", false),
        ISZERO("isZero", false),
        ISMAX("isMax", false),
        ISNULL("isNull", false),
        EQ("EQ", false),
        NE("NE", false),
        GT("GT", false),
        GE("GE", false),
        LT("LT", false),
        LE("LE", false),
        SGT("sGT", false),
        SGE("sGE", false),
        SLT("sLT", false),
        SLE("sLE", false),
        FROMINT("fromInt", false),
        FROMINTSIGNEXTEND("fromIntSignExtend", false),
        FROMINTZEROEXTEND("fromIntZeroExtend", false),
        FROMLONG("fromLong", false),
        LSH("lsh", false),
        RSHL("rshl", false),
        RSHA("rsha", false),
        LOADBYTE("loadByte", true, "()B"),
        LOADBYTE_OFS("loadByte", true, "(Lorg/vmmagic/unboxed/Offset;)B"),
        LOADCHAR("loadChar", true, "()C"),
        LOADCHAR_OFS("loadChar", true, "(Lorg/vmmagic/unboxed/Offset;)C"),
        LOADSHORT("loadShort", true, "()S"),
        LOADSHORT_OFS("loadShort", true, "(Lorg/vmmagic/unboxed/Offset;)S"),
        LOADINT("loadInt", true, "()I"),
        LOADINT_OFS("loadInt", true, "(Lorg/vmmagic/unboxed/Offset;)I"),
        LOADFLOAT("loadFloat", true, "()F"),
        LOADFLOAT_OFS("loadFloat", true, "(Lorg/vmmagic/unboxed/Offset;)F"),
        LOADLONG("loadLong", true, "()J"),
        LOADLONG_OFS("loadLong", true, "(Lorg/vmmagic/unboxed/Offset;)J"),
        LOADDOUBLE("loadDouble", true, "()D"),
        LOADDOUBLE_OFS("loadDouble", true, "(Lorg/vmmagic/unboxed/Offset;)D"),
        LOADADDRESS("loadAddress", true, "()Lorg/vmmagic/unboxed/Address;"),
        LOADADDRESS_OFS("loadAddress", true, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/Address;"),
        LOADWORD("loadWord", true, "()Lorg/vmmagic/unboxed/Word;"),
        LOADWORD_OFS("loadWord", true, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/Word;"),
        LOADOBJECTREFERENCE("loadObjectReference", true, "()Lorg/vmmagic/unboxed/ObjectReference;"),
        LOADOBJECTREFERENCE_OFS("loadObjectReference", true,
            "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/ObjectReference;"),
        SIZE("size", false),
        STOREBYTE("store", true, "(B)V"),
        STOREBYTE_OFS("store", true, "(BLorg/vmmagic/unboxed/Offset;)V"),
        STORECHAR("store", true, "(C)V"),
        STORECHAR_OFS("store", true, "(CLorg/vmmagic/unboxed/Offset;)V"),
        STORESHORT("store", true, "(S)V"),
        STORESHORT_OFS("store", true, "(SLorg/vmmagic/unboxed/Offset;)V"),
        STOREINT("store", true, "(I)V"),
        STOREINT_OFS("store", true, "(ILorg/vmmagic/unboxed/Offset;)V"),
        STOREFLOAT("store", true, "(F)V"),
        STOREFLOAT_OFS("store", true, "(FLorg/vmmagic/unboxed/Offset;)V"),
        STORELONG("store", true, "(J)V"),
        STORELONG_OFS("store", true, "(JLorg/vmmagic/unboxed/Offset;)V"),
        STOREDOUBLE("store", true, "(D)V"),
        STOREDOUBLE_OFS("store", true, "(DLorg/vmmagic/unboxed/Offset;)V"),
        STOREADDRESS("store", true, "(Lorg/vmmagic/unboxed/Address;)V"),
        STOREADDRESS_OFS("store", true, "(Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Offset;)V"),
        STOREWORD("store", true, "(Lorg/vmmagic/unboxed/Word;)V"),
        STOREWORD_OFS("store", true, "(Lorg/vmmagic/unboxed/Word;Lorg/vmmagic/unboxed/Offset;)V"),
        STOREOBJECTREFERENCE("store", true, "(Lorg/vmmagic/unboxed/ObjectReference;)V"),
        STOREOBJECTREFERENCE_OFS("store", true, "(Lorg/vmmagic/unboxed/ObjectReference;Lorg/vmmagic/unboxed/Offset;)V"),
        PREPAREINT("prepareInt", true, "()I"),
        PREPAREINT_OFS("prepareInt", true, "(Lorg/vmmagic/unboxed/Offset;)I"),
        PREPAREADDRESS("prepareAddress", true, "()Lorg/vmmagic/unboxed/Address;"),
        PREPAREADDRESS_OFS("prepareAddress", true, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/Address;"),
        PREPAREWORD("prepareWord", true, "()Lorg/vmmagic/unboxed/Word;"),
        PREPAREWORD_OFS("prepareWord", true, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/Word;"),
        PREPAREOBJECTREFERENCE("prepareObjectReference", true, "()Lorg/vmmagic/unboxed/ObjectReference;"),
        PREPAREOBJECTREFERENCE_OFS("prepareObjectReference", true,
            "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/ObjectReference;"),
        ATTEMPTINT("attempt", true, "(II)Z"),
        ATTEMPTINT_OFS("attempt", true, "(IILorg/vmmagic/unboxed/Offset;)Z"),
        ATTEMPTADDRESS("attempt", true, "(Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Address;)Z"),
        ATTEMPTADDRESS_OFS("attempt", true,
            "(Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Offset;)Z"),
        ATTEMPTOBJECTREFERENCE("attempt", true,
            "(Lorg/vmmagic/unboxed/ObjectReference;Lorg/vmmagic/unboxed/ObjectReference;)Z"),
        ATTEMPTOBJECTREFERENCE_OFS("attempt", true,
            "(Lorg/vmmagic/unboxed/ObjectReference;Lorg/vmmagic/unboxed/ObjectReference;" +
                "Lorg/vmmagic/unboxed/Offset;)Z"),
        ATTEMPTWORD("attempt", true, "(Lorg/vmmagic/unboxed/Word;Lorg/vmmagic/unboxed/Word;)Z"),
        ATTEMPTWORD_OFS("attempt", true,
            "(Lorg/vmmagic/unboxed/Word;Lorg/vmmagic/unboxed/Word;Lorg/vmmagic/unboxed/Offset;)Z"),
        FROMOBJECT("fromObject", true),
        FROMADDRESS("fromAddress", true),
        GETOBJECTTYPE("getObjectType", true),
        GETTIB("getTIB", true),
        GETOBJECTFLAGS("getObjectFlags", true),
        SETOBJECTFLAGS("setObjectFlags", true),
        TOOBJECT("toObject", true),
        GETARRAYDATA("getArrayData", true),
        GETOBJECTCOLOR("getObjectColor", true),
        ISFINALIZED("isFinalized", true),
        ATOMICADD("atomicAdd", true),
        ATOMICAND("atomicAnd", true),
        ATOMICOR("atomicOr", true),
        ATOMICSUB("atomicSub", true),
        GETCURRENTFRAME("getCurrentFrame", true),
        GETTIMESTAMP("getTimeStamp", true),
        INTBITSTOFLOAT("intBitsToFloat", false),
        FLOATTORAWINTBITS("floatToRawIntBits", false),
        LONGBITSTODOUBLE("longBitsToDouble", false),
        DOUBLETORAWLONGBITS("doubleToRawLongBits", false),
        BREAKPOINT("breakPoint", true),
        DIFF("diff", false),
        NULLREFERENCE("nullReference", false),
        CURRENTPROCESSOR("currentProcessor", true, "()Lorg/jnode/vm/scheduler/VmProcessor;"),
        GETSHAREDSTATICSFIELDADDRESS("getSharedStaticFieldAddress", true),
        GETISOLATEDSTATICSFIELDADDRESS("getIsolatedStaticFieldAddress", true),
        ISRUNNINGJNODE("isRunningJNode", false, "()Z"),

        // Array classes
        ARR_CREATE("create", true),
        ARR_GET("get", true),
        ARR_SET("set", true),
        ARR_LENGTH("length", false);

        private final String name;
        private final String signature;
        private final boolean permissionRequired;
        private static BootableHashMap<VmMethod, MagicMethod> methods = new BootableHashMap<VmMethod, MagicMethod>();

        private MagicMethod(String name, boolean permissionRequired) {
            this.name = name;
            this.signature = null;
            this.permissionRequired = permissionRequired;
        }

        private MagicMethod(String name, boolean permissionRequired, String signature) {
            this.name = name;
            this.permissionRequired = permissionRequired;
            this.signature = signature;
        }

        public static MagicMethod get(VmMethod method) {
            MagicMethod mm = methods.get(method);
            if (mm != null) {
                return mm;
            }
            final String mname = method.getName();
            final String msignature = method.getSignature();

            for (MagicMethod m : values()) {
                if (m.name.equals(mname)) {
                    if ((m.signature == null) || m.signature.equals(msignature)) {
                        methods.put(method, m);
                        return m;
                    }

                }
            }
            throw new InternalError("Unknown method " + mname + '#' + msignature + " in "
                + method.getDeclaringClass().getName());
        }

        /**
         * Is MagicPermission required for this method.
         *
         * @return
         */
        final boolean isPermissionRequired() {
            return this.permissionRequired;
        }
    }

    /**
     * Is the given method allowed to call magic code.
     *
     * @param caller
     */
    public static void testMagicPermission(MagicMethod callee, VmMethod caller)
        throws SecurityException {
        if (callee.isPermissionRequired()) {
            if (!caller.getDeclaringClass().isMagicPermissionGranted()) {
                System.out.println("MagicPermission is not granted for type: "
                    + caller.getDeclaringClass().getName());
                // throw new SecurityException("MagicPermission is not granted
                // for method: " + caller.getFullName());
            }
        }
    }
}
