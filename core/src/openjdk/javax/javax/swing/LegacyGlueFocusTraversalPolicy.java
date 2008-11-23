/*
 * Copyright 2000-2002 Sun Microsystems, Inc.  All Rights Reserved.
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
package javax.swing;

import java.awt.FocusTraversalPolicy;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.HashMap;
import java.util.HashSet;
import java.io.*;


/**
 * A FocusTraversalPolicy which provides support for legacy applications which
 * handle focus traversal via JComponent.setNextFocusableComponent or by
 * installing a custom DefaultFocusManager. If a specific traversal has not
 * been hard coded, then that traversal is provided either by the custom
 * DefaultFocusManager, or by a wrapped FocusTraversalPolicy instance.
 *
 * @author David Mendenhall
 */
final class LegacyGlueFocusTraversalPolicy extends FocusTraversalPolicy
    implements Serializable
{
    private transient FocusTraversalPolicy delegatePolicy;
    private transient DefaultFocusManager delegateManager;

    private HashMap forwardMap = new HashMap(),
        backwardMap = new HashMap();

    LegacyGlueFocusTraversalPolicy(FocusTraversalPolicy delegatePolicy) {
        this.delegatePolicy = delegatePolicy;
    }
    LegacyGlueFocusTraversalPolicy(DefaultFocusManager delegateManager) {
        this.delegateManager = delegateManager;
    }

    void setNextFocusableComponent(Component left, Component right) {
        forwardMap.put(left, right);
        backwardMap.put(right, left);
    }
    void unsetNextFocusableComponent(Component left, Component right) {
        forwardMap.remove(left);
        backwardMap.remove(right);
    }

    public Component getComponentAfter(Container focusCycleRoot,
                                       Component aComponent) {
        Component hardCoded = aComponent, prevHardCoded;
        HashSet sanity = new HashSet();

        do {
            prevHardCoded = hardCoded;
            hardCoded = (Component)forwardMap.get(hardCoded);
            if (hardCoded == null) {
                if (delegatePolicy != null &&
		    prevHardCoded.isFocusCycleRoot(focusCycleRoot)) {
                    return delegatePolicy.getComponentAfter(focusCycleRoot,
                                                            prevHardCoded);
                } else if (delegateManager != null) {
                    return delegateManager.
                        getComponentAfter(focusCycleRoot, aComponent);
                } else {
                    return null;
                }
            }
            if (sanity.contains(hardCoded)) {
                // cycle detected; bail
                return null;
            }
            sanity.add(hardCoded);
        } while (!accept(hardCoded));

        return hardCoded;
    }
    public Component getComponentBefore(Container focusCycleRoot,
                                        Component aComponent) {
        Component hardCoded = aComponent, prevHardCoded;
        HashSet sanity = new HashSet();

        do {
            prevHardCoded = hardCoded;
            hardCoded = (Component)backwardMap.get(hardCoded);
            if (hardCoded == null) {
                if (delegatePolicy != null &&
		    prevHardCoded.isFocusCycleRoot(focusCycleRoot)) {
                    return delegatePolicy.getComponentBefore(focusCycleRoot,
                                                       prevHardCoded);
                } else if (delegateManager != null) {
                    return delegateManager.
                        getComponentBefore(focusCycleRoot, aComponent);
                } else {
                    return null;
                }
            }
            if (sanity.contains(hardCoded)) {
                // cycle detected; bail
                return null;
            }
            sanity.add(hardCoded);
        } while (!accept(hardCoded));

        return hardCoded;
    }
    public Component getFirstComponent(Container focusCycleRoot) {
        if (delegatePolicy != null) {
            return delegatePolicy.getFirstComponent(focusCycleRoot);
        } else if (delegateManager != null) {
            return delegateManager.getFirstComponent(focusCycleRoot);
        } else {
            return null;
        }
    }
    public Component getLastComponent(Container focusCycleRoot) {
        if (delegatePolicy != null) {
            return delegatePolicy.getLastComponent(focusCycleRoot);
        } else if (delegateManager != null) {
            return delegateManager.getLastComponent(focusCycleRoot);
        } else {
            return null;
        }
    }
    public Component getDefaultComponent(Container focusCycleRoot) {
        if (delegatePolicy != null) {
            return delegatePolicy.getDefaultComponent(focusCycleRoot);
        } else {
            return getFirstComponent(focusCycleRoot);
        }
    }
    private boolean accept(Component aComponent) {
        if (!(aComponent.isVisible() && aComponent.isDisplayable() &&
              aComponent.isFocusable() && aComponent.isEnabled())) {
            return false;
        }

        // Verify that the Component is recursively enabled. Disabling a
        // heavyweight Container disables its children, whereas disabling
        // a lightweight Container does not.
        if (!(aComponent instanceof Window)) {
            for (Container enableTest = aComponent.getParent();
                 enableTest != null;
                 enableTest = enableTest.getParent())
            {
                if (!(enableTest.isEnabled() || enableTest.isLightweight())) {
                    return false;
                }
                if (enableTest instanceof Window) {
                    break;
                }
            }
        }

        return true;
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        if (delegatePolicy instanceof Serializable) {
            out.writeObject(delegatePolicy);
        } else {
            out.writeObject(null);
        }

        if (delegateManager instanceof Serializable) {
            out.writeObject(delegateManager);
        } else {
            out.writeObject(null);
        }
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        delegatePolicy = (FocusTraversalPolicy)in.readObject();
        delegateManager = (DefaultFocusManager)in.readObject();
    }
}
