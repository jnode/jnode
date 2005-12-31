/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package org.jnode.test.net;

/**
 * Created by IntelliJ IDEA.
 * User: mh
 * Date: 23-05-2004
 * Time: 21:45:15
 * To change this template use File | Settings | File Templates.
 */

import com.jcraft.jsch.JSch;

import javax.crypto.NullCipher;

public class CvsTest
{

  public static void main(String[] args)
  {
    NullCipher nullCipher = new NullCipher();
    System.out.println(nullCipher);


    JSch jsch = new JSch();
    System.out.println(jsch);
  }
}
