/*
 * $Id$
 */
package org.vmmagic.pragma; 


/**
 * A pragma that can be used to declare that a 
 * particular method is interruptible.  
 * Used to override the class-wide pragma
 * implied by implementing {@link Uninterruptible}.
 * 
 * @author Dave Grove
 */
public class InterruptiblePragma extends PragmaException {
}
