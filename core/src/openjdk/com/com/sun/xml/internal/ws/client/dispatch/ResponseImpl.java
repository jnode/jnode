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
package com.sun.xml.internal.ws.client.dispatch;

import com.sun.xml.internal.ws.client.AsyncHandlerService;
import com.sun.xml.internal.ws.client.ResponseContext;

import javax.xml.ws.Response;
import java.rmi.server.UID;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * The <code>Response</code> interface provides methods used to obtain the
 * payload and context a message sent in response to an operation invocation.
 * For asynchronous operation invocations it provides additional methods to
 * check the status of the request.
 *
 * @author JAXWS Development Team
 * @version 1.0
 */


public class ResponseImpl<T> extends FutureTask<T> implements Response<T> {
    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.internal.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());
    private UID uid;
    private Lock _lock;
    private AsyncHandlerService _handlerService;
    private ResponseContext _responseContext;
    private boolean handler;

    public ResponseImpl(Callable<T> callable) {
        super(callable);
        _lock = new ReentrantLock();
    }

    public ResponseImpl(Runnable runable, T result) {
        super(runable, result);
        _lock = new ReentrantLock();
    }

    //protected method need to overide
    public void setException(Exception ex) {
        _lock.lock();
        try {
            super.setException(ex);
        } catch (Exception e) {
        } finally {
            _lock.unlock();
        }
    }

    public void set(T result) {
        _lock.lock();
        try {
            super.set(result);
        } catch (Exception e) {
        } finally {
            _lock.unlock();
        }
    }

    /**
     * Gets the contained response context.
     *
     * @return The contained response context. May be <code>null</code> if a
     *         response is not yet available.
     */
    public Map<String, Object> getContext() {
        if (!isDone()) {
            return null;
        } else {
            return (_responseContext);
        }
    }

    public void setResponseContext(Map context) {
        _responseContext = (ResponseContext) context;
    }

    public synchronized void setUID(UID id) {
        uid = id;
    }

    public synchronized UID getUID() {
        return uid;
    }

    public void setHandlerService(AsyncHandlerService handlerService) {
        _handlerService = handlerService;
    }

    //got to lock

    public void done() {
        _lock.lock();
        try {
            if (!isCancelled())
                _handlerService.executeWSFuture();

        } catch (Exception e) {
        } finally {
            _lock.unlock();
        }
    }
}
