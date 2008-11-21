/*
 * Copyright 2002-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.corba.se.impl.orb ;

import java.applet.Applet;

import java.io.IOException ;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field ;
import java.lang.reflect.Modifier ;
import java.lang.reflect.InvocationTargetException ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.Properties ;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.LinkedList ;
import java.util.Collection ;
import java.util.Collections ;
import java.util.StringTokenizer ;
import java.util.Enumeration ;
import java.util.WeakHashMap ;

import java.net.InetAddress ;

import java.security.PrivilegedAction;
import java.security.AccessController ;

import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.ValueHandler;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.Environment;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.NVList;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Request;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.UnionMember;
import org.omg.CORBA.ValueMember;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.MARSHAL;

import org.omg.CORBA.portable.ValueFactory;

import org.omg.CORBA.ORBPackage.InvalidName;

import com.sun.org.omg.SendingContext.CodeBase;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.protocol.ClientInvocationInfo ;
import com.sun.corba.se.pept.transport.ContactInfo;
import com.sun.corba.se.pept.transport.ConnectionCache;
import com.sun.corba.se.pept.transport.TransportManager;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.se.spi.ior.TaggedComponentFactoryFinder;
import com.sun.corba.se.spi.ior.IORFactories ;
import com.sun.corba.se.spi.ior.ObjectKey ;
import com.sun.corba.se.spi.ior.ObjectKeyFactory ;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.oa.OAInvocationInfo;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory;
import com.sun.corba.se.spi.orb.DataCollector;
import com.sun.corba.se.spi.orb.Operation;
import com.sun.corba.se.spi.orb.ORBData;
import com.sun.corba.se.spi.orb.ORBConfigurator;
import com.sun.corba.se.spi.orb.ParserImplBase;
import com.sun.corba.se.spi.orb.PropertyParser;
import com.sun.corba.se.spi.orb.OperationFactory;
import com.sun.corba.se.spi.orb.ORBVersion;
import com.sun.corba.se.spi.orb.ORBVersionFactory;
import com.sun.corba.se.spi.orbutil.closure.ClosureFactory;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager;
import com.sun.corba.se.spi.protocol.ClientDelegateFactory;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;
import com.sun.corba.se.spi.protocol.RequestDispatcherDefault;
import com.sun.corba.se.spi.protocol.PIHandler;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.protocol.ForwardException;
import com.sun.corba.se.spi.resolver.Resolver;
import com.sun.corba.se.spi.resolver.LocalResolver;
import com.sun.corba.se.spi.orb.StringPair;
import com.sun.corba.se.spi.orb.StringPair;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketManager;
import com.sun.corba.se.spi.copyobject.CopierManager ;
import com.sun.corba.se.spi.presentation.rmi.PresentationDefaults ;
import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.se.spi.servicecontext.ServiceContextRegistry;

import com.sun.corba.se.impl.corba.TypeCodeFactory;
import com.sun.corba.se.impl.corba.TypeCodeImpl;
import com.sun.corba.se.impl.corba.NVListImpl;
import com.sun.corba.se.impl.corba.ExceptionListImpl;
import com.sun.corba.se.impl.corba.ContextListImpl;
import com.sun.corba.se.impl.corba.NamedValueImpl;
import com.sun.corba.se.impl.corba.EnvironmentImpl;
import com.sun.corba.se.impl.corba.AsynchInvoke;
import com.sun.corba.se.impl.corba.AnyImpl;
import com.sun.corba.se.impl.corba.RequestImpl;
import com.sun.corba.se.impl.dynamicany.DynAnyFactoryImpl;
import com.sun.corba.se.impl.encoding.EncapsOutputStream;
import com.sun.corba.se.impl.interceptors.PIHandlerImpl;
import com.sun.corba.se.impl.interceptors.PINoOpHandlerImpl;
import com.sun.corba.se.impl.ior.TaggedComponentFactoryFinderImpl;
import com.sun.corba.se.impl.ior.TaggedProfileFactoryFinderImpl;
import com.sun.corba.se.impl.ior.TaggedProfileTemplateFactoryFinderImpl;
import com.sun.corba.se.impl.oa.toa.TOAFactory;
import com.sun.corba.se.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.se.impl.oa.poa.DelegateImpl;
import com.sun.corba.se.impl.oa.poa.POAFactory;
import com.sun.corba.se.impl.orbutil.ORBClassLoader;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.orbutil.StackImpl;
import com.sun.corba.se.impl.orbutil.threadpool.ThreadPoolImpl;
import com.sun.corba.se.impl.orbutil.threadpool.ThreadPoolManagerImpl;
import com.sun.corba.se.impl.protocol.RequestDispatcherRegistryImpl;
import com.sun.corba.se.impl.protocol.CorbaInvocationInfo;
import com.sun.corba.se.impl.transport.CorbaTransportManagerImpl;
import com.sun.corba.se.impl.legacy.connection.LegacyServerSocketManagerImpl;
import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.copyobject.CopierManagerImpl;
import com.sun.corba.se.impl.presentation.rmi.PresentationManagerImpl;
             
/**
 * The JavaIDL ORB implementation.
 */
public class ORBImpl extends com.sun.corba.se.spi.orb.ORB
{
    protected TransportManager transportManager;
    protected LegacyServerSocketManager legacyServerSocketManager;

    private ThreadLocal OAInvocationInfoStack ; 

    private ThreadLocal clientInvocationInfoStack ; 

    // pure java orb, caching the servant IOR per ORB
    private static IOR codeBaseIOR ;

    // Vector holding deferred Requests
    private Vector	      dynamicRequests ; 
    private SynchVariable     svResponseReceived ;

    private java.lang.Object runObj = new java.lang.Object();
    private java.lang.Object shutdownObj = new java.lang.Object();
    private static final byte STATUS_OPERATING = 1;
    private static final byte STATUS_SHUTTING_DOWN = 2;
    private static final byte STATUS_SHUTDOWN = 3;
    private static final byte STATUS_DESTROYED = 4;
    private byte status = STATUS_OPERATING;

    // XXX Should we move invocation tracking to the first level server dispatcher?
    private java.lang.Object invocationObj = new java.lang.Object();

    // thread local variable to store a boolean to detect deadlock in 
    // ORB.shutdown(true).
    private ThreadLocal isProcessingInvocation = new ThreadLocal () {
        protected java.lang.Object initialValue() {
            return Boolean.FALSE;
        }
    };

    // This map is caching TypeCodes created for a certain class (key)
    // and is used in Util.writeAny()
    private Map typeCodeForClassMap ;

