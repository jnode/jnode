/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.xml.internal.stream.buffer;

class FragmentedArray<T> {
    protected T _item;
    protected FragmentedArray<T> _next;
    protected FragmentedArray<T> _previous;
    
    FragmentedArray(T item) {
        this(item, null);
    }
    
    FragmentedArray(T item, FragmentedArray<T> previous) {
        setArray(item);
        if (previous != null) {
            previous._next = this;
            _previous = previous;
        }
    }
    
    T getArray() {
        return _item;
    }
    
    void setArray(T item) {
        assert(item.getClass().isArray());
        
        _item = item;
    }
    
    FragmentedArray<T> getNext() {
        return _next;
    }
    
    void setNext(FragmentedArray<T> next) {
        _next = next;
        if (next != null) {
            next._previous = this;
        }
    }
    
    FragmentedArray<T> getPrevious() {
        return _previous;
    }
    
    void setPrevious(FragmentedArray<T> previous) {
        _previous = previous;
        if (previous != null) {
            previous._next = this;
        }
    }
}
