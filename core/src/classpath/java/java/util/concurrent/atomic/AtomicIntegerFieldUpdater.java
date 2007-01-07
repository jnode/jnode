/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent.atomic;
import sun.misc.Unsafe;
import java.lang.reflect.*;

/**
 * A reflection-based utility that enables atomic updates to
 * designated <tt>volatile int</tt> fields of designated classes.
 * This class is designed for use in atomic data structures in which
 * several fields of the same node are independently subject to atomic
 * updates.
 *
 * <p>Note that the guarantees of the {@code compareAndSet}
 * method in this class are weaker than in other atomic classes.
 * Because this class cannot ensure that all uses of the field
 * are appropriate for purposes of atomic access, it can
 * guarantee atomicity only with respect to other invocations of
 * {@code compareAndSet} and {@code set} on the same updater.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <T> The type of the object holding the updatable field
 */
public abstract class  AtomicIntegerFieldUpdater<T>  {
    /**
     * Creates and returns an updater for objects with the given field.
     * The Class argument is needed to check that reflective types and
     * generic types match.
     *
     * @param tclass the class of the objects holding the field
     * @param fieldName the name of the field to be updated
     * @return the updater
     * @throws IllegalArgumentException if the field is not a
     * volatile integer type
     * @throws RuntimeException with a nested reflection-based
     * exception if the class does not hold field or is the wrong type
     */
    public static <U> AtomicIntegerFieldUpdater<U> newUpdater(Class<U> tclass, String fieldName) {
        return new AtomicIntegerFieldUpdaterImpl<U>(tclass, fieldName);
    }

    /**
     * Protected do-nothing constructor for use by subclasses.
     */
    protected AtomicIntegerFieldUpdater() {
    }

    /**
     * Atomically sets the field of the given object managed by this updater
     * to the given updated value if the current value <tt>==</tt> the
     * expected value. This method is guaranteed to be atomic with respect to
     * other calls to <tt>compareAndSet</tt> and <tt>set</tt>, but not
     * necessarily with respect to other changes in the field.
     *
     * @param obj An object whose field to conditionally set
     * @param expect the expected value
     * @param update the new value
     * @return true if successful
     * @throws ClassCastException if <tt>obj</tt> is not an instance
     * of the class possessing the field established in the constructor
     */
    public abstract boolean compareAndSet(T obj, int expect, int update);

    /**
     * Atomically sets the field of the given object managed by this updater
     * to the given updated value if the current value <tt>==</tt> the
     * expected value. This method is guaranteed to be atomic with respect to
     * other calls to <tt>compareAndSet</tt> and <tt>set</tt>, but not
     * necessarily with respect to other changes in the field.
     * May fail spuriously and does not provide ordering guarantees,
     * so is only rarely an appropriate alternative to <tt>compareAndSet</tt>.
     *
     * @param obj An object whose field to conditionally set
     * @param expect the expected value
     * @param update the new value
     * @return true if successful
     * @throws ClassCastException if <tt>obj</tt> is not an instance
     * of the class possessing the field established in the constructor
     */
    public abstract boolean weakCompareAndSet(T obj, int expect, int update);

    /**
     * Sets the field of the given object managed by this updater to the
     * given updated value. This operation is guaranteed to act as a volatile
     * store with respect to subsequent invocations of
     * <tt>compareAndSet</tt>.
     *
     * @param obj An object whose field to set
     * @param newValue the new value
     */
    public abstract void set(T obj, int newValue);

    /**
     * Eventually sets the field of the given object managed by this
     * updater to the given updated value.
     *
     * @param obj An object whose field to set
     * @param newValue the new value
     * @since 1.6
     */
    public abstract void lazySet(T obj, int newValue);


    /**
     * Gets the current value held in the field of the given object managed
     * by this updater.
     *
     * @param obj An object whose field to get
     * @return the current value
     */
    public abstract int get(T obj);

    /**
     * Atomically sets the field of the given object managed by this updater
     * to the given value and returns the old value.
     *
     * @param obj An object whose field to get and set
     * @param newValue the new value
     * @return the previous value
     */
    public int getAndSet(T obj, int newValue) {
        for (;;) {
            int current = get(obj);
            if (compareAndSet(obj, current, newValue))
                return current;
        }
    }

    /**
     * Atomically increments by one the current value of the field of the
     * given object managed by this updater.
     *
     * @param obj An object whose field to get and set
     * @return the previous value
     */
    public int getAndIncrement(T obj) {
        for (;;) {
            int current = get(obj);
            int next = current + 1;
            if (compareAndSet(obj, current, next))
                return current;
        }
    }

