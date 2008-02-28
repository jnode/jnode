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
 * Methods that have this exception in their throws list, will not trigger yieldpoints.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UninterruptiblePragma extends PragmaException {

}
