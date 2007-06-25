/*
 * Portions Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.tools.internal.ws.wsdl.document.jaxws;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.sun.tools.internal.ws.wsdl.framework.Extensible;
import com.sun.tools.internal.ws.wsdl.framework.Extension;


/**
 * @author Vivek Pandey
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JAXWSBinding extends Extension implements Extensible {

    /**
     *
     */
    public JAXWSBinding(){
        super();
        jaxbBindings = new HashSet<Element>();
        // TODO Auto-generated constructor stub
    }


    /* (non-Javadoc)
     * @see Entity#validateThis()
     */
    public void validateThis(){
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see Elemental#getElementName()
     */
    public QName getElementName(){
        // TODO Auto-generated method stub
        return JAXWSBindingsConstants.JAXWS_BINDINGS;
    }

    /* (non-Javadoc)
     * @see Extensible#addExtension(Extension)
     */
    public void addExtension(Extension e) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see Extensible#extensions()
     */
    public Iterator extensions() {
        // TODO Auto-generated method stub
        return null;
    }


//    /**
//     * @return Returns the enableAdditionalHeaderMapping.
//     */
//    public Boolean isEnableAdditionalHeaderMapping() {
//        return enableAdditionalHeaderMapping;
//    }
//    /**
//     * @param enableAdditionalHeaderMapping The enableAdditionalHeaderMapping to set.
//     */
//    public void setEnableAdditionalHeaderMapping(
//            Boolean enableAdditionalHeaderMapping) {
//        this.enableAdditionalHeaderMapping = enableAdditionalHeaderMapping;
//    }
    /**
     * @return Returns the enableAsyncMapping.
     */
    public Boolean isEnableAsyncMapping() {
        return enableAsyncMapping;
    }
    /**
     * @param enableAsyncMapping The enableAsyncMapping to set.
     */
    public void setEnableAsyncMapping(Boolean enableAsyncMapping) {
        this.enableAsyncMapping = enableAsyncMapping;
    }
    /**
     * @return Returns the enableMimeContentMapping.
     */
    public Boolean isEnableMimeContentMapping() {
        return enableMimeContentMapping;
    }
    /**
     * @param enableMimeContentMapping The enableMimeContentMapping to set.
     */
    public void setEnableMimeContentMapping(Boolean enableMimeContentMapping) {
        this.enableMimeContentMapping = enableMimeContentMapping;
    }
    /**
     * @return Returns the enableWrapperStyle.
     */
    public Boolean isEnableWrapperStyle() {
        return enableWrapperStyle;
    }
    /**
     * @param enableWrapperStyle The enableWrapperStyle to set.
     */
    public void setEnableWrapperStyle(Boolean enableWrapperStyle) {
        this.enableWrapperStyle = enableWrapperStyle;
    }
    /**
     * @return Returns the jaxwsPackage.
     */
    public CustomName getJaxwsPackage() {
        return jaxwsPackage;
    }
    /**
     * @param jaxwsPackage The jaxwsPackage to set.
     */
    public void setJaxwsPackage(CustomName jaxwsPackage) {
        this.jaxwsPackage = jaxwsPackage;
    }
    /**
     * @return Returns the node.
     */
    public String getNode() {
        return node;
    }
    /**
     * @param node The node to set.
     */
    public void setNode(String node) {
        this.node = node;
    }
    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }
    /**
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return Returns the wsdlLocation.
     */
    public String getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * @param wsdlLocation The wsdlLocation to set.
     */
    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    /**
     * @return Returns the wsdlNamespace.
     */
    public String getWsdlNamespace() {
        return wsdlNamespace;
    }


    /**
     * @param wsdlNamespace The wsdlNamespace to set.
     */
    public void setWsdlNamespace(String wsdlNamespace) {
        this.wsdlNamespace = wsdlNamespace;
    }

    /**
     * @return Returns the jaxbBindings.
     */
    public Set<Element> getJaxbBindings() {
        return jaxbBindings;
    }

    /**
     * @param jaxbBinding The jaxbBindings to set.
     */
    public void addJaxbBindings(Element jaxbBinding) {
        if(jaxbBindings == null)
            return;
        this.jaxbBindings.add(jaxbBinding);
    }


    /**
     * @return the isProvider.
     */
    public Boolean isProvider() {
        return isProvider;
    }
    /**
     * @param isProvider The isProvider to set.
     */
    public void setProvider(Boolean isProvider) {
        this.isProvider = isProvider;
    }

    /* (non-Javadoc)
     * @see Entity#getProperty(java.lang.String)
     */
    public Object getProperty(String key) {
        if(key.equals(JAXWSBindingsConstants.JAXB_BINDINGS))
            return jaxbBindings;
        return null;
    }

    /**
     * @return Returns the methodName.
     */
    public CustomName getMethodName() {
        return methodName;
    }
    /**
     * @param methodName The methodName to set.
     */
    public void setMethodName(CustomName methodName) {
        this.methodName = methodName;
    }

    /**
     * @return Returns the parameter.
     */
    public Iterator<Parameter> parameters() {
        return parameters.iterator();
    }

    /**
     * @param parameter The parameter to set.
     */
    public void addParameter(Parameter parameter) {
        if(parameters == null)
            parameters = new ArrayList<Parameter>();
        parameters.add(parameter);
    }

    public String getParameterName(String msgName, String wsdlPartName, QName element, boolean wrapperStyle){
        if(msgName == null || wsdlPartName == null || element == null || parameters == null)
            return null;
        for(Parameter param : parameters){
            if(param.getMessageName().equals(msgName) && param.getPart().equals(wsdlPartName)){
                if(wrapperStyle && (param.getElement() != null)){
                    if(param.getElement().equals(element))
                        return param.getName();
                }else if(!wrapperStyle){
                    return param.getName();
                }
            }
        }
        return null;
    }

    /**
     * @return Returns the className.
     */
    public CustomName getClassName() {
        return className;
    }
    /**
     * @param className The className to set.
     */
    public void setClassName(CustomName className) {
        this.className = className;
    }

    /**
     * @return Returns the exception.
     */
    public Exception getException() {
        return exception;
    }
    /**
     * @param exception The exception to set.
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    private String wsdlNamespace;
    private String wsdlLocation;
    private String node;
    private String version;

    private CustomName jaxwsPackage;
    private List<Parameter> parameters;
    private Boolean enableWrapperStyle;
    private Boolean enableAsyncMapping;
//    private Boolean enableAdditionalHeaderMapping;
    private Boolean enableMimeContentMapping;
    private Boolean isProvider;
    private Exception exception;

    private Set<Element> jaxbBindings;

    // portType className
    private CustomName className;

    //portType Operation
    private CustomName methodName;
}
