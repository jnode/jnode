/* ActivationInstantiator.java --
   Copyright (c) 1996, 1997, 1998, 1999, 2004  Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

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


package java.rmi.activation;

import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The implementation of this interface creates (instantiates) the new remote
 * objects in response to the activation request. The instantiator is returned
 * by the {@link ActivationGroup} that calls 
 * {@link ActivationSystem#activeGroup(ActivationGroupID, ActivationInstantiator, long)}.
 */
public interface ActivationInstantiator
  extends Remote
{
  /**
   * Creates and instantiate a new remote object. This method performs the
   * following tasks:
   * <ul>
   * <li>Finds and loads (if not already loaded) the class of the object being
   * instantiated</li>
   * <li>Creates an instance of the object using its special two parameter
   * activation constructor, the first parameter being the {@link ActivationID}
   * and the second the {@link MarshalledObject}.</li>
   * 
   * @param id the id of the object being instantiated
   * @param desc the activation descriptor being instantiated
   * @return the MarshalledObject, containing the stub to the newly created
   *         object.
   * @throws ActivationException if the activation fails
   * @throws RemoteException if the remote call fails
   */
  MarshalledObject newInstance (ActivationID id, ActivationDesc desc)
    throws ActivationException, RemoteException;
}
