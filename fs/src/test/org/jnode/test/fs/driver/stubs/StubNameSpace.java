/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.test.fs.driver.stubs;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import org.jnode.naming.DefaultNameSpace;
import org.jnode.test.support.MockUtils;

public class StubNameSpace extends DefaultNameSpace {
    private static final Logger log = Logger.getLogger(StubNameSpace.class);

    public StubNameSpace() {
    }

    /**
     * Lookup a service with a given name.
     *
     * @param name
     * @throws NameNotFoundException if the name was not found in this namespace
     */
    public <T> T lookup(Class<T> name) throws NameNotFoundException {
        if (!namespace.containsKey(name)) {
            createAndBindMockService(name);
        }

        return super.lookup(name);
    }

    @SuppressWarnings("unchecked")
    protected <T> void createAndBindMockService(Class<T> name) {
        try {
            bind(name, MockUtils.createMockObject(name));
        } catch (NameAlreadyBoundException e) {
            log.error("can't bind service", e);
        } catch (NamingException e) {
            log.error("can't bind service", e);
        }
    }
}