    // Cache to hold ValueFactories (Helper classes) keyed on repository ids
    private Hashtable valueFactoryCache = new Hashtable();

    // thread local variable to store the current ORB version.
    // default ORB version is the version of ORB with correct Rep-id
    // changes
    private ThreadLocal orbVersionThreadLocal ; 

    private RequestDispatcherRegistry requestDispatcherRegistry ;

    private CopierManager copierManager ;

    private int transientServerId ;

    private ThreadGroup threadGroup ; 

    private ServiceContextRegistry serviceContextRegistry ;

    // Needed here to implement connect/disconnect
    private TOAFactory toaFactory ;

    // Needed here for set_delegate
    private POAFactory poaFactory ;

    // The interceptor handler, which provides portable interceptor services for
    // subcontracts and object adapters.
    private PIHandler pihandler ;

    private ORBData configData ;

    private BadServerIdHandler badServerIdHandler ;

    private ClientDelegateFactory clientDelegateFactory ;

    private CorbaContactInfoListFactory corbaContactInfoListFactory ;

    // All access to resolver, localResolver, and urlOperation must be protected using
    // resolverLock.  Do not hold the ORBImpl lock while accessing
    // resolver, or deadlocks may occur.
    private Object resolverLock ;

    // Used for resolver_initial_references and list_initial_services
    private Resolver resolver ;

    // Used for register_initial_references
    private LocalResolver localResolver ;

    // Converts strings to object references for resolvers and string_to_object
    private Operation urlOperation ;

    private CorbaServerRequestDispatcher insNamingDelegate ;

    private TaggedComponentFactoryFinder taggedComponentFactoryFinder ;

    private IdentifiableFactoryFinder taggedProfileFactoryFinder ;

    private IdentifiableFactoryFinder taggedProfileTemplateFactoryFinder ;

    private ObjectKeyFactory objectKeyFactory ;

    private ThreadPoolManager threadpoolMgr;

    private void dprint( String msg )
    {
        ORBUtility.dprint( this, msg ) ;
    }

    ////////////////////////////////////////////////////
    //
    // NOTE:
    //
    // Methods that are synchronized MUST stay synchronized.
    //
    // Methods that are NOT synchronized must stay that way to avoid deadlock.
    //
    //
    // REVISIT:
    //
    // checkShutDownState - lock on different object - and normalize usage.
    // starting/FinishDispatch and Shutdown
    // 

    public ORBData getORBData() 
    {
	return configData ;
    }
 
    public PIHandler getPIHandler()
    {
	return pihandler ;
    }

    /**
     * Create a new ORB. Should be followed by the appropriate
     * set_parameters() call.
     */
    public ORBImpl()
    {
	// All initialization is done through set_parameters().
    }

    public ORBVersion getORBVersion()
    {
        return (ORBVersion)(orbVersionThreadLocal.get()) ;
    }

    public void setORBVersion(ORBVersion verObj)
    {
        orbVersionThreadLocal.set(verObj);
    }

/****************************************************************************
 * The following methods are ORB initialization
 ****************************************************************************/

    // preInit initializes all non-pluggable ORB data that is independent
    // of the property parsing.
    private void preInit( String[] params, Properties props )
    {
        // Before ORBConfiguration we need to set a PINoOpHandlerImpl,
        // because PersisentServer Initialization inside configurator will
        // invoke orb.resolve_initial_references( ) which will result in a 
        // check on piHandler to invoke Interceptors. We do not want any
        // Interceptors to be invoked before the complete ORB initialization.
        // piHandler will be replaced by a real PIHandler implementation at the
        // end of this method.
	pihandler = new PINoOpHandlerImpl( );

	// See bugs 4916766 and 4936203
	// We intend to create new threads in a reliable thread group.
	// This avoids problems if the application/applet
	// creates a thread group, makes JavaIDL calls which create a new
	// connection and ReaderThread, and then destroys the thread
	// group. If our ReaderThreads were to be part of such destroyed thread
	// group then it might get killed and cause other invoking threads
	// sharing the same connection to get a non-restartable
	// CommunicationFailure. We'd like to avoid that.
	//
	// Our solution is to create all of our threads in the highest thread
	// group that we have access to, given our own security clearance.
	//
        try { 
	    // try to get a thread group that's as high in the threadgroup  
	    // parent-child hierarchy, as we can get to.
	    // this will prevent an ORB thread created during applet-init from 
	    // being killed when an applet dies.
	    threadGroup = (ThreadGroup) AccessController.doPrivileged( 
		new PrivilegedAction() { 
		    public Object run() { 
			ThreadGroup tg = Thread.currentThread().getThreadGroup() ;  
			ThreadGroup ptg = tg ; 
			try { 
			    while (ptg != null) { 
				tg = ptg;  
				ptg = tg.getParent(); 
			    } 
			} catch (SecurityException se) { 
			    // Discontinue going higher on a security exception.
			}
			return new ThreadGroup(tg, "ORB ThreadGroup"); 
		    } 
		}
	    );
	} catch (SecurityException e) { 
	    // something wrong, we go back to the original code 
	    threadGroup = Thread.currentThread().getThreadGroup(); 
	}
 
	// This is the unique id of this server (JVM). Multiple incarnations
	// of this server will get different ids.
	// Compute transientServerId = milliseconds since Jan 1, 1970
	// Note: transientServerId will wrap in about 2^32 / 86400000 = 49.7 days.
	// If two ORBS are started at the same time then there is a possibility
	// of having the same transientServerId. This may result in collision 
	// and may be a problem in ior.isLocal() check to see if the object 
	// belongs to the current ORB. This problem is taken care of by checking
	// to see if the IOR port matches ORB server port in legacyIsLocalServerPort()
	// method.
	//
	// XXX need to move server ID to a string for CORBA 3.0.  At that point,
	// make this more unique (possibly use java.rmi.server.UID).
	transientServerId = (int)System.currentTimeMillis();

	orbVersionThreadLocal  = new ThreadLocal () {
	    protected java.lang.Object initialValue() {
		// set default to version of the ORB with correct Rep-ids
		return ORBVersionFactory.getORBVersion() ;
	    }
	};

	resolverLock = new java.lang.Object() ;

	requestDispatcherRegistry = new RequestDispatcherRegistryImpl( 
	    this, ORBConstants.DEFAULT_SCID);
	copierManager = new CopierManagerImpl( this ) ;

	taggedComponentFactoryFinder = 
	    new TaggedComponentFactoryFinderImpl(this) ;
	taggedProfileFactoryFinder = 
	    new TaggedProfileFactoryFinderImpl(this) ;
	taggedProfileTemplateFactoryFinder = 
	    new TaggedProfileTemplateFactoryFinderImpl(this) ;

	dynamicRequests = new Vector();
	svResponseReceived = new SynchVariable();

	OAInvocationInfoStack = 
	    new ThreadLocal () {
		protected java.lang.Object initialValue() 
		{
		    return new StackImpl();
		} 
	    };

	clientInvocationInfoStack = 
	    new ThreadLocal() {
		protected java.lang.Object initialValue() {
		    return new StackImpl();
		}
	    };

	serviceContextRegistry = new ServiceContextRegistry( this ) ;
    }

