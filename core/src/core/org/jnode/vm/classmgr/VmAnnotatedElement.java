/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.jnode.vm.VmSystemObject;
import org.jnode.vm.annotation.PrivilegedActionPragma;

/**
 * Base class for annoted elements.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class VmAnnotatedElement extends VmSystemObject implements
        AnnotatedElement {

    /** Runtime annotations */
    private VmAnnotation[] runtimeAnnotations;

    /**
     * @param runtimeAnnotations
     *            The runtimeAnnotations to set.
     */
    final void setRuntimeAnnotations(VmAnnotation[] runtimeAnnotations) {
        if (this.runtimeAnnotations == null) {
            this.runtimeAnnotations = runtimeAnnotations;
        } else {
            throw new SecurityException("Cannot override runtime annotations");
        }
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#getAnnotation(java.lang.Class)
     */
    @PrivilegedActionPragma
    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (runtimeAnnotations.length > 0) {
            final VmClassLoader loader = getLoader();
            final VmType<T> reqType = annotationClass.getVmClass();
            for (VmAnnotation ann : runtimeAnnotations) {
                if (ann.annotationType(loader) == reqType) {
                    try {
                        return annotationClass.cast(ann.getValue(loader));
                    } catch (ClassNotFoundException ex) {
                        throw new NoClassDefFoundError(ex.getMessage());
                    }
                }
            }
        }
        return null;
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#getAnnotations()
     */
    public final Annotation[] getAnnotations() {
        final Annotation[] ann = getDeclaredAnnotations();
        final VmAnnotatedElement parent = getSuperElement();
        if (parent != null) {
            final Annotation[] parentAnn = parent.getAnnotations();
            int cnt = 0;
            for (Annotation a : parentAnn) {
                if (((VmAnnotation.ImplBase) a).isInheritable()) {
                    cnt++;
                }
            }
            if (cnt > 0) {
                int j = ann.length;
                final Annotation[] result = new Annotation[j + cnt];
                System.arraycopy(ann, 0, result, 0, j);
                for (Annotation a : parentAnn) {
                    if (((VmAnnotation.ImplBase) a).isInheritable()) {
                        result[j++] = a;
                    }
                }
                return result;
            }
        }

        return ann;
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#getDeclaredAnnotations()
     */
    public final Annotation[] getDeclaredAnnotations() {
        final int max = runtimeAnnotations.length;
        // Count the runtime visible annotations
        int cnt = 0;
        for (int i = 0; i < max; i++) {
            if (runtimeAnnotations[i].isRuntimeVisible()) {
                cnt++;
            }
        }
        final Annotation[] arr = new Annotation[cnt];
        if (cnt > 0) {
            final VmClassLoader loader = getLoader();
            cnt = 0;
            for (int i = 0; i < max; i++) {
                if (runtimeAnnotations[i].isRuntimeVisible()) {
                    try {
                        arr[cnt++] = runtimeAnnotations[i].getValue(loader);
                    } catch (ClassNotFoundException e) {
                        throw new NoClassDefFoundError(e.getMessage());
                    }
                }
            }
        }
        return arr;
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#isAnnotationPresent(java.lang.Class)
     */
    @PrivilegedActionPragma
    public final boolean isAnnotationPresent(
            Class< ? extends Annotation> annotationClass) {
        if (runtimeAnnotations.length > 0) {
            final VmClassLoader loader = getLoader();
            final VmType< ? > reqType = annotationClass.getVmClass();
            for (VmAnnotation ann : runtimeAnnotations) {
                if (ann.annotationType(loader) == reqType) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return the loader of this class
     * 
     * @return The loader
     */
    protected abstract VmClassLoader getLoader();

    /**
     * Gets the parent of this element.
     * 
     * @return
     */
    protected abstract VmAnnotatedElement getSuperElement();
}
