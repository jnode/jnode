/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.jnode.vm.objects.VmSystemObject;
import org.jnode.annotation.PrivilegedActionPragma;

/**
 * Base class for annoted elements.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class VmAnnotatedElement extends VmSystemObject implements
    AnnotatedElement {

    /**
     * Runtime annotations
     */
    private VmAnnotation[] runtimeAnnotations;

    /**
     * @param runtimeAnnotations The runtimeAnnotations to set.
     */
    final void setRuntimeAnnotations(VmAnnotation[] runtimeAnnotations) {
        if (this.runtimeAnnotations == null) {
            if (runtimeAnnotations == null) {
                this.runtimeAnnotations = VmAnnotation.EMPTY_ARR;
            } else {
                this.runtimeAnnotations = runtimeAnnotations;
            }
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
            final VmType<T> reqType = VmType.fromClass(annotationClass);
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
        final Annotation[] arr = new Annotation[max];
        if (max > 0) {
            final VmClassLoader loader = getLoader();
            for (int i = 0; i < max; i++) {
                try {
                    arr[i] = runtimeAnnotations[i].getValue(loader);
                } catch (ClassNotFoundException e) {
                    throw new NoClassDefFoundError(e.getMessage());
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
        Class<? extends Annotation> annotationClass) {
        if (runtimeAnnotations.length > 0) {
            final VmClassLoader loader = getLoader();
            final VmType<?> reqType = VmType.fromClass((Class<? extends Annotation>) annotationClass);
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
     * @return the parent element
     */
    protected abstract VmAnnotatedElement getSuperElement();

    /**
     * Raw runtime annotations to be parsed by java.lang.Class of OpenJDK.
     */
    private byte[] rawAnnotations;

    /**
     * Returns the raw runtime annotations.
     * @return the raw annotation data as a byte[]
     */
    public byte[] getRawAnnotations() {
        return rawAnnotations;
    }

    /**
     * Sets the raw runtime annotations.
     * @param rawAnnotations the raw annotation data as a byte[]
     */
    public void setRawAnnotations(byte[] rawAnnotations) {
        this.rawAnnotations = rawAnnotations;
    }
}
