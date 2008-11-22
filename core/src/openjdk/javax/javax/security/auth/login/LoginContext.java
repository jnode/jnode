/*
 * Copyright 1998-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.security.auth.login;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.text.MessageFormat;
import javax.security.auth.Subject;
import javax.security.auth.AuthPermission;
import javax.security.auth.callback.*;
import java.security.AccessController;
import java.security.AccessControlContext;
import sun.security.util.PendingException;
import sun.security.util.ResourcesMgr;

/**
 * <p> The <code>LoginContext</code> class describes the basic methods used
 * to authenticate Subjects and provides a way to develop an
 * application independent of the underlying authentication technology.
 * A <code>Configuration</code> specifies the authentication technology, or
 * <code>LoginModule</code>, to be used with a particular application.
 * Different LoginModules can be plugged in under an application
 * without requiring any modifications to the application itself.
 *
 * <p> In addition to supporting <i>pluggable</i> authentication, this class
 * also supports the notion of <i>stacked</i> authentication.
 * Applications may be configured to use more than one
 * LoginModule.  For example, one could
 * configure both a Kerberos LoginModule and a smart card
 * LoginModule under an application.
 *
 * <p> A typical caller instantiates a LoginContext with
 * a <i>name</i> and a <code>CallbackHandler</code>.
 * LoginContext uses the <i>name</i> as the index into a
 * Configuration to determine which LoginModules should be used,
 * and which ones must succeed in order for the overall authentication to
 * succeed.  The <code>CallbackHandler</code> is passed to the underlying
 * LoginModules so they may communicate and interact with users
 * (prompting for a username and password via a graphical user interface,
 * for example).
 *
 * <p> Once the caller has instantiated a LoginContext,
 * it invokes the <code>login</code> method to authenticate
 * a <code>Subject</code>.  The <code>login</code> method invokes
 * the configured modules to perform their respective types of authentication
 * (username/password, smart card pin verification, etc.).
 * Note that the LoginModules will not attempt authentication retries nor
 * introduce delays if the authentication fails.
 * Such tasks belong to the LoginContext caller.
 *
 * <p> If the <code>login</code> method returns without
 * throwing an exception, then the overall authentication succeeded.
 * The caller can then retrieve
 * the newly authenticated Subject by invoking the
 * <code>getSubject</code> method.  Principals and Credentials associated
 * with the Subject may be retrieved by invoking the Subject's
 * respective <code>getPrincipals</code>, <code>getPublicCredentials</code>,
 * and <code>getPrivateCredentials</code> methods.
 *
 * <p> To logout the Subject, the caller calls
 * the <code>logout</code> method.  As with the <code>login</code>
 * method, this <code>logout</code> method invokes the <code>logout</code>
 * method for the configured modules.
 * 
 * <p> A LoginContext should not be used to authenticate
 * more than one Subject.  A separate LoginContext
 * should be used to authenticate each different Subject.
 *
 * <p> The following documentation applies to all LoginContext constructors:
 * <ol>
 *
 * <li> <code>Subject</code>
 * <ul>
 * <li> If the constructor has a Subject
 * input parameter, the LoginContext uses the caller-specified
 * Subject object.
 * <p>
 * <li> If the caller specifies a <code>null</code> Subject
 * and a <code>null</code> value is permitted,
 * the LoginContext instantiates a new Subject.
 * <p>
 * <li> If the constructor does <b>not</b> have a Subject
 * input parameter, the LoginContext instantiates a new Subject.
 * <p>
 * </ul>
 *
 * <li> <code>Configuration</code> 
 * <ul> 
 * <li> If the constructor has a Configuration 
 * input parameter and the caller specifies a non-null Configuration, 
 * the LoginContext uses the caller-specified Configuration.
 * <p>
 * If the constructor does <b>not</b> have a Configuration
 * input parameter, or if the caller specifies a <code>null</code>
 * Configuration object, the constructor uses the following call to
 * get the installed Configuration:
 * <pre>
 *      config = Configuration.getConfiguration();
 * </pre>
 * For both cases,
 * the <i>name</i> argument given to the constructor is passed to the
 * <code>Configuration.getAppConfigurationEntry</code> method.
 * If the Configuration has no entries for the specified <i>name</i>,
 * then the <code>LoginContext</code> calls
 * <code>getAppConfigurationEntry</code> with the name, "<i>other</i>"
 * (the default entry name).  If there is no entry for "<i>other</i>",
 * then a <code>LoginException</code> is thrown.
 * <p>
 * <li> When LoginContext uses the installed Configuration, the caller
 * requires the createLoginContext.<em>name</em> and possibly
 * createLoginContext.other AuthPermissions. Furthermore, the
 * LoginContext will invoke configured modules from within an
 * <code>AccessController.doPrivileged</code> call so that modules that
 * perform security-sensitive tasks (such as connecting to remote hosts,
 * and updating the Subject) will require the respective permissions, but
 * the callers of the LoginContext will not require those permissions.
 * <p>
 * <li> When LoginContext uses a caller-specified Configuration, the caller
 * does not require any createLoginContext AuthPermission.  The LoginContext
 * saves the <code>AccessControlContext</code> for the caller,
 * and invokes the configured modules from within an
 * <tt>AccessController.doPrivileged</tt> call constrained by that context.
 * This means the caller context (stored when the LoginContext was created)
 * must have sufficient permissions to perform any security-sensitive tasks
 * that the modules may perform.
 * <p>
 * </ul>
 *
 * <li> <code>CallbackHandler</code>
 * <ul>
 * <li> If the constructor has a CallbackHandler
 * input parameter, the LoginContext uses the caller-specified
 * CallbackHandler object.
 * <p>
 * <li> If the constructor does <b>not</b> have a CallbackHandler
 * input parameter, or if the caller specifies a <code>null</code>
 * CallbackHandler object (and a <code>null</code> value is permitted),
 * the LoginContext queries the
 * <i>auth.login.defaultCallbackHandler</i> security property
 * for the fully qualified class name of a default handler implementation.
 * If the security property is not set,
 * then the underlying modules will not have a
 * CallbackHandler for use in communicating
 * with users.  The caller thus assumes that the configured
 * modules have alternative means for authenticating the user.
 *
 * <p>
 * <li> When the LoginContext uses the installed Configuration (instead of
 * a caller-specified Configuration, see above),
 * then this LoginContext must wrap any
 * caller-specified or default CallbackHandler implementation
 * in a new CallbackHandler implementation
 * whose <code>handle</code> method implementation invokes the
 * specified CallbackHandler's <code>handle</code> method in a
 * <code>java.security.AccessController.doPrivileged</code> call
 * constrained by the caller's current <code>AccessControlContext</code>.
 * </ul>
 * </ol>
 *
 * <p> Note that Security Properties
 * (such as <code>auth.login.defaultCallbackHandler</code>)
 * can be set programmatically via the
 * <code>java.security.Security</code> class,
 * or statically in the Java security properties file located in the
 * file named &lt;JAVA_HOME&gt;/lib/security/java.security.
 * &lt;JAVA_HOME&gt; refers to the value of the java.home system property,
 * and specifies the directory where the JRE is installed.
 * 
 * @see java.security.Security
 * @see javax.security.auth.AuthPermission
 * @see javax.security.auth.Subject
 * @see javax.security.auth.callback.CallbackHandler
 * @see javax.security.auth.login.Configuration
 * @see javax.security.auth.spi.LoginModule
 */
