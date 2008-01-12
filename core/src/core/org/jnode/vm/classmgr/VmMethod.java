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
 
package org.jnode.vm.classmgr;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.jnode.vm.LoadCompileService;
import org.jnode.vm.Vm;
import org.jnode.vm.VmAddress;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.isolate.VmIsolateLocal;
import org.vmmagic.unboxed.Address;

@MagicPermission
public abstract class VmMethod extends VmMember implements VmSharedStaticsEntry {

    /** Address of native code of this method */
    private VmAddress nativeCode;

    /** #Slots taken by arguments of this method (including this pointer) */
    private final short argSlotCount;

    /** Resolved types for each argument of this method */
    private VmType[] paramTypes;

    /** Resolved return type of this method */
    private VmType returnType;

    /** java.lang.reflect.Method for this method */
    private VmIsolateLocal<Member> javaMemberHolder;

    /** The bytecode (if any) */
    private VmByteCode bytecode;

    /** The compiled code (if any) */
    private VmCompiledCode compiledCode;

    /** The exceptions we can throw */
    private VmExceptions exceptions;

    /** The selector of this method name&type */
    private int selector;

    /** Optimization level of native code */
    private short nativeCodeOptLevel = -1;

    /** The index in the statics table */
    private final int staticsIndex;

    /** The mangled name of this method */
    private String mangledName;
    
    /** Flags of variour pragma's set to this method */
    private char pragmaFlags;

    /**
     * Constructor for VmMethod.
     * 
     * @param name
     * @param signature
     * @param modifiers
     * @param declaringClass
     */
    protected VmMethod(String name, String signature, int modifiers,
            VmType<?> declaringClass) {
        super(
                name,
                signature,
                modifiers | (returnsObject(signature) ? Modifier.ACC_OBJECTREF : 0),
                declaringClass);
        this.argSlotCount = (short)(Signature.getArgSlotCount(declaringClass
                .getLoader().getArchitecture().getTypeSizeInfo(), signature)
                + (isStatic() ? 0 : 1));
        final VmClassLoader cl = declaringClass.getLoader();
        if (isStatic()) {
            this.selector = 0;
        } else {
            this.selector = cl.getSelectorMap().get(name, signature);
        }
        this.staticsIndex = cl.getSharedStatics().allocMethodCode();
    }
    
    private static final boolean returnsObject(String signature) {
        final char firstReturnSignatureChar = signature.charAt(signature
                .indexOf(')') + 1);
        return (firstReturnSignatureChar == '[' || firstReturnSignatureChar == 'L');        
    }

    /**
     * Initialize this instance, copy the given method.
     * 
     * @param src
     *            The method that is copied.
     */
    protected VmMethod(VmMethod src) {
        super(src.name, src.signature, src.getModifiers(), src.declaringClass);
        this.argSlotCount = src.argSlotCount;
        this.selector = src.selector;
        this.staticsIndex = src.staticsIndex;
    }

    /**
     * Get the currently used byte-code information for this method. This
     * bytecode may have been optimized. This may return null if this is a
     * native or abstract method.
     * 
     * @return The current bytecode
     */
    public final VmByteCode getBytecode() {
        return bytecode;
    }

    //todo security review
    /**
     * Sets the bytecode information of this method.
     * 
     * @param bc
     */
    public final void setBytecode(VmByteCode bc) {
        this.bytecode = bc;
        bc.lock();
    }

    /**
     * Get the number of bytes in the byte-codes for this method.
     * 
     * @return Length of bytecode
     */
    public final int getBytecodeSize() {
        return (bytecode == null) ? 0 : bytecode.getLength();
    }

    /**
     * Gets myself as java.lang.reflect.Method or java.lang.reflect.Constructor,
     * depending on isConstructor().
     * 
     * @return Method
     */
    public final Member asMember() {
        if (javaMemberHolder == null) {
            javaMemberHolder = new VmIsolateLocal<Member>();            
        }
        Member javaMember = javaMemberHolder.get();
        if (javaMember == null) {
            if (isConstructor()) {
                javaMember = new Constructor(this);
            } else {
                javaMember = new Method(this);
            }
            javaMemberHolder.set(javaMember);
        }
        return javaMember;
    }

    /**
     * Convert myself into a String representation
     * 
     * @return String
     */
    public final String toString() {
        return getMangledName();
    }

    /**
     * Convert myself into a String representation
     * 
     * @return The mangled name
     */
    public final String getMangledName() {
        if (mangledName == null) {
            mangledName = (declaringClass.getMangledName()
                    + mangle("#" + getName() + '.' + getSignature())).intern();
        }
        return mangledName;
    }

    /**
     * Gets the full name of this method consisting of
     * its declaring class, its name and its signature.
     * @return
     */
    public final String getFullName() {
        return declaringClass.getName() + '#' + getName() + '!' + getSignature();
    }

