/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.vmmagic.pragma; 

/**
 * A pragma that has the same direct effect as UninterruptiblePragma
 * but also suppresses checking of uninterruptibility violations for
 * the method.  This should be used with care and is only justified when 
 * Uninterruptibility is ensured via some other mechansism. 
 * For example, the method explicitly disables threadswitching
 * around the interruptible regions (VM.sysWrite on String).  
 * Or the interruptible regions are not reachable when the VM is 
 * running (various VM.sysWrite that check VM.runningVM).
 *
 * @author Dave Grove
 */
public class UninterruptibleNoWarnPragma extends PragmaException {
}
