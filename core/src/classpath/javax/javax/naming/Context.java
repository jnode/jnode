/* Context.java --
   Copyright (C) 2000 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package javax.naming;

import java.util.Hashtable;

public interface Context
{
  // Property with name of the inital context factory to use
  String INITIAL_CONTEXT_FACTORY 
    = "java.naming.factory.initial";

  // Property with colon-separated list of object factories to use.
  String OBJECT_FACTORIES
    = "java.naming.factory.object";

  // Property with colon-separated list of state factories to use.
  String STATE_FACTORIES
    = "java.naming.factory.state";

  // Property with colon-separated list of package prefixes to use.
  String URL_PKG_PREFIXES
    = "java.naming.factory.url.pkgs";

  // Property with URL specifying configuration for the service
  // provider to use.
  String PROVIDER_URL 
    = "java.naming.provider.url";
  
  // Property with the DNS host and domain names to use.
  String DNS_URL 
    = "java.naming.dns.url";
  
  // Property with the authoritativeness of the service requested.
  String AUTHORITATIVE 
    = "java.naming.authoritative";
  
  // Property with the batch size to use when returning data via the
  // service's protocol.
  String BATCHSIZE
    = "java.naming.batchsize";
  
  // Property defining how referrals encountered by the service
  // provider are to be processed.
  String REFERRAL
    = "java.naming.referral";

  // Property specifying the security protocol to use.
  String SECURITY_PROTOCOL
    = "java.naming.security.protocol";

  // Property specifying the security level to use.
  String SECURITY_AUTHENTICATION
    = "java.naming.security.authentication";

  // Property for the identity of the principal for authenticating
  // the caller to the service.
  String SECURITY_PRINCIPAL
    = "java.naming.security.principal";

  // Property specifying the credentials of the principal for
  // authenticating the caller to the service.
  String SECURITY_CREDENTIALS
    = "java.naming.security.credentials";

  // Property for specifying the preferred language to use with the
  // service.
  String LANGUAGE
    = "java.naming.language";

  // Property for the initial context constructor to use when searching
  // for other properties.
  String APPLET
    = "java.naming.applet";

  void bind (Name name, Object obj) throws NamingException;
  void bind (String name, Object obj) throws NamingException;

  Object lookup (Name name) throws NamingException;
  Object lookup (String name) throws NamingException;

  void rebind (Name name, Object obj) throws NamingException;
  void rebind (String name, Object obj) throws NamingException;

  void unbind (Name name) throws NamingException;
  void unbind (String name) throws NamingException;

  void rename (Name oldName, Name newName) throws NamingException;
  void rename (String oldName, String newName) throws NamingException;

  NamingEnumeration list (Name name) throws NamingException;
  NamingEnumeration list (String name) throws NamingException;

  NamingEnumeration listBindings (Name name) throws NamingException;
  NamingEnumeration listBindings (String name) throws NamingException;

  void destroySubcontext (Name name) throws NamingException;
  void destroySubcontext (String name) throws NamingException;

  Context createSubcontext (Name name) throws NamingException;
  Context createSubcontext (String name) throws NamingException;

  Object lookupLink (Name name) throws NamingException;
  Object lookupLink (String name) throws NamingException;

  NameParser getNameParser (Name name) throws NamingException;
  NameParser getNameParser (String name) throws NamingException;

  Name composeName (Name name, Name prefix) throws NamingException;
  String composeName (String name, 
			     String prefix) throws NamingException;

  Object addToEnvironment (String propName, 
				  Object propVal) throws NamingException;

  Object removeFromEnvironment (String propName) throws NamingException;

  Hashtable getEnvironment () throws NamingException;

  void close () throws NamingException;

  String getNameInNamespace () throws NamingException;
}

