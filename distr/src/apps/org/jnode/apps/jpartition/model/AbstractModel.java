package org.jnode.apps.jpartition.model;

import it.battlehorse.stamps.Model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

abstract public class AbstractModel implements Model 
{
    final protected PropertyChangeSupport propSupport;

    /**
     * Creates a new instance of the class
     */
    public AbstractModel() {
        propSupport = new PropertyChangeSupport(this);
	}
	
    //
    // This method will be invoked by the dispatcher on model registration
    //
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    //
    // This method will be invoked by the dispatcher on model deregistration
    //
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }
}
