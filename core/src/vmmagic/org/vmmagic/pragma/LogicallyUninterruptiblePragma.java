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
 
package org.vmmagic.pragma;

/**
 * A pragma that can be used to declare that a 
 * particular method is logically uninterruptible
 * even though it contains bytecodes that are actually interruptible.
 * The effect of this pragma is to supress warning messages
 * about violations of uninterruptiblity when compiling a method 
 * that throws this exception.
 * There are two cases in which using the pragma is justified.
 * <ul>
 * <li> Uninterruptibility is ensured via some other mechansism. 
 *      For example, the method explicitly disables threadswitching
 *      around the interruptible regions (VM.sysWrite on String).  
 *      Or the interruptible regions are not reachable when the VM is 
 *      running (various VM.sysWrite that check VM.runningVM).
 * <li> The interruptible regions represent an 'error' condition that will
 *      never be executed unless the VM is already in the process of reporting
 *      an error, for example VM_Runtime.raiseClassCastException.
 * <ul>
 * Extreme care must be exercised when using this pragma since it supresses 
 * the checking of uninterruptibility.
 * 
 * @deprecated
 * @author Dave Grove
 */
public class LogicallyUninterruptiblePragma extends PragmaException {
}