    //todo security review
    public final void resetOptLevel(){
        nativeCodeOptLevel = 0;
    }
    /**
     * Compile this method with n optimization level 1 higher then the current
     * optimization level.
     */
    public final void recompile() {
        final int optLevel = nativeCodeOptLevel + 1;
        if (!declaringClass.isPrepared()) {
            throw new IllegalStateException(
                    "Declaring class must have been prepared");
        }
        LoadCompileService.compile(this, optLevel, false);
    }

    /**
     * Recompile a method declaring in the type given by its statics table index
     * and the index of the method within the type.
     * 
     * @param typeStaticsIndex
     * @param methodIndex
     */
    static final void recompileMethod(int typeStaticsIndex, int methodIndex) {
        final VmType<?> type = Vm.getVm().getSharedStatics().getTypeEntry(
                typeStaticsIndex);
        type.initialize();
        final VmMethod method = type.getDeclaredMethod(methodIndex);
        method.recompile();
    }

    public final boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    public final boolean isNative() {
        return Modifier.isNative(getModifiers());
    }

    public final boolean isSpecial() {
        return Modifier.isSpecial(getModifiers());
    }

    public final boolean isSynchronized() {
        return Modifier.isSynchronized(getModifiers());
    }

    public final boolean isConstructor() {
        return Modifier.isConstructor(getModifiers());
    }

    public final boolean isInitializer() {
        return Modifier.isInitializer(getModifiers());
    }

    /*
     * public final boolean isCompiled() { return
     * Modifier.isCompiled(getModifiers());
     */

    /**
     * Resolve the type of this method.
     */
    protected final void resolve() {
        resolveTypes();
    }

    private final void resolveTypes() {
        if (paramTypes == null) {
            try {
                Signature sig = new Signature(getSignature(), declaringClass
                        .getLoader());
                returnType = sig.getReturnType();
                int count = sig.getParamCount();
                final VmType[] types = new VmType[count];
                for (int i = 0; i < count; i++) {
                    types[i] = sig.getParamType(i);
                }
                this.paramTypes = types;
            } catch (ClassNotFoundException ex) {
                throw (Error) new NoClassDefFoundError("In method "
                        + toString()).initCause(ex);
            }
        }
    }

    public final int getNoArguments() {
        resolveTypes();
        return paramTypes.length;
    }

    public final VmType<?> getArgumentType(int index) {
        resolveTypes();
        return paramTypes[index];
    }

