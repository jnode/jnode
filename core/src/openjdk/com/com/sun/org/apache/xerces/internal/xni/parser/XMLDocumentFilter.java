/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;

/**
 * Defines a document filter that acts as both a receiver and an emitter
 * of document events.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id: XMLDocumentFilter.java,v 1.2.6.1 2005/09/06 08:23:21 neerajbj Exp $
 */
public interface XMLDocumentFilter 
    extends XMLDocumentHandler, XMLDocumentSource {


} // interface XMLDocumentFilter
