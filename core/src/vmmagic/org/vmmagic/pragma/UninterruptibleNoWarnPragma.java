/*
 * $Id$
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
