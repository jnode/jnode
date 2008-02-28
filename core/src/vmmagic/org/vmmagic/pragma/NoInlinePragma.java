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
 * Do not inline method that can throw this exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @deprecated Use NoInline annotation instead.
 */
public class NoInlinePragma extends PragmaException {

}
