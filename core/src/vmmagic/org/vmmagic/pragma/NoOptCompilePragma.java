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
 * This pragma indicates that a particular method should never be 
 * compiled by the optimizing compiler. It also implies that the
 * method will never be inlined by the optimizing compiler.
 * 
 * @author Dave Grove
 */
public class NoOptCompilePragma extends PragmaException {
}