public class LoginContext {

    private static final String INIT_METHOD		= "initialize";
    private static final String LOGIN_METHOD		= "login";
    private static final String COMMIT_METHOD		= "commit";
    private static final String ABORT_METHOD		= "abort";
    private static final String LOGOUT_METHOD		= "logout";
    private static final String OTHER			= "other";
    private static final String DEFAULT_HANDLER		=
				"auth.login.defaultCallbackHandler";
    private Subject subject = null;
    private boolean subjectProvided = false;
    private boolean loginSucceeded = false;
    private CallbackHandler callbackHandler;
    private Map state = new HashMap();

    private Configuration config;
    private boolean configProvided = false;
    private AccessControlContext creatorAcc = null;
    private ModuleInfo[] moduleStack;
    private ClassLoader contextClassLoader = null;
    private static final Class[] PARAMS = { };

    // state saved in the event a user-specified asynchronous exception
    // was specified and thrown

    private int moduleIndex = 0;
    private LoginException firstError = null;
    private LoginException firstRequiredError = null;
    private boolean success = false;

    private static final sun.security.util.Debug debug =
	sun.security.util.Debug.getInstance("logincontext", "\t[LoginContext]");

    private void init(String name) throws LoginException {

	SecurityManager sm = System.getSecurityManager();
	if (sm != null && !configProvided) {
	    sm.checkPermission(new AuthPermission
				("createLoginContext." + name));
	}

	if (name == null)
	    throw new LoginException
		(ResourcesMgr.getString("Invalid null input: name"));

	// get the Configuration
	if (config == null) {
	    config = java.security.AccessController.doPrivileged
		(new java.security.PrivilegedAction<Configuration>() {
		public Configuration run() {	
		    return Configuration.getConfiguration();
		}
	    });
	}

	// get the LoginModules configured for this application
	AppConfigurationEntry[] entries = config.getAppConfigurationEntry(name);
	if (entries == null) {

	    if (sm != null && !configProvided) {
		sm.checkPermission(new AuthPermission
				("createLoginContext." + OTHER));
	    }

	    entries = config.getAppConfigurationEntry(OTHER);
	    if (entries == null) {
		MessageFormat form = new MessageFormat(ResourcesMgr.getString
			("No LoginModules configured for name"));
		Object[] source = {name};
		throw new LoginException(form.format(source));
	    }
	}
	moduleStack = new ModuleInfo[entries.length];
	for (int i = 0; i < entries.length; i++) {
	    // clone returned array
	    moduleStack[i] = new ModuleInfo
				(new AppConfigurationEntry
					(entries[i].getLoginModuleName(),
					entries[i].getControlFlag(),
					entries[i].getOptions()),
				null);
	}

	contextClassLoader = java.security.AccessController.doPrivileged
		(new java.security.PrivilegedAction<ClassLoader>() {
		public ClassLoader run() {	
		    return Thread.currentThread().getContextClassLoader();
		}
	});
    }

