/**
 * $Id$
 */

package org.jnode.vm.classmgr;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.jnode.vm.Address;
import org.jnode.vm.PragmaUninterruptible;
import org.jnode.vm.VmCompilationManager;

public abstract class VmMethod extends VmMember {

	/** Address of native code of this method */
	private Address nativeCode;
	/** #Arguments of this method */
	private final int noArgs;
	/** #Slots taken by arguments of this method */
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
	/** Profiler used in recordInvoke */
	private HotMethodDetector detector;
	/** The selector of this method name&type */
	private int selector;
	/** This field will be used to mask the thread switch indicator */
	private int threadSwitchIndicatorMask = 0xFFFFFFFF;

	/**
	 * Constructor for VmMethod.
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param declaringClass
	 * @param noArgs
	 * @param selectorMap
	 */
	protected VmMethod(
		String name,
		String signature,
		int modifiers,
		VmType declaringClass,
		int noArgs,
		SelectorMap selectorMap) {
		this(name, signature, modifiers, declaringClass, noArgs, selectorMap.get(name, signature));
	}

	/**
	 * Constructor for VmMethod.
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param declaringClass
	 * @param noArgs
	 * @param selector
	 */
	protected VmMethod(
		String name,
		String signature,
		int modifiers,
		VmType declaringClass,
		int noArgs,
		int selector) {
		super(name, signature, modifiers, declaringClass);
		this.noArgs = noArgs;
		this.argSlotCount = Signature.getArgSlotCount(signature);
		this.returnVoid = (signature.endsWith("V"));
		char firstReturnSignatureChar = signature.charAt(signature.indexOf(')') + 1);
		this.returnObject = (firstReturnSignatureChar == '[' || firstReturnSignatureChar == 'L');
		this.selector = selector;
		setProfile(!(isAbstract() | isNative()));
	}

	/**
	* Get the VmClass this method is declared in.
	** @return The declaring class
	 */
	public VmType getDeclaringClass() {
		return declaringClass;
	}

	/**
	 * Get the byte-code information for this method as loaded by the
	 * classdecoder.
	 * This may return null if this is a native or abstract method.
	 * @return The original bytecode
	 */
	public VmByteCode getOriginalBytecode() {
		return originalBytecode;
	}
	
	/**
	 * Get the currently used byte-code information for this method.
	 * This bytecode may have been optimized.
	 * This may return null if this is a native or abstract method.
	 * @return The current bytecode
	 */
	public VmByteCode getBytecode() {
		return bytecode;
	}
	
	/**
	 * Sets the bytecode information of this method.
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
	 * @return Length of bytecode
	 */
	public int getBytecodeSize() {
		return (bytecode == null) ? 0 : bytecode.getLength();
	}

	/**
	 * Get the number of arguments
	 * @return Number of arguments
	 */
	public int getNoArgs() {
		return noArgs;
	}

	/**
	 * Gets myself as java.lang.reflect.Method or java.lang.reflect.Constructor,
	 * depending on isConstructor().
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
	 * @return String
	 */
	public String toString() {
		return getMangledName();
	}

	/**
	 * Convert myself into a String representation
	 * @return The mangled name
	 */
	public String getMangledName() {
		return mangleClassName(declaringClass.getName())
			+ mangle("#" + getName() + '.' + getSignature());
	}

	/**
	 * Record an invocation of this method.
	 * This method is called by vm_invoke in vm-invoke.asm when
	 * this method is invocated AND ACC_PROFILE is set.
	 * 
	 * @see Modifier#ACC_PROFILE
	 * @throws PragmaUninterruptible
	 */
	protected final void recordInvoke() 
	throws PragmaUninterruptible {
		final HotMethodDetector detector = this.detector;
		if (detector != null) {
			if (!detector.isHot()) {
				detector.recordInvoke();
				if (detector.isHot()) {
					if (VmCompilationManager.recordHotMethod(this)) {
						setProfile(false);
					} else {
						detector.reset();
					}
				}
			}
		}
	}

	/**
	 * Compile all the methods in this class during runtime.
	 */
	public final void compile() {
		if (!isCompiled()) {
			doCompile();
		}
	}

