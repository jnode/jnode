/*
 * $Id$
 */

package org.jnode.driver.input;

import java.util.Arrays;

/**
 * @author Martin Husted Hartvig
 * @since 0.1.6
 */


public class Keys
{
  private Key[] keys = new Key[128];

  public Keys()
  {
  }

  public Key getKey(int _scancode)
  {
    Key key = keys[_scancode];

    if (key == null)
    {
      key = new Key();
      keys[_scancode] = key;
    }
    return key;
  }


  public void setKey(int _scancode, Key key)
  {
    keys[_scancode] = key;
  }

  public String toString()
  {
    return "Keys{" +
        "keys=" + (keys == null ? null : Arrays.asList(keys)) +
        "}";
  }

  public static void main(String[] args)
  {
    Keys keys = new Keys();

    System.out.println(keys);

    System.out.println(keys.getKey(1));
    System.out.println(keys);
  }
}