    private void loadDefaultCallbackHandler() throws LoginException {

	// get the default handler class
	try {

	    final ClassLoader finalLoader = contextClassLoader;

	    this.callbackHandler = java.security.AccessController.doPrivileged(
		new java.security.PrivilegedExceptionAction<CallbackHandler>() {
		public CallbackHandler run() throws Exception {
		    String defaultHandler = java.security.Security.getProperty
			(DEFAULT_HANDLER);
		    if (defaultHandler == null || defaultHandler.length() == 0)
			return null;
		    Class c = Class.forName(defaultHandler,
					true,
					finalLoader);
		    return (CallbackHandler)c.newInstance();
		}
	    });
	} catch (java.security.PrivilegedActionException pae) {
	    throw new LoginException(pae.getException().toString());
	}

	// secure it with the caller's ACC
	if (this.callbackHandler != null && !configProvided) {
	    this.callbackHandler = new SecureCallbackHandler
				(java.security.AccessController.getContext(),
				this.callbackHandler);
	}
    }

    /**
     * Instantiate a new <code>LoginContext</code> object with a name.
     *
     * @param name the name used as the index into the
     *		<code>Configuration</code>.
     *
     * @exception LoginException if the caller-specified <code>name</code>
     *		does not appear in the <code>Configuration</code>
     *		and there is no <code>Configuration</code> entry
     *		for "<i>other</i>", or if the
     *		<i>auth.login.defaultCallbackHandler</i>
     *		security property was set, but the implementation
     *		class could not be loaded.
     *		<p>
     * @exception SecurityException if a SecurityManager is set and
     *		the caller does not have
     *		AuthPermission("createLoginContext.<i>name</i>"),
     *		or if a configuration entry for <i>name</i> does not exist and
     *		the caller does not additionally have
     *		AuthPermission("createLoginContext.other")
     */
    public LoginContext(String name) throws LoginException {
	init(name);
	loadDefaultCallbackHandler();
    }

