/**
 * $Id$
 */

package org.jnode.vm.classmgr;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.jnode.vm.Address;
import org.jnode.vm.PragmaUninterruptible;

public abstract class VmMethod extends VmMember {

    /** Address of native code of this method */
    private Address nativeCode;

    /** #Slots taken by arguments of this method (including this pointer) */
    private final int argSlotCount;

    /** #Invocations of this method */
    private int invocationCount;

    /** Resolved types for each argument of this method */
    private VmType[] paramTypes;

    /** Resolved return type of this method */
    private VmType returnType;

    /** Does this method return void? */
    private final boolean returnVoid;

    /** Does this method return an object? */
    private final boolean returnObject;

    /** java.lang.reflect.Method for this method */
    private Member javaMember;

    /** The bytecode (if any) */
    private VmByteCode bytecode;

    /** The original bytecode (as loaded byte the classdecoder), (if any) */
    private VmByteCode originalBytecode;

    /** The compiled code (if any) */
    private VmCompiledCode compiledCode;

    /** The exceptions we can throw */
    private VmExceptions exceptions;

    /** The selector of this method name&type */
    private int selector;

    /** This field will be used to mask the thread switch indicator */
    private int threadSwitchIndicatorMask = 0xFFFFFFFF;

    /** Optimization level of native code */
    private int nativeCodeOptLevel = -1;

    /** The index in the statics table */
    private final int staticsIndex;

    private int lastInvocationCount;

    /** The mangled name of this method */
    private String mangledName;

    /**
     * Constructor for VmMethod.
     * 
     * @param name
     * @param signature
     * @param modifiers
     * @param declaringClass
     */
    protected VmMethod(String name, String signature, int modifiers,
            VmType declaringClass) {
        super(name, signature, modifiers /*| (declaringClass.isFinal() ? Modifier.ACC_FINAL : 0)*/, declaringClass);
        this.argSlotCount = Signature.getArgSlotCount(signature)
                + (isStatic() ? 0 : 1);
        this.returnVoid = (signature.endsWith("V"));
        char firstReturnSignatureChar = signature
                .charAt(signature.indexOf(')') + 1);
        this.returnObject = (firstReturnSignatureChar == '[' || firstReturnSignatureChar == 'L');
        final VmClassLoader cl = declaringClass.getLoader();
        if (isStatic()) {
            this.selector = 0;
        } else {
            this.selector = cl.getSelectorMap().get(name, signature);
        }
        this.staticsIndex = cl.getStatics().allocMethod(this);
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
        this.returnVoid = src.returnVoid;
        this.returnObject = src.returnObject;
        this.selector = src.selector;
        this.staticsIndex = src.staticsIndex;
    }

    /**
     * Get the VmClass this method is declared in. *
     * 
     * @return The declaring class
     */
    public VmType getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Get the byte-code information for this method as loaded by the
     * classdecoder. This may return null if this is a native or abstract
     * method.
     * 
     * @return The original bytecode
     */
    public VmByteCode getOriginalBytecode() {
        return originalBytecode;
    }

    /**
     * Get the currently used byte-code information for this method. This
     * bytecode may have been optimized. This may return null if this is a
     * native or abstract method.
     * 
     * @return The current bytecode
     */
    public VmByteCode getBytecode() {
        return bytecode;
    }

    /**
     * Sets the bytecode information of this method.
     * 
     * @param bc
     */
    final void setBytecode(VmByteCode bc) {
        if (this.originalBytecode == null) {
            this.originalBytecode = bc;
        }
        this.bytecode = bc;
        bc.lock();
    }

    /**
     * Get the number of bytes in the byte-codes for this method.
     * 
     * @return Length of bytecode
     */
    public int getBytecodeSize() {
        return (bytecode == null) ? 0 : bytecode.getLength();
    }

    /**
     * Gets myself as java.lang.reflect.Method or
     * java.lang.reflect.Constructor, depending on isConstructor().
     * 
     * @return Method
     */
    public Member asMember() {
        if (javaMember == null) {
            if (isConstructor()) {
                javaMember = new Constructor(this);
            } else {
                javaMember = new Method(this);
            }
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
            mangledName = declaringClass.getMangledName()
                    + mangle("#" + getName() + '.' + getSignature());
        }
        return mangledName;
    }

    /**
     * Compile this method with n optimization level 1 higher then the current
     * optimization level.
     */
    public final synchronized void recompile() {
        doCompile(nativeCodeOptLevel + 1);
        invocationCount = 0;
    }

