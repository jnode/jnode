/*
 * Copyright 1996-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
package java.beans;

import java.io.Serializable;
import java.io.ObjectStreamField;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map.Entry;

/**
 * This is a utility class that can be used by beans that support constrained
 * properties.  You can use an instance of this class as a member field
 * of your bean and delegate various work to it.
 *
 * This class is serializable.  When it is serialized it will save
 * (and restore) any listeners that are themselves serializable.  Any
 * non-serializable listeners will be skipped during serialization.
 */
public class VetoableChangeSupport implements Serializable {
    private VetoableChangeListenerMap map = new VetoableChangeListenerMap();

    /**
     * Constructs a <code>VetoableChangeSupport</code> object.
     *
     * @param sourceBean  The bean to be given as the source for any events.
     */
    public VetoableChangeSupport(Object sourceBean) {
	if (sourceBean == null) {
	    throw new NullPointerException();
	}
	source = sourceBean;
    }

    /**
     * Add a VetoableChangeListener to the listener list.
     * The listener is registered for all properties.
     * The same listener object may be added more than once, and will be called
     * as many times as it is added.
     * If <code>listener</code> is null, no exception is thrown and no action
     * is taken.
     *
     * @param listener  The VetoableChangeListener to be added
     */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listener instanceof VetoableChangeListenerProxy) {
            VetoableChangeListenerProxy proxy =
                    (VetoableChangeListenerProxy)listener;
            // Call two argument add method.
            addVetoableChangeListener(proxy.getPropertyName(),
                    (VetoableChangeListener)proxy.getListener());
        } else {
            this.map.add(null, listener);
        }
    }

    /**
     * Remove a VetoableChangeListener from the listener list.
     * This removes a VetoableChangeListener that was registered
     * for all properties.
     * If <code>listener</code> was added more than once to the same event
     * source, it will be notified one less time after being removed.
     * If <code>listener</code> is null, or was never added, no exception is
     * thrown and no action is taken.
     *
     * @param listener  The VetoableChangeListener to be removed
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listener instanceof VetoableChangeListenerProxy) {
            VetoableChangeListenerProxy proxy =
                    (VetoableChangeListenerProxy)listener;
            // Call two argument remove method.
            removeVetoableChangeListener(proxy.getPropertyName(),
                    (VetoableChangeListener)proxy.getListener());
        } else {
            this.map.remove(null, listener);
        }
    }

    /**
     * Returns the list of VetoableChangeListeners. If named vetoable change listeners
     * were added, then VetoableChangeListenerProxy wrappers will returned
     * <p>
     * @return List of VetoableChangeListeners and VetoableChangeListenerProxys
     *         if named property change listeners were added.
     * @since 1.4
     */
    public VetoableChangeListener[] getVetoableChangeListeners(){
        return this.map.getListeners();
    }

    /**
     * Add a VetoableChangeListener for a specific property.  The listener
     * will be invoked only when a call on fireVetoableChange names that
     * specific property.
     * The same listener object may be added more than once.  For each
     * property,  the listener will be invoked the number of times it was added
     * for that property.
     * If <code>propertyName</code> or <code>listener</code> is null, no
     * exception is thrown and no action is taken.
     *
     * @param propertyName  The name of the property to listen on.
     * @param listener  The VetoableChangeListener to be added
     */
    public void addVetoableChangeListener(
				String propertyName,
                VetoableChangeListener listener) {
        if (listener == null || propertyName == null) {
            return;
        }
        listener = this.map.extract(listener);
        if (listener != null) {
            this.map.add(propertyName, listener);
        }
    }

    /**
     * Remove a VetoableChangeListener for a specific property.
     * If <code>listener</code> was added more than once to the same event
     * source for the specified property, it will be notified one less time
     * after being removed.
     * If <code>propertyName</code> is null, no exception is thrown and no
     * action is taken.
     * If <code>listener</code> is null, or was never added for the specified
     * property, no exception is thrown and no action is taken.
     *
     * @param propertyName  The name of the property that was listened on.
     * @param listener  The VetoableChangeListener to be removed
     */
    public void removeVetoableChangeListener(
				String propertyName,
                VetoableChangeListener listener) {
        if (listener == null || propertyName == null) {
            return;
        }
        listener = this.map.extract(listener);
        if (listener != null) {
            this.map.remove(propertyName, listener);
        }
    }

    /**
     * Returns an array of all the listeners which have been associated 
     * with the named property.
     *
     * @param propertyName  The name of the property being listened to
     * @return all the <code>VetoableChangeListeners</code> associated with
     *         the named property.  If no such listeners have been added,
     *         or if <code>propertyName</code> is null, an empty array is
     *         returned.
     * @since 1.4
     */
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return this.map.getListeners(propertyName);
    }

    /**
     * Report a vetoable property update to any registered listeners.  If
     * anyone vetos the change, then fire a new event reverting everyone to 
     * the old value and then rethrow the PropertyVetoException.
     * <p>
     * No event is fired if old and new are equal and non-null.
     *
     * @param propertyName  The programmatic name of the property
     *		that is about to change..
     * @param oldValue  The old value of the property.
     * @param newValue  The new value of the property.
     * @exception PropertyVetoException if the recipient wishes the property
     *              change to be rolled back.
     */
    public void fireVetoableChange(String propertyName, 
					Object oldValue, Object newValue)
					throws PropertyVetoException {
        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
	    return;
	}
       	PropertyChangeEvent evt = new PropertyChangeEvent(source, propertyName,
							    oldValue, newValue);
	fireVetoableChange(evt);
    }

    /**
     * Report a int vetoable property update to any registered listeners.
     * No event is fired if old and new are equal.
     * <p>
     * This is merely a convenience wrapper around the more general
     * fireVetoableChange method that takes Object values.
     *
     * @param propertyName  The programmatic name of the property
     *		that is about to change.
     * @param oldValue  The old value of the property.
     * @param newValue  The new value of the property.
     */
    public void fireVetoableChange(String propertyName, 
					int oldValue, int newValue)
					throws PropertyVetoException {
	if (oldValue == newValue) {
	    return;
	}
        fireVetoableChange(propertyName, Integer.valueOf(oldValue), Integer.valueOf(newValue));
    }

    /**
     * Report a boolean vetoable property update to any registered listeners.
     * No event is fired if old and new are equal.
     * <p>
     * This is merely a convenience wrapper around the more general
     * fireVetoableChange method that takes Object values.
     *
     * @param propertyName  The programmatic name of the property
     *		that is about to change.
     * @param oldValue  The old value of the property.
     * @param newValue  The new value of the property.
     */
    public void fireVetoableChange(String propertyName, 
					boolean oldValue, boolean newValue) 
					throws PropertyVetoException {
	if (oldValue == newValue) {
	    return;
	}
	fireVetoableChange(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
    }

    /**
     * Fire a vetoable property update to any registered listeners.  If
     * anyone vetos the change, then fire a new event reverting everyone to 
     * the old value and then rethrow the PropertyVetoException.
     * <p>
     * No event is fired if old and new are equal and non-null.
     *
     * @param evt  The PropertyChangeEvent to be fired.
     * @exception PropertyVetoException if the recipient wishes the property
     *              change to be rolled back.
     */
    public void fireVetoableChange(PropertyChangeEvent evt)
					throws PropertyVetoException {

	Object oldValue = evt.getOldValue();
	Object newValue = evt.getNewValue();
	String propertyName = evt.getPropertyName();
	if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
	    return;
	}
        VetoableChangeListener[] common = this.map.get(null);
        VetoableChangeListener[] named = (propertyName != null)
                    ? this.map.get(propertyName)
                    : null;
        fire(common, evt);
        fire(named, evt);
	}

    private void fire(VetoableChangeListener[] listeners, PropertyChangeEvent event) throws PropertyVetoException {
	if (listeners != null) {
            VetoableChangeListener current = null;
	    try {
                for (VetoableChangeListener listener : listeners) {
                    current = listener;
                    listener.vetoableChange(event);
	        }
	    } catch (PropertyVetoException veto) {
	        // Create an event to revert everyone to the old value.
                event = new PropertyChangeEvent( this.source,
                                                 event.getPropertyName(),
                                                 event.getNewValue(),
                                                 event.getOldValue() );
                for (VetoableChangeListener listener : listeners) {
		    try {
                        listener.vetoableChange(event);
		    } catch (PropertyVetoException ex) {
		         // We just ignore exceptions that occur during reversions.
		    }
	        }
	        // And now rethrow the PropertyVetoException.
	        throw veto;
	    }
	}
    }

    /**
     * Check if there are any listeners for a specific property, including
     * those registered on all properties.  If <code>propertyName</code>
     * is null, only check for listeners registered on all properties.
     *
     * @param propertyName  the property name.
     * @return true if there are one or more listeners for the given property
     */
    public boolean hasListeners(String propertyName) {
        return this.map.hasListeners(propertyName);
    }

    /**
     * @serialData Null terminated list of <code>VetoableChangeListeners</code>.
     * <p>
     * At serialization time we skip non-serializable listeners and
     * only serialize the serializable listeners.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Hashtable<String, VetoableChangeSupport> children = null;
        VetoableChangeListener[] listeners = null;
        synchronized (this.map) {
            for (Entry<String, VetoableChangeListener[]> entry : this.map.getEntries()) {
                String property = entry.getKey();
                if (property == null) {
                    listeners = entry.getValue();
                } else {
                    if (children == null) {
                        children = new Hashtable<String, VetoableChangeSupport>();
                    }
                    VetoableChangeSupport vcs = new VetoableChangeSupport(this.source);
                    vcs.map.set(null, entry.getValue());
                    children.put(property, vcs);
                }
            }
	}
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("children", children);
        fields.put("source", this.source);
        fields.put("vetoableChangeSupportSerializedDataVersion", 2);
        s.writeFields();

        if (listeners != null) {
            for (VetoableChangeListener l : listeners) {
	        if (l instanceof Serializable) {
	            s.writeObject(l);
	        }
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        this.map = new VetoableChangeListenerMap();

        ObjectInputStream.GetField fields = s.readFields();

        Hashtable<String, VetoableChangeSupport> children = (Hashtable<String, VetoableChangeSupport>) fields.get("children", null);
        this.source = fields.get("source", null);
        fields.get("vetoableChangeSupportSerializedDataVersion", 2);
      
        Object listenerOrNull;
        while (null != (listenerOrNull = s.readObject())) {
            this.map.add(null, (VetoableChangeListener)listenerOrNull);
        }
        if (children != null) {
            for (Entry<String, VetoableChangeSupport> entry : children.entrySet()) {
                for (VetoableChangeListener listener : entry.getValue().getVetoableChangeListeners()) {
                    this.map.add(entry.getKey(), listener);
                }
            }
        }
    }

    /**
     * The object to be provided as the "source" for any generated events.
     */
    private Object source;

    /** 
     * @serialField children                                   Hashtable
     * @serialField source                                     Object
     * @serialField propertyChangeSupportSerializedDataVersion int
     */
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("children", Hashtable.class),
            new ObjectStreamField("source", Object.class),
            new ObjectStreamField("vetoableChangeSupportSerializedDataVersion", Integer.TYPE)
    };

    /**
     * Serialization version ID, so we're compatible with JDK 1.1
     */
    static final long serialVersionUID = -5090210921595982017L;

    /** 
     * This is a {@link ChangeListenerMap ChangeListenerMap} implementation
     * that works with {@link VetoableChangeListener VetoableChangeListener} objects.
     */
    private static final class VetoableChangeListenerMap extends ChangeListenerMap<VetoableChangeListener> {
        private static final VetoableChangeListener[] EMPTY = {};

    /**
         * Creates an array of {@link VetoableChangeListener VetoableChangeListener} objects.
         * This method uses the same instance of the empty array
         * when {@code length} equals {@code 0}.
         *
         * @param length  the array length
         * @return        an array with specified length
     */
        @Override
        protected VetoableChangeListener[] newArray(int length) {
            return (0 < length)
                    ? new VetoableChangeListener[length]
                    : EMPTY;
        }

    /**
         * Creates a {@link VetoableChangeListenerProxy VetoableChangeListenerProxy}
         * object for the specified property.
         *
         * @param name      the name of the property to listen on
         * @param listener  the listener to process events
         * @return          a {@code VetoableChangeListenerProxy} object
     */
        @Override
        protected VetoableChangeListener newProxy(String name, VetoableChangeListener listener) {
            return new VetoableChangeListenerProxy(name, listener);
        }
    }
}