    /**
     * Does the given array of types match my argument types?
     * 
     * @param argTypes
     * @return boolean
     */
    protected final boolean matchArgumentTypes(VmType[] argTypes) {
        resolveTypes();
        int argTypesLength = (argTypes == null) ? 0 : argTypes.length;
        if (paramTypes.length != argTypesLength) {
            return false;
        }
        for (int i = 0; i < argTypesLength; i++) {
            if (argTypes[i] != paramTypes[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return VmClass
     */
    public final VmType<?> getReturnType() {
        resolveTypes();
        return returnType;
    }

    /**
     * Does this method return void?
     * 
     * @return boolean
     */
    public final boolean isReturnVoid() {
        return (signature.charAt(signature.length() - 1) == 'V');
    }

    /**
     * Does this method return long or double?
     * 
     * @return boolean
     */
    public final boolean isReturnWide() {
        return ((this.getModifiers() & Modifier.ACC_WIDE) != 0);
    }

    /**
     * Does this method return an object reference.
     * 
     * @return boolean
     */
    public final boolean isReturnObject() {
        return ((this.getModifiers() & Modifier.ACC_OBJECTREF) != 0);
    }

    /**
     * Gets the exceptions this method has declared to throw
     * 
     * @return The exceptions this method has declared to throw, never null.
     */
    public final VmExceptions getExceptions() {
        if (exceptions == null) {
            exceptions = new VmExceptions();
        }
        return exceptions;
    }

    /**
     * Sets the exceptions this method has declared to throw
     * 
     * @param exceptions
     * @throws ClassFormatError
     */
    final void setExceptions(VmExceptions exceptions) throws ClassFormatError {
        if (this.exceptions == null) {
            this.exceptions = exceptions;
            this.pragmaFlags |= exceptions.getPragmaFlags();
        } else {
            throw new ClassFormatError(
                    "Cannot have more then 1 Exceptions attribute");
        }
    }

    /**
     * Add the given pragma flags to my flags.
     */
    final void addPragmaFlags(int flags) {
        this.pragmaFlags |= flags;
        
        // KernelSpace implies uninterruptible
        if ((flags & MethodPragmaFlags.KERNELSPACE) != 0) {
            this.pragmaFlags |= MethodPragmaFlags.UNINTERRUPTIBLE;
        }
    }

    /**
     * Gets the compiled code information of this method (if any)
     * 
     * @return The compiled code, or null if no compiled code has been set.
     */
    public final VmCompiledCode getDefaultCompiledCode() {
        return compiledCode;
    }

    /**
     * Gets the compiled code for a given magic value (if any)
     * 
     * @return The compiled code, or null if no compiled code with the given
     *         magic has been set.
     */
    public final VmCompiledCode getCompiledCode(int magic) {
        final VmCompiledCode c = compiledCode;
        if (c != null) {
            return c.lookup(magic);
        } else {
            return null;
        }
    }

    /**
     * Install the generated code.
     * 
     * @param code
     * @param optLevel
     *            The optimization level of the generated code.
     */
    public final void addCompiledCode(VmCompiledCode code, int optLevel) {
        if ((this.nativeCode == null) || (optLevel > nativeCodeOptLevel)) {
            synchronized (this) {
                code.setNext(this.compiledCode);
                this.compiledCode = code;
                this.nativeCode = code.getNativeCode();
                this.compiledCode = code;
                Vm.getVm().getSharedStatics().setMethodCode(
                        getSharedStaticsIndex(), code.getNativeCode());
                this.nativeCodeOptLevel = (short) optLevel;
            }
        }
    }

    /**
     * Gets the global unique selector if this method name&amp;type.
     * 
     * @return The selector
     */
    public final int getSelector() {
        return selector;
    }

    /**
     * Gets the number of stack slots used by the arguments of this method. This
     * number included the slot for "this" on non-static fields.
     * 
     * @return int
     */
    public final int getArgSlotCount() {
        return this.argSlotCount;
    }

    /**
     * Is this method uninterruptible.
     * @return True | false.
     */
    public final boolean isUninterruptible() {
        return ((pragmaFlags & MethodPragmaFlags.UNINTERRUPTIBLE) != 0);
    }
    
    /**
     * Is the checkpermission pragma set for this method.
     * @return
     */
    public final boolean hasCheckPermissionPragma() {
        return ((pragmaFlags & MethodPragmaFlags.CHECKPERMISSION) != 0);        
    }

    /**
     * Is the doprivileged pragma set for this method.
     * @return
     */
    public final boolean hasDoPrivilegedPragma() {
        return ((pragmaFlags & MethodPragmaFlags.DOPRIVILEGED) != 0);        
    }

    /**
     * Is the inline pragma set for this method.
     * @return
     */
    public final boolean hasInlinePragma() {
        return ((pragmaFlags & MethodPragmaFlags.INLINE) != 0);        
    }

    /**
     * Is the loadstatics pragma set for this method.
     * @return
     */
    public final boolean hasLoadStaticsPragma() {
        return ((pragmaFlags & MethodPragmaFlags.LOADSTATICS) != 0);        
    }

    /**
     * Is the noinline pragma set for this method.
     * @return
     */
    public final boolean hasNoInlinePragma() {
        return ((pragmaFlags & MethodPragmaFlags.NOINLINE) != 0);        
    }

    /**
     * Is the noreadbarrier pragma set for this method.
     * @return
     */
    public final boolean hasNoReadBarrierPragma() {
        return ((pragmaFlags & MethodPragmaFlags.NOREADBARRIER) != 0);        
    }

    /**
     * Is the nowritebarrier pragma set for this method.
     * @return
     */
    public final boolean hasNoWriteBarrierPragma() {
        return ((pragmaFlags & MethodPragmaFlags.NOWRITEBARRIER) != 0);        
    }

    /**
     * Is the privilegedaction pragma set for this method.
     * @return
     */
    public final boolean hasPrivilegedActionPragma() {
        return ((pragmaFlags & MethodPragmaFlags.PRIVILEGEDACTION) != 0);        
    }

    /**
     * Is the KernelSpace pragma set for this method.
     * @return
     */
    public final boolean hasKernelSpacePragma() {
        return ((pragmaFlags & MethodPragmaFlags.KERNELSPACE) != 0);        
    }

    /**
     * Mark this method as uninterruptable.
     */
    final void setUninterruptible() {
        pragmaFlags |= MethodPragmaFlags.UNINTERRUPTIBLE;
    }

    /**
     * Gets the optimization level of the native code. A value of -1 means not
     * compiled yet.
     * 
     * @return Returns the nativeCodeOptLevel.
     */
    public final int getNativeCodeOptLevel() {
        return this.nativeCodeOptLevel;
    }

    /**
     * Gets the indexe of this field in the shared statics table.
     * 
     * @return Returns the staticsIndex.
     */
    public final int getSharedStaticsIndex() {
        return this.staticsIndex;
    }

    public boolean hasNativeCode() {
        if (nativeCode == null) {
            return false;
        } else {
            return true;
        }
    }

    public void testNativeCode() {
        if (declaringClass.isInterface()) {
            return;
        }
        if (nativeCode == null) {
            System.err.println("nativeCode == null in " + this);
        } else {
            final int ptr = Address.fromAddress(nativeCode).toInt();
            if ((ptr < 0) || (Math.abs(ptr) < 4096)) {
                System.err.println("nativeCode has low address " + ptr + " in "
                        + this);
            }
        }
    }

    /**
     * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
     */
    public void verifyBeforeEmit() {
        super.verifyBeforeEmit();
        if (nativeCode == null) {
            if (!declaringClass.isInterface()) {
                throw new RuntimeException("nativeCode of " + this
                        + " is null; declaringclass compiled? "
                        + getDeclaringClass().isCompiled());
            }
        }
    }
}