    /**
     * Atomically decrements by one the current value of the field of the
     * given object managed by this updater.
     *
     * @param obj An object whose field to get and set
     * @return the previous value
     */
    public int getAndDecrement(T obj) {
        for (;;) {
            int current = get(obj);
            int next = current - 1;
            if (compareAndSet(obj, current, next))
                return current;
        }
    }

    /**
     * Atomically adds the given value to the current value of the field of
     * the given object managed by this updater.
     *
     * @param obj An object whose field to get and set
     * @param delta the value to add
     * @return the previous value
     */
    public int getAndAdd(T obj, int delta) {
        for (;;) {
            int current = get(obj);
            int next = current + delta;
            if (compareAndSet(obj, current, next))
                return current;
        }
    }

    /**
     * Atomically increments by one the current value of the field of the
     * given object managed by this updater.
     *
     * @param obj An object whose field to get and set
     * @return the updated value
     */
    public int incrementAndGet(T obj) {
        for (;;) {
            int current = get(obj);
            int next = current + 1;
            if (compareAndSet(obj, current, next))
                return next;
        }
    }

    /**
     * Atomically decrements by one the current value of the field of the
     * given object managed by this updater.
     *
     * @param obj An object whose field to get and set
     * @return the updated value
     */
    public int decrementAndGet(T obj) {
        for (;;) {
            int current = get(obj);
            int next = current - 1;
            if (compareAndSet(obj, current, next))
                return next;
        }
    }

    /**
     * Atomically adds the given value to the current value of the field of
     * the given object managed by this updater.
     *
     * @param obj An object whose field to get and set
     * @param delta the value to add
     * @return the updated value
     */
    public int addAndGet(T obj, int delta) {
        for (;;) {
            int current = get(obj);
            int next = current + delta;
            if (compareAndSet(obj, current, next))
                return next;
        }
    }

    /**
     * Standard hotspot implementation using intrinsics
     */
    private static class AtomicIntegerFieldUpdaterImpl<T> extends AtomicIntegerFieldUpdater<T> {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private final long offset;
        private final Class<T> tclass;
        private final Class cclass;

        AtomicIntegerFieldUpdaterImpl(Class<T> tclass, String fieldName) {
            Field field = null;
	    Class caller = null;
	    int modifiers = 0;
            try {
                field = tclass.getDeclaredField(fieldName);
		caller = sun.reflect.Reflection.getCallerClass(3);
		modifiers = field.getModifiers();
                sun.reflect.misc.ReflectUtil.ensureMemberAccess(
                    caller, tclass, null, modifiers); 
		sun.reflect.misc.ReflectUtil.checkPackageAccess(tclass);
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }

            Class fieldt = field.getType();
            if (fieldt != int.class)
                throw new IllegalArgumentException("Must be integer type");
            
            if (!Modifier.isVolatile(modifiers))
                throw new IllegalArgumentException("Must be volatile type");
         
	    this.cclass = (Modifier.isProtected(modifiers) &&
			   caller != tclass) ? caller : null;
            this.tclass = tclass;
            offset = unsafe.objectFieldOffset(field);
        }

        private void fullCheck(T obj) {
            if (!tclass.isInstance(obj))
                throw new ClassCastException();
	    if (cclass != null)
		ensureProtectedAccess(obj);
        }

        public boolean compareAndSet(T obj, int expect, int update) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.compareAndSwapInt(obj, offset, expect, update);
        }

        public boolean weakCompareAndSet(T obj, int expect, int update) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.compareAndSwapInt(obj, offset, expect, update);
        }

        public void set(T obj, int newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            unsafe.putIntVolatile(obj, offset, newValue);
        }

        public void lazySet(T obj, int newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            unsafe.putOrderedInt(obj, offset, newValue);
        }

        public final int get(T obj) {
            if (obj == null || obj.getClass() != tclass || cclass != null) fullCheck(obj);
            return unsafe.getIntVolatile(obj, offset);
        }

	private void ensureProtectedAccess(T obj) {
	    if (cclass.isInstance(obj)) {
		return;
	    }
	    throw new RuntimeException(
                new IllegalAccessException("Class " +
		    cclass.getName() +
                    " can not access a protected member of class " +
                    tclass.getName() +
		    " using an instance of " +
                    obj.getClass().getName()
		)
	    );
	}
    }
}