    protected void setDebugFlags( String[] args )
    {
	for (int ctr=0; ctr<args.length; ctr++ ) {
            String token = args[ctr] ;

            // If there is a public boolean data member in this class
            // named token + "DebugFlag", set it to true.
            try {
                Field fld = this.getClass().getField( token + "DebugFlag" ) ;
                int mod = fld.getModifiers() ;
                if (Modifier.isPublic( mod ) && !Modifier.isStatic( mod ))
                    if (fld.getType() == boolean.class)
                        fld.setBoolean( this, true ) ;
            } catch (Exception exc) {
                // ignore it XXX log this as info
            }
        }
    }

    // Class that defines a parser that gets the name of the
    // ORBConfigurator class.
    private static class ConfigParser extends ParserImplBase {
	// The default here is the ORBConfiguratorImpl that we define,
	// but this can be replaced.
	public Class configurator = ORBConfiguratorImpl.class ;

	public PropertyParser makeParser()
	{
	    PropertyParser parser = new PropertyParser() ;
	    parser.add( ORBConstants.SUN_PREFIX + "ORBConfigurator",
		OperationFactory.classAction(), "configurator" ) ;
	    return parser ;
	}
    }

    private void postInit( String[] params, DataCollector dataCollector )
    {
	// First, create the standard ORB config data.
	// This must be initialized before the ORBConfigurator
	// is executed.
	configData = new ORBDataParserImpl( this, dataCollector) ;

	// Set the debug flags early so they can be used by other
	// parts of the initialization.
	setDebugFlags( configData.getORBDebugFlags() ) ;

	// REVISIT: this should go away after more transport init cleanup
	// and going to ORT based ORBD.  
	getTransportManager();
	getLegacyServerSocketManager();

	// Create a parser to get the configured ORBConfigurator.
	ConfigParser parser = new ConfigParser() ;
	parser.init( dataCollector ) ;

	ORBConfigurator configurator =  null ;
	try {
	    configurator = 
		(ORBConfigurator)(parser.configurator.newInstance()) ;
	} catch (Exception iexc) {
	    throw wrapper.badOrbConfigurator( iexc, parser.configurator.getName() ) ;
	}

	// Finally, run the configurator.  Note that the default implementation allows
	// other configurators with their own parsers to run,
	// using the same DataCollector.
	try {
	    configurator.configure( dataCollector, this ) ;
	} catch (Exception exc) {
	    throw wrapper.orbConfiguratorError( exc ) ;
	}

	// Last of all, create the PIHandler and run the ORB initializers.
	pihandler = new PIHandlerImpl( this, params) ;
	pihandler.initialize() ;

        // Initialize the thread manager pool and byte buffer pool
        // so they may be initialized & accessed without synchronization
        getThreadPoolManager();

        super.getByteBufferPool();
    }

    private synchronized POAFactory getPOAFactory() 
    {
	if (poaFactory == null) {
	    poaFactory = (POAFactory)requestDispatcherRegistry.getObjectAdapterFactory( 
		ORBConstants.TRANSIENT_SCID ) ;
	}

	return poaFactory ;
    }

    private synchronized TOAFactory getTOAFactory() 
    {
	if (toaFactory == null) {
	    toaFactory = (TOAFactory)requestDispatcherRegistry.getObjectAdapterFactory( 
		ORBConstants.TOA_SCID ) ;
	}

	return toaFactory ;
    }

    public void set_parameters( Properties props )
    {
	preInit( null, props ) ;
	DataCollector dataCollector = 
	    DataCollectorFactory.create( props, getLocalHostName() ) ;
	postInit( null, dataCollector ) ;
    }

    protected void set_parameters(Applet app, Properties props)
    {
	preInit( null, props ) ;
	DataCollector dataCollector = 
	    DataCollectorFactory.create( app, props, getLocalHostName() ) ;
	postInit( null, dataCollector ) ;
    }

    protected void set_parameters (String[] params, Properties props)
    {
	preInit( params, props ) ;
	DataCollector dataCollector = 
	    DataCollectorFactory.create( params, props, getLocalHostName() ) ;
	postInit( params, dataCollector ) ;
    }

/****************************************************************************
 * The following methods are standard public CORBA ORB APIs
 ****************************************************************************/