    /**
     * Instantiate a new <code>LoginContext</code> object with a name
     * and a <code>Subject</code> object.
     *
     * <p>
     *
     * @param name the name used as the index into the
     *		<code>Configuration</code>. <p>
     *
     * @param subject the <code>Subject</code> to authenticate.
     *
     * @exception LoginException if the caller-specified <code>name</code>
     *		does not appear in the <code>Configuration</code>
     *          and there is no <code>Configuration</code> entry
     *          for "<i>other</i>", if the caller-specified <code>subject</code>
     *		is <code>null</code>, or if the
     *		<i>auth.login.defaultCallbackHandler</i>
     *		security property was set, but the implementation
     *		class could not be loaded.
     *		<p>
     * @exception SecurityException if a SecurityManager is set and
     *		the caller does not have
     *		AuthPermission("createLoginContext.<i>name</i>"),
     *		or if a configuration entry for <i>name</i> does not exist and
     *		the caller does not additionally have
     *		AuthPermission("createLoginContext.other")
     */
    public LoginContext(String name, Subject subject)
    throws LoginException {
	init(name);
	if (subject == null)
	    throw new LoginException
		(ResourcesMgr.getString("invalid null Subject provided"));
	this.subject = subject;
	subjectProvided = true;
	loadDefaultCallbackHandler();
    }

    /**
     * Instantiate a new <code>LoginContext</code> object with a name
     * and a <code>CallbackHandler</code> object.
     *
     * <p>
     *
     * @param name the name used as the index into the
     *		<code>Configuration</code>. <p>
     *
     * @param callbackHandler the <code>CallbackHandler</code> object used by
     *		LoginModules to communicate with the user.
     *
     * @exception LoginException if the caller-specified <code>name</code>
     *          does not appear in the <code>Configuration</code>
     *          and there is no <code>Configuration</code> entry
     *          for "<i>other</i>", or if the caller-specified
     *		<code>callbackHandler</code> is <code>null</code>.
     *		<p>
     * @exception SecurityException if a SecurityManager is set and
     *		the caller does not have
     *		AuthPermission("createLoginContext.<i>name</i>"),
     *		or if a configuration entry for <i>name</i> does not exist and
     *		the caller does not additionally have
     *		AuthPermission("createLoginContext.other")
     */
    public LoginContext(String name, CallbackHandler callbackHandler)
    throws LoginException {
	init(name);
	if (callbackHandler == null)
	    throw new LoginException(ResourcesMgr.getString
				("invalid null CallbackHandler provided"));
	this.callbackHandler = new SecureCallbackHandler
				(java.security.AccessController.getContext(),
				callbackHandler);
    }

    /**
     * Instantiate a new <code>LoginContext</code> object with a name,
     * a <code>Subject</code> to be authenticated, and a
     * <code>CallbackHandler</code> object.
     *
     * <p>
     *
     * @param name the name used as the index into the
     *		<code>Configuration</code>. <p>
     *
     * @param subject the <code>Subject</code> to authenticate. <p>
     *
     * @param callbackHandler the <code>CallbackHandler</code> object used by
     *		LoginModules to communicate with the user.
     *
     * @exception LoginException if the caller-specified <code>name</code>
     *          does not appear in the <code>Configuration</code>
     *          and there is no <code>Configuration</code> entry
     *          for "<i>other</i>", or if the caller-specified
     *		<code>subject</code> is <code>null</code>,
     *		or if the caller-specified
     *		<code>callbackHandler</code> is <code>null</code>.
     *		<p>
     * @exception SecurityException if a SecurityManager is set and
     *		the caller does not have
     *		AuthPermission("createLoginContext.<i>name</i>"),
     *		or if a configuration entry for <i>name</i> does not exist and
     *		the caller does not additionally have
     *		AuthPermission("createLoginContext.other")
     */
    public LoginContext(String name, Subject subject,
			CallbackHandler callbackHandler) throws LoginException {
	this(name, subject);
	if (callbackHandler == null)
	    throw new LoginException(ResourcesMgr.getString
				("invalid null CallbackHandler provided"));
	this.callbackHandler = new SecureCallbackHandler
				(java.security.AccessController.getContext(),
				callbackHandler);
    }

