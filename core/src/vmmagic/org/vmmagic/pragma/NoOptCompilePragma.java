/*
 * $Id$
 */
package org.vmmagic.pragma; 

/**
 * This pragma indicates that a particular method should never be 
 * compiled by the optimizing compiler. It also implies that the
 * method will never be inlined by the optimizing compiler.
 * 
 * @author Dave Grove
 */
public class NoOptCompilePragma extends PragmaException {
}
