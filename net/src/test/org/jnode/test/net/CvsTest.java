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