    /**
     * Instantiate a new <code>LoginContext</code> object with a name,
     * a <code>Subject</code> to be authenticated,
     * a <code>CallbackHandler</code> object, and a login
     * <code>Configuration</code>.
     *
     * <p>
     *
     * @param name the name used as the index into the caller-specified
     *          <code>Configuration</code>. <p>
     *
     * @param subject the <code>Subject</code> to authenticate,
     *          or <code>null</code>. <p>
     *
     * @param callbackHandler the <code>CallbackHandler</code> object used by
     *          LoginModules to communicate with the user, or <code>null</code>.
     *		<p>
     *
     * @param config the <code>Configuration</code> that lists the
     *          login modules to be called to perform the authentication,
     *          or <code>null</code>.
     *
     * @exception LoginException if the caller-specified <code>name</code>
     *          does not appear in the <code>Configuration</code>
     *          and there is no <code>Configuration</code> entry
     *          for "<i>other</i>".
     *		<p>
     * @exception SecurityException if a SecurityManager is set,
     *		<i>config</i> is <code>null</code>,
     *		and either the caller does not have
     *		AuthPermission("createLoginContext.<i>name</i>"),
     *		or if a configuration entry for <i>name</i> does not exist and
     *		the caller does not additionally have
     *		AuthPermission("createLoginContext.other")
     *
     * @since 1.5
     */
    public LoginContext(String name, Subject subject,
                        CallbackHandler callbackHandler,
                        Configuration config) throws LoginException {
	this.config = config;
	configProvided = (config != null) ? true : false;
	if (configProvided) {
	    creatorAcc = java.security.AccessController.getContext();
	}
	
	init(name);
	if (subject != null) {
	    this.subject = subject;
	    subjectProvided = true;
	}
	if (callbackHandler == null) {
	    loadDefaultCallbackHandler();
	} else if (!configProvided) {
	    this.callbackHandler = new SecureCallbackHandler
				(java.security.AccessController.getContext(),
				callbackHandler);
	} else {
	    this.callbackHandler = callbackHandler;
	}
    }

    /**
     * Perform the authentication.
     *
     * <p> This method invokes the <code>login</code> method for each
     * LoginModule configured for the <i>name</i> specified to the
     * <code>LoginContext</code> constructor, as determined by the login
     * <code>Configuration</code>.  Each <code>LoginModule</code>
     * then performs its respective type of authentication
     * (username/password, smart card pin verification, etc.).
     *
     * <p> This method completes a 2-phase authentication process by
     * calling each configured LoginModule's <code>commit</code> method
     * if the overall authentication succeeded (the relevant REQUIRED,
     * REQUISITE, SUFFICIENT, and OPTIONAL LoginModules succeeded),
     * or by calling each configured LoginModule's <code>abort</code> method
     * if the overall authentication failed.  If authentication succeeded,
     * each successful LoginModule's <code>commit</code> method associates
     * the relevant Principals and Credentials with the <code>Subject</code>.
     * If authentication failed, each LoginModule's <code>abort</code> method
     * removes/destroys any previously stored state.
     *
     * <p> If the <code>commit</code> phase of the authentication process
     * fails, then the overall authentication fails and this method
     * invokes the <code>abort</code> method for each configured
     * <code>LoginModule</code>.
     *
     * <p> If the <code>abort</code> phase
     * fails for any reason, then this method propagates the
     * original exception thrown either during the <code>login</code> phase
     * or the <code>commit</code> phase.  In either case, the overall
     * authentication fails.
     *
     * <p> In the case where multiple LoginModules fail,
     * this method propagates the exception raised by the first
     * <code>LoginModule</code> which failed.
     *
     * <p> Note that if this method enters the <code>abort</code> phase
     * (either the <code>login</code> or <code>commit</code> phase failed),
     * this method invokes all LoginModules configured for the
     * application regardless of their respective <code>Configuration</code>
     * flag parameters.  Essentially this means that <code>Requisite</code>
     * and <code>Sufficient</code> semantics are ignored during the
     * <code>abort</code> phase.  This guarantees that proper cleanup
     * and state restoration can take place.
     * 
     * <p>
     *
     * @exception LoginException if the authentication fails.
     */
    public void login() throws LoginException {

	loginSucceeded = false;

	if (subject == null) {
	    subject = new Subject();
	}

	try {
	    if (configProvided) {
		// module invoked in doPrivileged with creatorAcc
		invokeCreatorPriv(LOGIN_METHOD);
		invokeCreatorPriv(COMMIT_METHOD);
	    } else {
		// module invoked in doPrivileged
		invokePriv(LOGIN_METHOD);
		invokePriv(COMMIT_METHOD);
	    }
	    loginSucceeded = true;
	} catch (LoginException le) {
	    try {
		if (configProvided) {
		    invokeCreatorPriv(ABORT_METHOD);
		} else {
		    invokePriv(ABORT_METHOD);
		}
	    } catch (LoginException le2) {
		throw le;
	    }
	    throw le;
	}
    }

