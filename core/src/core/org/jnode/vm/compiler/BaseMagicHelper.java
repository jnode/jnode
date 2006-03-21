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
        
        /** Name of the class */
        private final String name;
        
        /** Lookup table */
        private static final Map<String, MagicClass> nameToClass;
        
        /**
         * Initialize this instance.
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
        ADD("add"),
        AND("and"),
        OR("or"),
        NOT("not"),
        SUB("sub"),
        XOR("xor"),
        ZERO("zero"),
        MAX("max"),
        ONE("one"),
        TOINT("toInt"),
        TOLONG("toLong"),
        TOWORD("toWord"),
        TOADDRESS("toAddress"),
        TOEXTENT("toExtent"),
        TOOFFSET("toOffset"),
        TOOBJECTREFERENCE("toObjectReference"),
        EQUALS("equals"),
        ISZERO("isZero"),
        ISMAX("isMax"),
        ISNULL("isNull"),
        EQ("EQ"),
        NE("NE"),
        GT("GT"),
        GE("GE"),
        LT("LT"),
        LE("LE"),
        SGT("sGT"),
        SGE("sGE"),
        SLT("sLT"),
        SLE("sLE"),
        FROMINT("fromInt"),
        FROMINTSIGNEXTEND("fromIntSignExtend"),
        FROMINTZEROEXTEND("fromIntZeroExtend"),
        FROMLONG("fromLong"),
        LSH("lsh"),
        RSHL("rshl"),
        RSHA("rsha"),
        LOADBYTE("loadByte", 0, "()B"),
        LOADBYTE_OFS("loadByte", 1, "(Lorg/vmmagic/unboxed/Offset;)B"),
        LOADCHAR("loadChar", 0, "()C"),
        LOADCHAR_OFS("loadChar", 1, "(Lorg/vmmagic/unboxed/Offset;)C"),
        LOADSHORT("loadShort", 0, "()S"),
        LOADSHORT_OFS("loadShort", 1, "(Lorg/vmmagic/unboxed/Offset;)S"),
        LOADINT("loadInt", 0, "()I"),
        LOADINT_OFS("loadInt", 1, "(Lorg/vmmagic/unboxed/Offset;)I"),
        LOADFLOAT("loadFloat", 0, "()F"),
        LOADFLOAT_OFS("loadFloat", 1, "(Lorg/vmmagic/unboxed/Offset;)F"),
        LOADLONG("loadLong", 0, "()J"),
        LOADLONG_OFS("loadLong", 1, "(Lorg/vmmagic/unboxed/Offset;)J"),
        LOADDOUBLE("loadDouble", 0, "()D"),
        LOADDOUBLE_OFS("loadDouble", 1, "(Lorg/vmmagic/unboxed/Offset;)D"),
        LOADADDRESS("loadAddress", 0, "()Lorg/vmmagic/unboxed/Address;"),
        LOADADDRESS_OFS("loadAddress", 1, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/Address;"),
        LOADWORD("loadWord", 0, "()Lorg/vmmagic/unboxed/Word;"),
        LOADWORD_OFS("loadWord", 1, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/Word;"),
        LOADOBJECTREFERENCE("loadObjectReference", 0, "()Lorg/vmmagic/unboxed/ObjectReference;"),
        LOADOBJECTREFERENCE_OFS("loadObjectReference", 1, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/ObjectReference;"),
        SIZE("size"),
        STOREBYTE("store", 1, "(B)V"),
        STOREBYTE_OFS("store", 2, "(BLorg/vmmagic/unboxed/Offset;)V"),
        STORECHAR("store", 1, "(C)V"),
        STORECHAR_OFS("store", 2, "(CLorg/vmmagic/unboxed/Offset;)V"),
        STORESHORT("store", 1, "(S)V"),
        STORESHORT_OFS("store", 2, "(SLorg/vmmagic/unboxed/Offset;)V"),
        STOREINT("store", 1, "(I)V"),
        STOREINT_OFS("store", 2, "(ILorg/vmmagic/unboxed/Offset;)V"),
        STOREFLOAT("store", 1, "(F)V"),
        STOREFLOAT_OFS("store", 2, "(FLorg/vmmagic/unboxed/Offset;)V"),
        STORELONG("store", 1, "(J)V"),
        STORELONG_OFS("store", 2, "(JLorg/vmmagic/unboxed/Offset;)V"),
        STOREDOUBLE("store", 1, "(D)V"),
        STOREDOUBLE_OFS("store", 2, "(DLorg/vmmagic/unboxed/Offset;)V"),
        STOREADDRESS("store", 1, "(Lorg/vmmagic/unboxed/Address;)V"),
        STOREADDRESS_OFS("store", 2, "(Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Offset;)V"),
        STOREWORD("store", 1, "(Lorg/vmmagic/unboxed/Word;)V"),
        STOREWORD_OFS("store", 2, "(Lorg/vmmagic/unboxed/Word;Lorg/vmmagic/unboxed/Offset;)V"),
        STOREOBJECTREFERENCE("store", 1, "(Lorg/vmmagic/unboxed/ObjectReference;)V"),
        STOREOBJECTREFERENCE_OFS("store", 2, "(Lorg/vmmagic/unboxed/ObjectReference;Lorg/vmmagic/unboxed/Offset;)V"),
        PREPAREINT("prepareInt", 0, "()I"),
        PREPAREINT_OFS("prepareInt", 1, "(Lorg/vmmagic/unboxed/Offset;)I"),
        PREPAREADDRESS("prepareAddress", 0, "()Lorg/vmmagic/unboxed/Address;"),
        PREPAREADDRESS_OFS("prepareAddress", 1, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/Address;"),
        PREPAREWORD("prepareWord", 0, "()Lorg/vmmagic/unboxed/Word;"),
        PREPAREWORD_OFS("prepareWord", 1, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/Word;"),
        PREPAREOBJECTREFERENCE("prepareObjectReference", 0, "()Lorg/vmmagic/unboxed/ObjectReference;"),
        PREPAREOBJECTREFERENCE_OFS("prepareObjectReference", 1, "(Lorg/vmmagic/unboxed/Offset;)Lorg/vmmagic/unboxed/ObjectReference;"),
        ATTEMPTINT("attempt", 2, "(II)Z"),
        ATTEMPTINT_OFS("attempt", 3, "(IILorg/vmmagic/unboxed/Offset;)Z"),
        ATTEMPTADDRESS("attempt", 2, "(Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Address;)Z"),
        ATTEMPTADDRESS_OFS("attempt", 3, "(Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Offset;)Z"),
        ATTEMPTOBJECTREFERENCE("attempt", 2, "(Lorg/vmmagic/unboxed/ObjectReference;Lorg/vmmagic/unboxed/ObjectReference;)Z"),
        ATTEMPTOBJECTREFERENCE_OFS("attempt", 3, "(Lorg/vmmagic/unboxed/ObjectReference;Lorg/vmmagic/unboxed/ObjectReference;Lorg/vmmagic/unboxed/Offset;)Z"),
        ATTEMPTWORD("attempt", 2, "(Lorg/vmmagic/unboxed/Word;Lorg/vmmagic/unboxed/Word;)Z"),
        ATTEMPTWORD_OFS("attempt", 3, "(Lorg/vmmagic/unboxed/Word;Lorg/vmmagic/unboxed/Word;Lorg/vmmagic/unboxed/Offset;)Z"),
        FROMOBJECT("fromObject"),
        FROMADDRESS("fromAddress"),
        GETOBJECTTYPE("getObjectType"),
        GETTIB("getTIB"),
        GETOBJECTFLAGS("getObjectFlags"),
        SETOBJECTFLAGS("setObjectFlags"),
        TOOBJECT("toObject"),
        GETARRAYDATA("getArrayData"),
        GETOBJECTCOLOR("getObjectColor"),
        ISFINALIZED("isFinalized"),
        ATOMICADD("atomicAdd"),
        ATOMICAND("atomicAnd"),
        ATOMICOR("atomicOr"),
        ATOMICSUB("atomicSub"),
        GETCURRENTFRAME("getCurrentFrame"),
        GETTIMESTAMP("getTimeStamp"),
        INTBITSTOFLOAT("intBitsToFloat"),
        FLOATTORAWINTBITS("floatToRawIntBits"),
        LONGBITSTODOUBLE("longBitsToDouble"),
        DOUBLETORAWLONGBITS("doubleToRawLongBits"),
        BREAKPOINT("breakPoint"),
        DIFF("diff"),
        NULLREFERENCE("nullReference"),
        CURRENTPROCESSOR("currentProcessor", 0, "()Lorg/jnode/vm/VmProcessor;"),
        // Array classes
        ARR_CREATE("create"),
        ARR_GET("get"),
        ARR_SET("set"),
        ARR_LENGTH("length");
        
        private final String name;
        private final String signature;
        private final int argCount;
        private static BootableHashMap<VmMethod, MagicMethod> methods = new BootableHashMap<VmMethod, MagicMethod>();
        
        private MagicMethod(String name) {
            this(name, 0);
        }
        private MagicMethod(String name, int argCount) {
            this.name = name;
            this.argCount = argCount;
            this.signature = null;
        }
        private MagicMethod(String name, int argCount, String signature) {
            this.name = name;
            this.argCount = argCount;
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
    }
}
