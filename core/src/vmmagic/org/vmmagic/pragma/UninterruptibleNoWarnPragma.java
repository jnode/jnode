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