    /**
     * Logout the <code>Subject</code>.
     *
     * <p> This method invokes the <code>logout</code> method for each
     * <code>LoginModule</code> configured for this <code>LoginContext</code>.
     * Each <code>LoginModule</code> performs its respective logout procedure
     * which may include removing/destroying
     * <code>Principal</code> and <code>Credential</code> information
     * from the <code>Subject</code> and state cleanup.
     *
     * <p> Note that this method invokes all LoginModules configured for the
     * application regardless of their respective
     * <code>Configuration</code> flag parameters.  Essentially this means
     * that <code>Requisite</code> and <code>Sufficient</code> semantics are
     * ignored for this method.  This guarantees that proper cleanup
     * and state restoration can take place.
     * 
     * <p>
     *
     * @exception LoginException if the logout fails.
     */
    public void logout() throws LoginException {
	if (subject == null) {
	    throw new LoginException(ResourcesMgr.getString
		("null subject - logout called before login"));
	}

	if (configProvided) {
	    // module invoked in doPrivileged with creatorAcc
	    invokeCreatorPriv(LOGOUT_METHOD);
	} else {
	    // module invoked in doPrivileged
	    invokePriv(LOGOUT_METHOD);
	}
    }

    /**
     * Return the authenticated Subject.
     *
     * <p>
     *
     * @return the authenticated Subject.  If the caller specified a
     *		Subject to this LoginContext's constructor,
     *		this method returns the caller-specified Subject.
     *		If a Subject was not specified and authentication succeeds,
     *		this method returns the Subject instantiated and used for
     *		authentication by this LoginContext.
     *		If a Subject was not specified, and authentication fails or
     *		has not been attempted, this method returns null.
     */
    public Subject getSubject() {
	if (!loginSucceeded && !subjectProvided)
	    return null;
	return subject;
    }

    private void clearState() {
	moduleIndex = 0;
	firstError = null;
	firstRequiredError = null;
	success = false;
    }

    private void throwException(LoginException originalError, LoginException le)
    throws LoginException {

	// first clear state
	clearState();
	
	// throw the exception
	LoginException error = (originalError != null) ? originalError : le;
	throw error;
    }

    /**
     * Invokes the login, commit, and logout methods
     * from a LoginModule inside a doPrivileged block.
     *
     * This version is called if the caller did not instantiate
     * the LoginContext with a Configuration object.
     */
    private void invokePriv(final String methodName) throws LoginException {
	try {
	    java.security.AccessController.doPrivileged
		(new java.security.PrivilegedExceptionAction<Void>() {
		public Void run() throws LoginException {
		    invoke(methodName);
		    return null;
		}
	    });
	} catch (java.security.PrivilegedActionException pae) {
	    throw (LoginException)pae.getException();
	}
    }

    /**
     * Invokes the login, commit, and logout methods
     * from a LoginModule inside a doPrivileged block restricted
     * by creatorAcc
     *
     * This version is called if the caller instantiated
     * the LoginContext with a Configuration object.
     */
    private void invokeCreatorPriv(final String methodName)
		throws LoginException {
	try {
	    java.security.AccessController.doPrivileged
		(new java.security.PrivilegedExceptionAction<Void>() {
		public Void run() throws LoginException {
		    invoke(methodName);
		    return null;
		}
	    }, creatorAcc);
	} catch (java.security.PrivilegedActionException pae) {
	    throw (LoginException)pae.getException();
	}
    }