    /**
     * Compile all the methods in this class during runtime.
     * 
     * @param optLevel
     *            The optimization level
     */
    private synchronized void doCompile(int optLevel) {
        if (!declaringClass.isPrepared()) { throw new IllegalStateException(
                "Declaring class must have been prepared"); }
        declaringClass.getLoader().compileRuntime(this, optLevel);
        //setModifier(true, Modifier.ACC_COMPILED);
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
     * Resolve the type of this method
     * 
     * @param cl
     */
    protected synchronized void resolve(VmClassLoader cl) {
        resolveTypes();
    }

    private void resolveTypes() {
        if (paramTypes == null) {
            try {
                Signature sig = new Signature(getSignature(), declaringClass
                        .getLoader());
                returnType = sig.getReturnType();
                int count = sig.getParamCount();
                paramTypes = new VmType[ count];
                for (int i = 0; i < count; i++) {
                    paramTypes[ i] = sig.getParamType(i);
                }
            } catch (ClassNotFoundException ex) {
                throw (Error) new NoClassDefFoundError("In method "
                        + toString()).initCause(ex);
            }
        }
    }

    public int getNoArguments() {
        resolveTypes();
        return paramTypes.length;
    }

    public VmType getArgumentType(int index) {
        resolveTypes();
        return paramTypes[ index];
    }

    /**
     * Does the given array of types match my argument types?
     * 
     * @param argTypes
     * @return boolean
     */
    protected boolean matchArgumentTypes(VmType[] argTypes) {
        resolveTypes();
        int argTypesLength = (argTypes == null) ? 0 : argTypes.length;
        if (paramTypes.length != argTypesLength) { return false; }
        for (int i = 0; i < argTypesLength; i++) {
            if (argTypes[ i] != paramTypes[ i]) { return false; }
        }
        return true;
    }

    /**
     * @return VmClass
     */
    public VmType getReturnType() {
        resolveTypes();
        return returnType;
    }

    /**
     * Does this method return void?
     * 
     * @return boolean
     */
    public boolean isReturnVoid() {
        return returnVoid;
    }

    /**
     * Does this method return long or double?
     * 
     * @return boolean
     */
    public boolean isReturnWide() {
        return wide;
    }

    /**
     * @return boolean
     */
    public boolean isReturnObject() {
        return returnObject;
    }

    /**
     * Gets the exceptions this method has declared to throw
     * 
     * @return The exceptions this method has declared to throw, never null.
     */
    public VmExceptions getExceptions() {
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
            if (exceptions.contains(PragmaUninterruptible.class)) {
                setUninterruptible();
            }
        } else {
            throw new ClassFormatError(
                    "Cannot have more then 1 Exceptions attribute");
        }
    }

    /**
     * Gets the compiled code information of this method (if any)
     * 
     * @return The compiled code, or null if no compiled code has been set.
     */
    public final VmCompiledCode getCompiledCode() {
        return compiledCode;
    }

    /**
     * Install the generated code.
     * 
     * @param code
     * @param optLevel
     *            The optimization level of the generated code.
     */
    public final void setCompiledCode(VmCompiledCode code, int optLevel) {
        if ((this.nativeCode != null) && (optLevel <= nativeCodeOptLevel)) { throw new RuntimeException(
                "Cannot set code twice"); }
        this.nativeCode = code.getNativeCode();
        this.compiledCode = code;
        this.nativeCodeOptLevel = optLevel;
    }

    /**
     * Gets the global unique selector if this method name&amp;type.
     * 
     * @return The selector
     */
    public int getSelector() {
        return selector;
    }

    /**
     * Gets the number of stack slots used by the arguments of this method.
     * This number included the slot for "this" on non-static fields.
     * 
     * @return int
     */
    public final int getArgSlotCount() {
        return this.argSlotCount;
    }

    /**
     * Gets the number of invocations of this method.
     * 
     * @return int
     */
    public final int getInvocationCount() {
        return this.invocationCount;
    }

    /**
     * @return Returns the threadSwitchIndicatorMask.
     */
    public final int getThreadSwitchIndicatorMask() {
        return this.threadSwitchIndicatorMask;
    }

    /**
     * Mark this method as uninterruptable.
     *  
     */
    final void setUninterruptible() {
        this.threadSwitchIndicatorMask = 0;
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
     * Gets the indexe of this field in the statics table.
     * 
     * @return Returns the staticsIndex.
     */
    public final int getStaticsIndex() {
        return this.staticsIndex;
    }

    /**
     * @return Returns the lastInvocationCount.
     */
    public final int getLastInvocationCount() {
        return this.lastInvocationCount;
    }

    /**
     * Set the last invocation count to the current invocation count.
     */
    public final void setLastInvocationCount() {
        this.lastInvocationCount = invocationCount;
    }

    public boolean hasNativeCode() {
        if (nativeCode == null) {
            return false;
        } else {
            return true;
        }
    }

    public void testNativeCode() {
        if (declaringClass.isInterface()) { return; }
        if (nativeCode == null) {
            System.err.println("nativeCode == null in " + this);
        } else {
            final int ptr = Address.as32bit(nativeCode);
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
            if (!declaringClass.isInterface()) { throw new RuntimeException(
                    "nativeCode of " + this
                            + " is null; declaringclass compiled? "
                            + getDeclaringClass().isCompiled()); }
        }
    }
}
