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
 * To be commented.
 *
 * @author Daniel Frampton
 * @see Address
 */
public final class Word {

  /**
   * @deprecated
   */
  public static Word fromInt (int val) {
    return null;
  }

  public static Word fromIntSignExtend (int val) {
    return null;
  }
  
  public static Word fromIntZeroExtend (int val) {
    return null;
  }

  public static Word zero () {
    return null;
  }

  public static Word one () {
    return null;
  }

  public static Word max() {
    return null;
  }

  public int toInt () {
    return 0;
  }

  public long toLong () {
    return 0L;
  }

  public Address toAddress() {
    return null;
  }

  public Offset toOffset () {
    return null;
  }

  public Extent toExtent () {
    return null;
  }

  public Word add (int w2) {
    return null;
  }

  public Word add (Word w2) {
    return null;
  }

  public Word add (Offset w2) {
    return null;
  }

  public Word add (Extent w2) {
    return null;
  }

  public Word sub (int w2) {
    return null;
  }

  public Word sub (Word w2) {
    return null;
  }

  public Word sub (Offset w2) {
    return null;
  }

  public Word sub (Extent w2) {
    return null;
  }

  public boolean isZero() {
    return false;
  }

  public boolean isMax() {
    return false;
  }

  public boolean LT (Word addr2) {
    return false;
  }

  public boolean LE (Word w2) {
    return false;
  }

  public boolean GT (Word w2) {
    return false;
  }

  public boolean GE (Word w2) {
    return false;
  }

  public boolean EQ (Word w2) {
    return false;
  }

  public boolean NE (Word w2) {
    return false;
  }

  public Word and(Word w2) {
    return null;
  }

  public Word or(Word w2) {
    return null;
  }

  public Word not() {
    return null;
  }

  public Word xor(Word w2) {
    return null;
  }

  public Word lsh (int amt) {
    return null;
  }

  public Word rshl (int amt) {
    return null;
  }

  public Word rsha (int amt) {
    return null;
  }

}