    private void invoke(String methodName) throws LoginException {

	// start at moduleIndex
	// - this can only be non-zero if methodName is LOGIN_METHOD

	for (int i = moduleIndex; i < moduleStack.length; i++, moduleIndex++) {
	    try {

		int mIndex = 0;
		Method[] methods = null;

		if (moduleStack[i].module != null) {
		    methods = moduleStack[i].module.getClass().getMethods();
		} else {

		    // instantiate the LoginModule
		    Class c = Class.forName
				(moduleStack[i].entry.getLoginModuleName(),
				true,
				contextClassLoader);

		    Constructor constructor = c.getConstructor(PARAMS);
		    Object[] args = { };

		    // allow any object to be a LoginModule
		    // as long as it conforms to the interface
		    moduleStack[i].module = constructor.newInstance(args);

		    methods = moduleStack[i].module.getClass().getMethods();

		    // call the LoginModule's initialize method
		    for (mIndex = 0; mIndex < methods.length; mIndex++) {
			if (methods[mIndex].getName().equals(INIT_METHOD))
			    break;
		    }

		    Object[] initArgs = {subject,
					callbackHandler,
					state,
					moduleStack[i].entry.getOptions() };
		    // invoke the LoginModule initialize method
		    methods[mIndex].invoke(moduleStack[i].module, initArgs);
		}

		// find the requested method in the LoginModule
		for (mIndex = 0; mIndex < methods.length; mIndex++) {
		    if (methods[mIndex].getName().equals(methodName))
			break;
		}

		// set up the arguments to be passed to the LoginModule method
		Object[] args = { };

		// invoke the LoginModule method
		boolean status = ((Boolean)methods[mIndex].invoke
				(moduleStack[i].module, args)).booleanValue();

		if (status == true) {

		    // if SUFFICIENT, return if no prior REQUIRED errors
		    if (!methodName.equals(ABORT_METHOD) &&
		        !methodName.equals(LOGOUT_METHOD) &&
			moduleStack[i].entry.getControlFlag() ==
		    AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT &&
			firstRequiredError == null) {

			// clear state
			clearState();

			if (debug != null)
			    debug.println(methodName + " SUFFICIENT success");
			return;
		    }

		    if (debug != null)
			debug.println(methodName + " success");
		    success = true;
		} else {
		    if (debug != null)
			debug.println(methodName + " ignored");
		}

	    } catch (NoSuchMethodException nsme) {
		MessageFormat form = new MessageFormat(ResourcesMgr.getString
			("unable to instantiate LoginModule, module, because " +
			"it does not provide a no-argument constructor"));
		Object[] source = {moduleStack[i].entry.getLoginModuleName()};
		throwException(null, new LoginException(form.format(source)));
	    } catch (InstantiationException ie) {
		throwException(null, new LoginException(ResourcesMgr.getString
			("unable to instantiate LoginModule: ") +
			ie.getMessage()));
	    } catch (ClassNotFoundException cnfe) {
		throwException(null, new LoginException(ResourcesMgr.getString
			("unable to find LoginModule class: ") +
			cnfe.getMessage()));
	    } catch (IllegalAccessException iae) {
		throwException(null, new LoginException(ResourcesMgr.getString
			("unable to access LoginModule: ") +
			iae.getMessage()));
	    } catch (InvocationTargetException ite) {

		// failure cases

		LoginException le;

		if (ite.getCause() instanceof PendingException &&
		    methodName.equals(LOGIN_METHOD)) {
		    
		    // XXX
		    //
		    // if a module's LOGIN_METHOD threw a PendingException
		    // then immediately throw it.
		    //
		    // when LoginContext is called again,
		    // the module that threw the exception is invoked first
		    // (the module list is not invoked from the start).
		    // previously thrown exception state is still present.
		    //
		    // it is assumed that the module which threw
		    // the exception can have its
		    // LOGIN_METHOD invoked twice in a row
		    // without any commit/abort in between.
		    //
		    // in all cases when LoginContext returns
		    // (either via natural return or by throwing an exception)
		    // we need to call clearState before returning.
		    // the only time that is not true is in this case -
		    // do not call throwException here.

		    throw (PendingException)ite.getCause();

		} else if (ite.getCause() instanceof LoginException) {

		    le = (LoginException)ite.getCause();

		} else if (ite.getCause() instanceof SecurityException) {

		    // do not want privacy leak
		    // (e.g., sensitive file path in exception msg)

		    le = new LoginException("Security Exception");
		    le.initCause(new SecurityException());
		    if (debug != null) {
			debug.println
			    ("original security exception with detail msg " +
			    "replaced by new exception with empty detail msg");
			debug.println("original security exception: " +
				ite.getCause().toString());
		    }
		} else {

		    // capture an unexpected LoginModule exception
		    java.io.StringWriter sw = new java.io.StringWriter();
		    ite.getCause().printStackTrace
						(new java.io.PrintWriter(sw));
		    sw.flush();
		    le = new LoginException(sw.toString());
		}

		if (moduleStack[i].entry.getControlFlag() ==
		    AppConfigurationEntry.LoginModuleControlFlag.REQUISITE) {

		    if (debug != null)
			debug.println(methodName + " REQUISITE failure");

		    // if REQUISITE, then immediately throw an exception
		    if (methodName.equals(ABORT_METHOD) ||
		        methodName.equals(LOGOUT_METHOD)) {
			if (firstRequiredError == null)
			    firstRequiredError = le;
		    } else {
			throwException(firstRequiredError, le);
		    }

		} else if (moduleStack[i].entry.getControlFlag() ==
		    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED) {

		    if (debug != null)
			debug.println(methodName + " REQUIRED failure");

		    // mark down that a REQUIRED module failed
		    if (firstRequiredError == null)
			firstRequiredError = le;

		} else {

		    if (debug != null)
			debug.println(methodName + " OPTIONAL failure");

		    // mark down that an OPTIONAL module failed
		    if (firstError == null)
			firstError = le;
		}
	    }
	}

	// we went thru all the LoginModules.
	if (firstRequiredError != null) {
	    // a REQUIRED module failed -- return the error
	    throwException(firstRequiredError, null);
	} else if (success == false && firstError != null) {
	    // no module succeeded -- return the first error
	    throwException(firstError, null);
	} else if (success == false) {
	    // no module succeeded -- all modules were IGNORED
	    throwException(new LoginException
		(ResourcesMgr.getString("Login Failure: all modules ignored")),
		null);
	} else {
	    // success

	    clearState();
	    return;
	}
    }

