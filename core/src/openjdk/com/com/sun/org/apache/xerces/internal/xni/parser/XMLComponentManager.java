/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 2000-2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.org.apache.xerces.internal.xni.parser;

/**
 * The component manager manages a parser configuration and the components
 * that make up that configuration. The manager notifies each component
 * before parsing to allow the components to initialize their state; and
 * also any time that a parser feature or property changes.
 * <p>
 * The methods of the component manager allow components to query features
 * and properties that affect the operation of the component.
 *
 * @see XMLComponent
 *
 * @author Andy Clark, IBM
 *
 * @version $Id: XMLComponentManager.java,v 1.2.6.1 2005/09/06 05:38:24 neerajbj Exp $
 */
public interface XMLComponentManager {

    //
    // XMLComponentManager methods
    //

    /**
     * Returns the state of a feature.
     * 
     * @param featureId The feature identifier.
     * 
     * @throws XMLConfigurationException Thrown on configuration error.
     */
    public boolean getFeature(String featureId)
        throws XMLConfigurationException;

    /**
     * Returns the value of a property.
     * 
     * @param propertyId The property identifier.
     * 
    * @throws XMLConfigurationException Thrown on configuration error.
     */
    public Object getProperty(String propertyId)
        throws XMLConfigurationException;

} // interface XMLComponentManager