	/**
	 * Compile all the methods in this class during runtime.
	 */
	private synchronized void doCompile() {
		if (!isCompiled()) {
			declaringClass.prepare();
			if (!isAbstract()) {
				declaringClass.getLoader().compile(this);
				setModifier(true, Modifier.ACC_COMPILED);
				setProfile(false);
			}
		}
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(getModifiers());
	}
	public boolean isNative() {
		return Modifier.isNative(getModifiers());
	}
	public boolean isSynchronized() {
		return Modifier.isSynchronized(getModifiers());
	}
	public boolean isConstructor() {
		return Modifier.isConstructor(getModifiers());
	}
	public boolean isInitializer() {
		return Modifier.isInitializer(getModifiers());
	}

	public boolean isCompiled() {
		return Modifier.isCompiled(getModifiers());
	}

	/**
	 * Resolve the type of this method
	 * @param cl
	 */
	protected synchronized void resolve(AbstractVmClassLoader cl) {
		resolveTypes();
	}

	private void resolveTypes() {
		if (paramTypes == null) {
			try {
				Signature sig =
					new Signature(getSignature(), declaringClass.getLoader());
				returnType = sig.getReturnType();
				int count = sig.getParamCount();
				paramTypes = new VmType[count];
				for (int i = 0; i < count; i++) {
					paramTypes[i] = sig.getParamType(i);
				}
			} catch (ClassNotFoundException ex) {
				throw (Error)new NoClassDefFoundError("In method " + toString()).initCause(ex);
			}
		}
	}

	public int getNoArguments() {
		resolveTypes();
		return paramTypes.length;
	}

	public VmType getArgumentType(int index) {
		resolveTypes();
		return paramTypes[index];
	}

	/**
	 * Does the given array of types match my argument types?
	 * @param argTypes
	 * @return boolean
	 */
	protected boolean matchArgumentTypes(VmType[] argTypes) {
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
	public VmType getReturnType() {
		resolveTypes();
		return returnType;
	}

	/**
	 * Does this method return void?
	 * @return boolean
	 */
	public boolean isReturnVoid() {
		return returnVoid;
	}

	/**
	 * Does this method return long or double?
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
	 * @param exceptions
	 * @throws ClassFormatError
	 */
	final void setExceptions(VmExceptions exceptions) 
	throws ClassFormatError {
		if (this.exceptions == null) {
			this.exceptions = exceptions;
			if (exceptions.contains(PragmaUninterruptible.class)) {
				setUninterruptible();
			}
		} else {
			throw new ClassFormatError("Cannot have more then 1 Exceptions attribute");
		}
	}
	
	/**
	 * Gets the compiled code information of this method (if any)
	 * @return The compiled code, or null if no compiled code has been set.
	 */
	public final VmCompiledCode getCompiledCode() {
		return compiledCode;
	}

	/**
	 * @param code
	 */
	public final void setCompiledCode(VmCompiledCode code) {
		if (this.nativeCode != null) {
			throw new RuntimeException("Cannot set code twice");
		}
		this.nativeCode = code.getNativeCode();
		this.compiledCode = code;
	}
	
	/**
	 * Should profiling be enabled on this method?
	 * @param on
	 */
	protected final void setProfile(boolean on) {
		if (on) {
			if (detector == null) {
				detector = new HotMethodDetector();
			}
		} else {
			detector = null;
		}
		setModifier(on, Modifier.ACC_PROFILE);
	}
	
	/**
	 * Gets the global unique selector if this method name&amp;type.
	 * @return The selector
	 */
	public int getSelector() {
		return selector;
	}

	/**
	 * @return
	 */
	int getArgSlotCount() {
		return this.argSlotCount;
	}
	
	/**
	 * @return
	 */
	int getInvocationCount() {
		return this.invocationCount;
	}

	/**
	 * @return Returns the threadSwitchIndicatorMask.
	 */
	public final int getThreadSwitchIndicatorMask() {
		return this.threadSwitchIndicatorMask;
	}
	
	final void setUninterruptible() {
		this.threadSwitchIndicatorMask = 0;
	}
}