    public synchronized org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        checkShutdownState();
        return new EncapsOutputStream(this);
    }

    /**
     * Get a Current pseudo-object.
     * The Current interface is used to manage thread-specific
     * information for use by the transactions, security and other
     * services. This method is deprecated,
     * and replaced by ORB.resolve_initial_references("NameOfCurrentObject");
     *
     * @return          a Current pseudo-object.
     * @deprecated
     */
    public synchronized org.omg.CORBA.Current get_current()
    {
        checkShutdownState();

        /* _REVISIT_
           The implementation of get_current is not clear. How would
           ORB know whether the caller wants a Current for transactions
           or security ?? Or is it assumed that there is just one
           implementation for both ? If Current is thread-specific,
           then it should not be instantiated; so where does the
           ORB get a Current ? 
	   
	   This should probably be deprecated. */

	throw wrapper.genericNoImpl() ;
    }

    /**
     * Create an NVList
     *
     * @param count	size of list to create
     * @result		NVList created
     *
     * @see NVList
     */
    public synchronized NVList create_list(int count)
    {
        checkShutdownState();
        return new NVListImpl(this, count);
    }

    /**
     * Create an NVList corresponding to an OperationDef
     *
     * @param oper	operation def to use to create list
     * @result		NVList created
     *
     * @see NVList
     */
    public synchronized NVList create_operation_list(org.omg.CORBA.Object oper)
    {
        checkShutdownState();
	throw wrapper.genericNoImpl() ;
    }

    /**
     * Create a NamedValue
     *
     * @result		NamedValue created
     */
    public synchronized NamedValue create_named_value(String s, Any any, int flags)
    {
        checkShutdownState();
        return new NamedValueImpl(this, s, any, flags);
    }

    /**
     * Create an ExceptionList
     *
     * @result		ExceptionList created
     */
    public synchronized org.omg.CORBA.ExceptionList create_exception_list()
    {
        checkShutdownState();
        return new ExceptionListImpl();
    }

    /**
     * Create a ContextList
     *
     * @result		ContextList created
     */
    public synchronized org.omg.CORBA.ContextList create_context_list()
    {
        checkShutdownState();
        return new ContextListImpl(this);
    }

    /**
     * Get the default Context object
     *
     * @result		the default Context object
     */
    public synchronized org.omg.CORBA.Context get_default_context()
    {
        checkShutdownState();
	throw wrapper.genericNoImpl() ;
    }

    /**
     * Create an Environment
     *
     * @result		Environment created
     */
    public synchronized org.omg.CORBA.Environment create_environment()
    {
        checkShutdownState();
        return new EnvironmentImpl();
    }

    public synchronized void send_multiple_requests_oneway(Request[] req)
    {
        checkShutdownState();

        // Invoke the send_oneway on each new Request
        for (int i = 0; i < req.length; i++) {
            req[i].send_oneway();
        }
    }

    /**
     * Send multiple dynamic requests asynchronously.
     *
     * @param req         an array of request objects.
     */
    public synchronized void send_multiple_requests_deferred(Request[] req)
    {
        checkShutdownState();

        // add the new Requests to pending dynamic Requests
        for (int i = 0; i < req.length; i++) {
            dynamicRequests.addElement(req[i]);
        }

        // Invoke the send_deferred on each new Request
        for (int i = 0; i < req.length; i++) {
            AsynchInvoke invokeObject = new AsynchInvoke( this, 
		(com.sun.corba.se.impl.corba.RequestImpl)req[i], true);
            new Thread(invokeObject).start();
        }
    }

    /**
     * Find out if any of the deferred invocations have a response yet.
     */
    public synchronized boolean poll_next_response()
    {
        checkShutdownState();

        Request currRequest;

        // poll on each pending request
        Enumeration ve = dynamicRequests.elements();
        while (ve.hasMoreElements() == true) {
            currRequest = (Request)ve.nextElement();
            if (currRequest.poll_response() == true) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the next request that has gotten a response.
     *
     * @result            the next request ready with a response.
     */
    public org.omg.CORBA.Request get_next_response()
        throws org.omg.CORBA.WrongTransaction
    {
	synchronized( this ) {
	    checkShutdownState();
	}

        while (true) {
            // check if there already is a response
            synchronized ( dynamicRequests ) {
                Enumeration elems = dynamicRequests.elements();
                while ( elems.hasMoreElements() ) {
                    Request currRequest = (Request)elems.nextElement();
                    if ( currRequest.poll_response() ) {
                        // get the response for this successfully polled Request
                        currRequest.get_response();
                        dynamicRequests.removeElement(currRequest);
                        return currRequest;
                    }
                }
            }

            // wait for a response
            synchronized(this.svResponseReceived) {
                while (!this.svResponseReceived.value()) {
                    try {
                        this.svResponseReceived.wait();
                    } catch(java.lang.InterruptedException ex) {
			// NO-OP
		    }
                }
                // reinitialize the response flag
                this.svResponseReceived.reset();
            }
        }
    }

    /**
     * Notify response to ORB for get_next_response
     */
    public void notifyORB() 
    {
	synchronized (this.svResponseReceived) {
	    this.svResponseReceived.set();
	    this.svResponseReceived.notify();
	}
    }

    /**
     * Convert an object ref to a string.
     * @param obj The object to stringify.
     * @return A stringified object reference.
     */
    public synchronized String object_to_string(org.omg.CORBA.Object obj)
    {
        checkShutdownState();

        // Handle the null objref case
        if (obj == null) {
	    IOR nullIOR = IORFactories.makeIOR( this ) ;
            return nullIOR.stringify();
	}

	IOR ior = null ;

	try {
	    ior = ORBUtility.connectAndGetIOR( this, obj ) ;
	} catch (BAD_PARAM bp) {
	    // Throw MARSHAL instead if this is a LOCAL_OBJECT_NOT_ALLOWED error.
	    if (bp.minor == ORBUtilSystemException.LOCAL_OBJECT_NOT_ALLOWED) {
		throw omgWrapper.notAnObjectImpl( bp ) ;
	    } else
		// Not a local object problem: just rethrow the exception.
		// Do not wrap and log this, since it was already logged at its
		// point of origin.
		throw bp ;
	}

	return ior.stringify() ;
    }

    /**
     * Convert a stringified object reference to the object it represents.
     * @param str The stringified object reference.
     * @return The unstringified object reference.
     */
    public org.omg.CORBA.Object string_to_object(String str)
    {
	Operation op ;

	synchronized (this) {
	    checkShutdownState();
	    op = urlOperation ;
	}

	if (str == null)
	    throw wrapper.nullParam() ;

	synchronized (resolverLock) {
	    org.omg.CORBA.Object obj = (org.omg.CORBA.Object)op.operate( str ) ;
	    return obj ;
	}
    }

    // pure java orb support, moved this method from FVDCodeBaseImpl.
    // Note that we connect this if we have not already done so.
    public synchronized IOR getFVDCodeBaseIOR()
    {
        if (codeBaseIOR != null) // i.e. We are already connected to it
            return codeBaseIOR;

        // backward compatability 4365188
        CodeBase cb;

        ValueHandler vh = ORBUtility.createValueHandler(this);

        cb = (CodeBase)vh.getRunTimeCodeBase();
	return ORBUtility.connectAndGetIOR( this, cb ) ;
    }

    /**
     * Get the TypeCode for a primitive type.
     *
     * @param tcKind	the integer kind for the primitive type
     * @return		the requested TypeCode
     */
    public synchronized TypeCode get_primitive_tc(TCKind tcKind)
    {
        checkShutdownState();
	return get_primitive_tc( tcKind.value() ) ; 
    }

    /**
     * Create a TypeCode for a structure.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param members	an array describing the members of the TypeCode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_struct_tc(String id,
                                     String name,
                                     StructMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_struct, id, name, members);
    }

    /**
     * Create a TypeCode for a union.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param discriminator_type
     *			the type of the union discriminator.
     * @param members	an array describing the members of the TypeCode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_union_tc(String id,
                                    String name,
                                    TypeCode discriminator_type,
                                    UnionMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this,
                                TCKind._tk_union,
                                id,
                                name,
                                discriminator_type,
                                members);
    }

    /**
     * Create a TypeCode for an enum.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param members	an array describing the members of the TypeCode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_enum_tc(String id,
                                   String name,
                                   String[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_enum, id, name, members);
    }

    /**
     * Create a TypeCode for an alias.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param original_type
     * 			the type this is an alias for.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_alias_tc(String id,
                                    String name,
                                    TypeCode original_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_alias, id, name, original_type);
    }

    /**
     * Create a TypeCode for an exception.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param members	an array describing the members of the TypeCode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_exception_tc(String id,
                                        String name,
                                        StructMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_except, id, name, members);
    }

    /**
     * Create a TypeCode for an interface.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_interface_tc(String id,
                                        String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_objref, id, name);
    }

    /**
     * Create a TypeCode for a string.
     *
     * @param bound	the bound for the string.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_string_tc(int bound)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_string, bound);
    }

    /**
     * Create a TypeCode for a wide string.
     *
     * @param bound	the bound for the string.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_wstring_tc(int bound)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_wstring, bound);
    }

    /**
     * Create a TypeCode for a sequence.
     *
     * @param bound	the bound for the sequence.
     * @param element_type
     *			the type of elements of the sequence.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_sequence_tc(int bound,
                                       TypeCode element_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, element_type);
    }


    /**
     * Create a recursive TypeCode in a sequence.
     *
     * @param bound	the bound for the sequence.
     * @param offset	the index to the enclosing TypeCode that is
     *			being referenced.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_recursive_sequence_tc(int bound,
                                                 int offset)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, offset);
    }


    /**
     * Create a TypeCode for an array.
     *
     * @param length	the length of the array.
     * @param element_type
     *			the type of elements of the array.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_array_tc(int length,
                                    TypeCode element_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_array, length, element_type);
    }


    public synchronized org.omg.CORBA.TypeCode create_native_tc(String id,
                                                   String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_native, id, name);
    }

    public synchronized org.omg.CORBA.TypeCode create_abstract_interface_tc(
                                                               String id,
                                                               String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_abstract_interface, id, name);
    }

    public synchronized org.omg.CORBA.TypeCode create_fixed_tc(short digits, short scale)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_fixed, digits, scale);
    }

    public synchronized org.omg.CORBA.TypeCode create_value_tc(String id,
                                                  String name,
                                                  short type_modifier,
                                                  TypeCode concrete_base,
                                                  ValueMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_value, id, name,
                                type_modifier, concrete_base, members);
    }

    public synchronized org.omg.CORBA.TypeCode create_recursive_tc(String id) {
        checkShutdownState();
        return new TypeCodeImpl(this, id);
    }

    public synchronized org.omg.CORBA.TypeCode create_value_box_tc(String id,
                                                      String name,
                                                      TypeCode boxed_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_value_box, id, name, 
	    boxed_type);
    }

    /**
     * Create a new Any
     *
     * @return		the new Any created.
     */
    public synchronized Any create_any()
    {
        checkShutdownState();
        return new AnyImpl(this);
    }

    // TypeCodeFactory interface methods.
    // Keeping track of type codes by repository id.

    // Keeping a cache of TypeCodes associated with the class
    // they got created from in Util.writeAny().

    public synchronized void setTypeCodeForClass(Class c, TypeCodeImpl tci) 
    {
        if (typeCodeForClassMap == null)
            typeCodeForClassMap = Collections.synchronizedMap(
		new WeakHashMap(64));
        // Store only one TypeCode per class.
        if ( ! typeCodeForClassMap.containsKey(c))
            typeCodeForClassMap.put(c, tci);
    }

    public synchronized TypeCodeImpl getTypeCodeForClass(Class c) 
    {
        if (typeCodeForClassMap == null)
            return null;
        return (TypeCodeImpl)typeCodeForClassMap.get(c);
    }

/****************************************************************************
 * The following methods deal with listing and resolving the initial
 * (bootstrap) object references such as "NameService".
 ****************************************************************************/

    /**
     * Get a list of the initially available CORBA services.
     * This does not work unless an ORBInitialHost is specified during 
     * initialization (or unless there is an ORB running on the AppletHost) 
     * since the localhostname
     * is inaccessible to applets. If a service properties URL was specified,
     * then it is used, otherwise the bootstrapping protocol is used.
     * @return A list of the initial services available.
     */
    public String[] list_initial_services()
    {
	Resolver res ;

	synchronized( this ) {
	    checkShutdownState();
	    res = resolver ;
	}

	synchronized (resolverLock) {
	    java.util.Set keys = res.list() ;
	    return (String[])keys.toArray( new String[keys.size()] ) ;
	}
    }

    /**
     * Resolve the stringified reference of one of the initially
     * available CORBA services.
     * @param identifier The stringified object reference of the
     * desired service.
     * @return An object reference for the desired service.
     * @exception InvalidName The supplied identifier is not associated
     * with a known service.
     * @exception SystemException One of a fixed set of Corba system exceptions.
     */
    public org.omg.CORBA.Object resolve_initial_references(
        String identifier) throws InvalidName
    {
	Resolver res ;

	synchronized( this ) {
	    checkShutdownState();
	    res = resolver ;
	}

	synchronized (resolverLock) {
	    org.omg.CORBA.Object result = res.resolve( identifier ) ;

	    if (result == null)
		throw new InvalidName() ;
	    else
		return result ;
	}
    }

    /**
     * If this operation is called with an id, <code>"Y"</code>, and an
     * object, <code>YY</code>, then a subsequent call to
     * <code>ORB.resolve_initial_references( "Y" )</code> will
     * return object <code>YY</code>.
     *
     * @param id The ID by which the initial reference will be known.
     * @param obj The initial reference itself.
     * @throws InvalidName if this operation is called with an empty string id
     *     or this operation is called with an id that is already registered,
     *     including the default names defined by OMG.
     * @throws BAD_PARAM if the obj parameter is null.
     */
    public void register_initial_reference(
        String id, org.omg.CORBA.Object obj ) throws InvalidName
    {
	CorbaServerRequestDispatcher insnd ;

        if ((id == null) || (id.length() == 0))
            throw new InvalidName() ;

	synchronized (this) {
	    checkShutdownState();
	}

	synchronized (resolverLock) {
	    insnd = insNamingDelegate ;

	    java.lang.Object obj2 = localResolver.resolve( id ) ;
	    if (obj2 != null)
		throw new InvalidName(id + " already registered") ;

	    localResolver.register( id, ClosureFactory.makeConstant( obj )) ;
	}
      
	synchronized (this) {
	    if (StubAdapter.isStub(obj))
		// Make all remote object references available for INS.
		requestDispatcherRegistry.registerServerRequestDispatcher( 
		    insnd, id ) ;
	}
    }

/****************************************************************************
 * The following methods (introduced in POA / CORBA2.1) deal with
 * shutdown / single threading.
 ****************************************************************************/

    public void run() 
    {
	synchronized (this) {
	    checkShutdownState();
	}

        synchronized (runObj) {
            try {
                runObj.wait();
            } catch ( InterruptedException ex ) {}
        }
    }

    public void shutdown(boolean wait_for_completion) 
    {
        // to wait for completion, we would deadlock, so throw a standard
        // OMG exception.
        if (wait_for_completion && ((Boolean)isProcessingInvocation.get()).booleanValue()) {
		throw omgWrapper.shutdownWaitForCompletionDeadlock() ;
            }

        boolean doShutdown = false ;

        synchronized (this) {
	    checkShutdownState() ;

	    if (status == STATUS_SHUTTING_DOWN) {
	        if (!wait_for_completion)
		// If we are already shutting down and don't want
		// to wait, nothing to do: return.
		return ;
	    } else {
	        // The ORB status was STATUS_OPERATING, so start the shutdown.
	        status = STATUS_SHUTTING_DOWN ;
	        doShutdown = true ;
                }
            }
	
        // At this point, status is SHUTTING_DOWN.
        // All shutdown calls with wait_for_completion == true must synchronize
        // here.  Only the first call will be made with doShutdown == true.
        synchronized (shutdownObj) {
	    if (doShutdown) {
	        // shutdownServants will set all POAManagers into the
	        // INACTIVE state, causing request to be rejected.
	        // If wait_for_completion is true, this will not return until
	        // all invocations have completed.
	        shutdownServants(wait_for_completion);
    
	        synchronized (runObj) {
                runObj.notifyAll();
            }
	    	
	        synchronized (this) {
            status = STATUS_SHUTDOWN;
        }
    }
        }
    }

    /** This method shuts down the ORB and causes orb.run() to return.
     *	It will cause all POAManagers to be deactivated, which in turn
     *  will cause all POAs to be deactivated.
     */
    protected void shutdownServants(boolean wait_for_completion) {
	Iterator iter = requestDispatcherRegistry.getObjectAdapterFactories().iterator() ;
	while (iter.hasNext()) {
	    ObjectAdapterFactory oaf = (ObjectAdapterFactory)iter.next() ;
	    oaf.shutdown( wait_for_completion ) ;
	}
    }

    // REVISIT: was protected - made public for framework
    // Note that the caller must hold the ORBImpl lock.
    public void checkShutdownState() 
    {
        if (status == STATUS_DESTROYED) {
	    throw wrapper.orbDestroyed() ;
        }

        if (status == STATUS_SHUTDOWN) {
	    throw omgWrapper.badOperationAfterShutdown() ;
        }
    }

    public boolean isDuringDispatch() 
    {
	Boolean value = (Boolean)(isProcessingInvocation.get()) ;
	return value.booleanValue() ;
    }

    public void startingDispatch() 
    {
        synchronized (invocationObj) {
            isProcessingInvocation.set(Boolean.TRUE);
        }
    }

    public void finishedDispatch() 
    {
        synchronized (invocationObj) {
            isProcessingInvocation.set(Boolean.FALSE);
        }
    }

    /**
     *	formal/99-10-07 p 159: "If destroy is called on an ORB that has
     *	not been shut down, it will start the shutdown process and block until
     *	the ORB has shut down before it destroys the ORB."
     */
    public synchronized void destroy() 
    {
	boolean shutdownFirst = false ;

	synchronized (this) {
	    shutdownFirst = (status == STATUS_OPERATING) ;
	}

	if (shutdownFirst) {
            shutdown(true);
        }

        synchronized (this) {
	    if (status < STATUS_DESTROYED) {
        getCorbaTransportManager().close();
	getPIHandler().destroyInterceptors() ;
        status = STATUS_DESTROYED;
    }
	}

    }

    /**
     * Registers a value factory for a particular repository ID.
     *
     * @param repositoryID the repository ID.
     * @param factory the factory.
     * @return the previously registered factory for the given repository ID, 
     * or null if no such factory was previously registered.
     * @exception org.omg.CORBA.BAD_PARAM if the registration fails.
     **/
    public synchronized ValueFactory register_value_factory(String repositoryID, 
	ValueFactory factory) 
    {
        checkShutdownState();

        if ((repositoryID == null) || (factory == null))
	    throw omgWrapper.unableRegisterValueFactory() ;

        return (ValueFactory)valueFactoryCache.put(repositoryID, factory);
    }

    /**
     * Unregisters a value factory for a particular repository ID.
     *
     * @param repositoryID the repository ID.
     **/
    public synchronized void unregister_value_factory(String repositoryID) 
    {
        checkShutdownState();

        if (valueFactoryCache.remove(repositoryID) == null)
	    throw wrapper.nullParam() ;
    }

    /**
     * Finds and returns a value factory for the given repository ID.
     * The value factory returned was previously registered by a call to
     * {@link #register_value_factory} or is the default factory.
     *
     * @param repositoryID the repository ID.
     * @return the value factory.
     * @exception org.omg.CORBA.BAD_PARAM if unable to locate a factory.
     **/
    public synchronized ValueFactory lookup_value_factory(String repositoryID) 
    {
        checkShutdownState();

        ValueFactory factory = 
	    (ValueFactory)valueFactoryCache.get(repositoryID);

        if (factory == null) {
            try {
                factory = Utility.getFactory(null, null, null, repositoryID);
            } catch(org.omg.CORBA.MARSHAL ex) {
		throw wrapper.unableFindValueFactory( ex ) ;
            }
        }

	return factory ;
    }

    public OAInvocationInfo peekInvocationInfo() 
    {
	StackImpl stack = (StackImpl)(OAInvocationInfoStack.get()) ;
	return (OAInvocationInfo)(stack.peek()) ;
    }

    public void pushInvocationInfo( OAInvocationInfo info ) 
    {
	StackImpl stack = (StackImpl)(OAInvocationInfoStack.get()) ;
	stack.push( info ) ;
    }

    public OAInvocationInfo popInvocationInfo() 
    {
	StackImpl stack = (StackImpl)(OAInvocationInfoStack.get()) ;
	return (OAInvocationInfo)(stack.pop()) ;
    }

    /**
     * The bad server id handler is used by the Locator to
     * send back the location of a persistant server to the client.
     */

    private Object badServerIdHandlerAccessLock = new Object();

    public void initBadServerIdHandler() 
    {
	synchronized (badServerIdHandlerAccessLock) {
	    Class cls = configData.getBadServerIdHandler() ;
	    if (cls != null) {
		try {
		    Class[] params = new Class[] { org.omg.CORBA.ORB.class };
		    java.lang.Object[] args = new java.lang.Object[]{this};
		    Constructor cons = cls.getConstructor(params);
		    badServerIdHandler = 
			(BadServerIdHandler) cons.newInstance(args);
		} catch (Exception e) {
		    throw wrapper.errorInitBadserveridhandler( e ) ;
		}
	    }
	}
    }

    public void setBadServerIdHandler( BadServerIdHandler handler ) 
    {
	synchronized (badServerIdHandlerAccessLock) {
	    badServerIdHandler = handler;
	}
    }

    public void handleBadServerId( ObjectKey okey ) 
    {
	synchronized (badServerIdHandlerAccessLock) {
	    if (badServerIdHandler == null)
		throw wrapper.badServerId() ;
	    else 
		badServerIdHandler.handle( okey ) ;
	}
    }

    public synchronized org.omg.CORBA.Policy create_policy( int type, 
	org.omg.CORBA.Any val ) throws org.omg.CORBA.PolicyError
    {
	checkShutdownState() ;

	return pihandler.create_policy( type, val ) ;
    }

    /** This is the implementation of the public API used to connect
     *  a servant-skeleton to the ORB.
     */
    public synchronized void connect(org.omg.CORBA.Object servant)
    {
        checkShutdownState();
	if (getTOAFactory() == null)
	    throw wrapper.noToa() ;

        try {
	    String codebase = javax.rmi.CORBA.Util.getCodebase( servant.getClass() ) ;
	    getTOAFactory().getTOA( codebase ).connect( servant ) ;
        } catch ( Exception ex ) {
	    throw wrapper.orbConnectError( ex ) ;
        }
    }

    public synchronized void disconnect(org.omg.CORBA.Object obj)
    {
        checkShutdownState();
	if (getTOAFactory() == null)
	    throw wrapper.noToa() ;

        try {
	    getTOAFactory().getTOA().disconnect( obj ) ;
        } catch ( Exception ex ) {
	    throw wrapper.orbConnectError( ex ) ;
        }
    }

    public int getTransientServerId()
    {
        if( configData.getORBServerIdPropertySpecified( ) ) {
            // ORBServerId is specified then use that value
            return configData.getPersistentServerId( );
        }
        return transientServerId;
    }

    public RequestDispatcherRegistry getRequestDispatcherRegistry()
    {
        return requestDispatcherRegistry;
    }

    public ServiceContextRegistry getServiceContextRegistry()
    {
	return serviceContextRegistry ;
    } 

    // XXX All of the isLocalXXX checking needs to be revisited.
    // First of all, all three of these methods are called from
    // only one place in impl.ior.IORImpl.  Second, we have problems
    // both with multi-homed hosts and with multi-profile IORs.
    // A possible strategy: like the LocalClientRequestDispatcher, we need
    // to determine this more abstractly at the ContactInfo level.
    // This level should probably just get the CorbaContactInfoList from
    // the IOR, then iterator over ContactInfo.  If any ContactInfo is
    // local, the IOR is local, and we can pick one to create the 
    // LocalClientRequestDispatcher as well.  Bottom line: this code needs to move.

    // XXX What about multi-homed host?
    public boolean isLocalHost( String hostName ) 
    {
	return hostName.equals( configData.getORBServerHost() ) ||
	    hostName.equals( getLocalHostName() ) ;
    }

    public boolean isLocalServerId( int subcontractId, int serverId )
    {
	if ((subcontractId < ORBConstants.FIRST_POA_SCID) || 
	    (subcontractId > ORBConstants.MAX_POA_SCID))
	    return serverId == getTransientServerId( ) ;
		
	// XXX isTransient info should be stored in subcontract registry
	if (ORBConstants.isTransient( subcontractId ))
	    return (serverId == getTransientServerId()) ;
	else if (configData.getPersistentServerIdInitialized())
	    return (serverId == configData.getPersistentServerId()) ;
	else
	    return false ;
    }

    /*************************************************************************
     *  The following public methods are for ORB shutdown.
     *************************************************************************/

    private String getHostName(String host) 
	throws java.net.UnknownHostException 
    {
        return InetAddress.getByName( host ).getHostAddress();
    }

    /* keeping a copy of the getLocalHostName so that it can only be called 
     * internally and the unauthorized clients cannot have access to the
     * localHost information, originally, the above code was calling 
     * getLocalHostName from Connection.java.  If the hostname is cached in 
     * Connection.java, then
     * it is a security hole, since any unauthorized client has access to
     * the host information.  With this change it is used internally so the
     * security problem is resolved.  Also in Connection.java, the 
     * getLocalHost() implementation has changed to always call the 
     * InetAddress.getLocalHost().getHostAddress()
     * The above mentioned method has been removed from the connection class
     */

    private static String localHostString = null;

    private synchronized String getLocalHostName() 
    {
        if (localHostString == null) {
            try {
		localHostString = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ex) {
		throw wrapper.getLocalHostFailed( ex ) ;
            }
	}
	return localHostString ;
    }

 /******************************************************************************
 *  The following public methods are for ORB shutdown. 
 *
 ******************************************************************************/

    /** This method always returns false because the ORB never needs the
     *  main thread to do work.
     */
    public synchronized boolean work_pending()
    {
        checkShutdownState();
	throw wrapper.genericNoImpl() ;
    }
  
    /** This method does nothing. It is not required by the spec to do anything!
     */
    public synchronized void perform_work()
    {
        checkShutdownState();
	throw wrapper.genericNoImpl() ;
    }

    public synchronized void set_delegate(java.lang.Object servant){
        checkShutdownState();

	POAFactory poaFactory = getPOAFactory() ;
	if (poaFactory != null)
	    ((org.omg.PortableServer.Servant)servant)
		._set_delegate( poaFactory.getDelegateImpl() ) ;
	else
	    throw wrapper.noPoa() ;
    }

    ////////////////////////////////////////////////////
    //
    // pept.broker.Broker
    //

    public ClientInvocationInfo createOrIncrementInvocationInfo() 
    {
	StackImpl invocationInfoStack =
	    (StackImpl) clientInvocationInfoStack.get();
	ClientInvocationInfo clientInvocationInfo = null;
	if (!invocationInfoStack.empty()) {
	    clientInvocationInfo =
		(ClientInvocationInfo) invocationInfoStack.peek();
	}
	if ((clientInvocationInfo == null) || 
	    (!clientInvocationInfo.isRetryInvocation()))
	{
	    // This is a new call - not a retry.
	    clientInvocationInfo = new CorbaInvocationInfo(this);
	    startingDispatch();
	    invocationInfoStack.push(clientInvocationInfo);
	}
	// Reset retry so recursive calls will get a new info object.
	clientInvocationInfo.setIsRetryInvocation(false);
	clientInvocationInfo.incrementEntryCount();
	return clientInvocationInfo;
    }
    
    public void releaseOrDecrementInvocationInfo() 
    {
	StackImpl invocationInfoStack =
	    (StackImpl)clientInvocationInfoStack.get();
	ClientInvocationInfo clientInvocationInfo = null;
	if (!invocationInfoStack.empty()) {
	    clientInvocationInfo =
		(ClientInvocationInfo)invocationInfoStack.peek();
	} else {
	    throw wrapper.invocationInfoStackEmpty() ;
	}
	clientInvocationInfo.decrementEntryCount();
	if (clientInvocationInfo.getEntryCount() == 0) {
	    invocationInfoStack.pop();
	    finishedDispatch();
	}
    }
    
    public ClientInvocationInfo getInvocationInfo() 
    {
	StackImpl invocationInfoStack =
	    (StackImpl) clientInvocationInfoStack.get();
	return (ClientInvocationInfo) invocationInfoStack.peek();
    }

    ////////////////////////////////////////////////////
    //
    //
    //

    private Object clientDelegateFactoryAccessorLock = new Object();

    public void setClientDelegateFactory( ClientDelegateFactory factory ) 
    {
	synchronized (clientDelegateFactoryAccessorLock) {
	    clientDelegateFactory = factory ;
	}
    }

    public ClientDelegateFactory getClientDelegateFactory() 
    {
	synchronized (clientDelegateFactoryAccessorLock) {
	    return clientDelegateFactory ;
	}
    }

    private Object corbaContactInfoListFactoryAccessLock = new Object();

    public void setCorbaContactInfoListFactory( CorbaContactInfoListFactory factory ) 
    {
	synchronized (corbaContactInfoListFactoryAccessLock) {
	    corbaContactInfoListFactory = factory ;
	}
    }

    public synchronized CorbaContactInfoListFactory getCorbaContactInfoListFactory() 
    {
	return corbaContactInfoListFactory ;
    }

    /** Set the resolver used in this ORB.  This resolver will be used for list_initial_services
     * and resolve_initial_references.
     */
    public void setResolver( Resolver resolver ) 
    {
	synchronized (resolverLock) {
	    this.resolver = resolver ;
	}
    }

    /** Get the resolver used in this ORB.  This resolver will be used for list_initial_services
     * and resolve_initial_references.
     */
    public Resolver getResolver() 
    {
	synchronized (resolverLock) {
	    return resolver ;
	}
    }

    /** Set the LocalResolver used in this ORB.  This LocalResolver is used for 
     * register_initial_reference only.
     */
    public void setLocalResolver( LocalResolver resolver ) 
    {
	synchronized (resolverLock) {
	    this.localResolver = resolver ;
	}
    }

    /** Get the LocalResolver used in this ORB.  This LocalResolver is used for 
     * register_initial_reference only.
     */
    public LocalResolver getLocalResolver() 
    {
	synchronized (resolverLock) {
	    return localResolver ;
	}
    }

    /** Set the operation used in string_to_object calls.  The Operation must expect a
     * String and return an org.omg.CORBA.Object.
     */
    public void setURLOperation( Operation stringToObject ) 
    {
	synchronized (resolverLock) {
	    urlOperation = stringToObject ;
	}
    }

    /** Get the operation used in string_to_object calls.  The Operation must expect a
     * String and return an org.omg.CORBA.Object.
     */
    public Operation getURLOperation() 
    {
	synchronized (resolverLock) {
	    return urlOperation ;
	}
    }

    public void setINSDelegate( CorbaServerRequestDispatcher sdel )
    {
	synchronized (resolverLock) {
	    insNamingDelegate = sdel ;
	}
    }

    public TaggedComponentFactoryFinder getTaggedComponentFactoryFinder() 
    {
	return taggedComponentFactoryFinder ;
    }

    public IdentifiableFactoryFinder getTaggedProfileFactoryFinder() 
    {
	return taggedProfileFactoryFinder ;
    }

    public IdentifiableFactoryFinder getTaggedProfileTemplateFactoryFinder() 
    {
	return taggedProfileTemplateFactoryFinder ;
    }

    private Object objectKeyFactoryAccessLock = new Object();

    public ObjectKeyFactory getObjectKeyFactory() 
    {
	synchronized (objectKeyFactoryAccessLock) {
	    return objectKeyFactory ;
	}
    }

    public void setObjectKeyFactory( ObjectKeyFactory factory ) 
    {
	synchronized (objectKeyFactoryAccessLock) {
	    objectKeyFactory = factory ;
	}
    }

    private Object transportManagerAccessorLock = new Object();

    public TransportManager getTransportManager()
    {
	synchronized (transportManagerAccessorLock) {
	    if (transportManager == null) {
		transportManager = new CorbaTransportManagerImpl(this);
	    }
	    return transportManager;
	}
    }

    public CorbaTransportManager getCorbaTransportManager()
    {
	return (CorbaTransportManager) getTransportManager();
    }

    private Object legacyServerSocketManagerAccessLock = new Object();

    public LegacyServerSocketManager getLegacyServerSocketManager()
    {
	synchronized (legacyServerSocketManagerAccessLock) {
	    if (legacyServerSocketManager == null) {
		legacyServerSocketManager = new LegacyServerSocketManagerImpl(this);
	    }
	    return legacyServerSocketManager;
	}
    }

    private Object threadPoolManagerAccessLock = new Object();

    public void setThreadPoolManager(ThreadPoolManager mgr) 
    {
	synchronized (threadPoolManagerAccessLock) {
	    threadpoolMgr = mgr;
	}
    }

    public ThreadPoolManager getThreadPoolManager() 
    {
	synchronized (threadPoolManagerAccessLock) {
	    if (threadpoolMgr == null) {
		threadpoolMgr = new ThreadPoolManagerImpl( threadGroup );
	    }
	    return threadpoolMgr;
	}
    }

    public CopierManager getCopierManager()
    {
	return copierManager ;
    }
} // Class ORBImpl

////////////////////////////////////////////////////////////////////////
/// Helper class for a Synchronization Variable
////////////////////////////////////////////////////////////////////////

class SynchVariable 
{
    // Synchronization Variable
    public boolean _flag;

    // Constructor
    SynchVariable() 
    {
        _flag = false;
    }

    // set Flag to true
    public void set() 
    {
        _flag = true;
    }

        // get value
    public boolean value() 
    {
        return _flag;
    }

    // reset Flag to true
    public void reset() 
    {
        _flag = false;
    }
}

// End of file.
