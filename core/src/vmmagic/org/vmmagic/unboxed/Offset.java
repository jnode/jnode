/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.vmmagic.unboxed;

/**
 * To be commented
 *
 * @author Daniel Frampton
 */
public final class Offset {

  /**
   * @deprecated
   */
  public static Offset fromInt(int address) {
    return null;
  }

  public static Offset fromIntSignExtend(int address) {
    return null;
  }

  public static Offset fromIntZeroExtend(int address) {
    return null;
  }

  public static Offset zero () {
    return null;
  }

  public static Offset max() {
    return null;
  }

  public int toInt () {
    return 0;
  }

  public long toLong () {
    return 0L;
  }

  public Word toWord() {
    return null;
  }

  public Offset add (int byteSize) {
    return null;
  }

  public Offset add (Extent byteSize) {
    return null;
  }

  public Offset sub (int byteSize) {
    return null;
  }

  public Offset sub (Offset off2) {
    return null;
  }

  public boolean EQ (Offset off2) {
    return false;
  }

  public boolean NE (Offset off2) {
    return false;
  }

  public boolean sLT (Offset off2) {
    return false;
  }

  public boolean sLE (Offset off2) {
    return false;
  }

  public boolean sGT (Offset off2) {
    return false;
  }

  public boolean sGE (Offset off2) {
    return false;
  }

  public boolean isZero() {
    return false;
  }

  public boolean isMax() {
    return false;
  }
}

