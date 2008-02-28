/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
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
