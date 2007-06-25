/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.xml.ws;

/** The <code>Binding</code> interface is the base interface
 *  for JAX-WS protocol bindings.
 *
 *  @since JAX-WS 2.0
**/
public interface Binding {

  /** Gets a copy of the handler chain for a protocol binding instance.
   *  If the returned chain is modified a call to <code>setHandlerChain</code>
   * is required to configure the binding instance with the new chain.
   *
   *  @return java.util.List<javax.xml.ws.handler.HandlerInfo> Handler chain
  **/
  public java.util.List<javax.xml.ws.handler.Handler> getHandlerChain();

  /** Sets the handler chain for the protocol binding instance.
   *
   *  @param chain    A List of handler configuration entries
   *  @throws WebServiceException On an error in the configuration of
   *                  the handler chain
   *  @throws java.lang.UnsupportedOperationException If this
   *          operation is not supported. This may be done to
   *          avoid any overriding of a pre-configured handler
   *          chain.
  **/
  public void setHandlerChain(java.util.List<javax.xml.ws.handler.Handler> chain);
}