    /**
     * Wrap the caller-specified CallbackHandler in our own
     * and invoke it within a privileged block, constrained by
     * the caller's AccessControlContext.
     */
    private static class SecureCallbackHandler implements CallbackHandler {

	private final java.security.AccessControlContext acc;
	private final CallbackHandler ch;

	SecureCallbackHandler(java.security.AccessControlContext acc,
			CallbackHandler ch) {
	    this.acc = acc;
	    this.ch = ch;
	}

	public void handle(final Callback[] callbacks)
		throws java.io.IOException, UnsupportedCallbackException {
	    try {
		java.security.AccessController.doPrivileged
		    (new java.security.PrivilegedExceptionAction<Void>() {
		    public Void run() throws java.io.IOException,
					UnsupportedCallbackException {	
			ch.handle(callbacks);
			return null;
		    }
		}, acc);
	    } catch (java.security.PrivilegedActionException pae) {
		if (pae.getException() instanceof java.io.IOException) {
		    throw (java.io.IOException)pae.getException();
		} else {
		    throw (UnsupportedCallbackException)pae.getException();
		}
	    }
	}
    }

    /**
     * LoginModule information -
     *		incapsulates Configuration info and actual module instances
     */
    private static class ModuleInfo {
	AppConfigurationEntry entry;
	Object module;

	ModuleInfo(AppConfigurationEntry newEntry, Object newModule) {
	    this.entry = newEntry;
	    this.module = newModule;
	}
    }
}
