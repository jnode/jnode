/* PreferencesFactory - Preferences system and user root factory interface
   Copyright (C) 2001 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.util.prefs;

/**
 * Preferences system and user root factory interface. Defines how to get
 * to the system and user root preferences objects. Should be implemented by
 * new preferences backends.
 *
 * @since 1.4
 * @author Mark Wielaard (mark@klomp.org)
 */
public interface PreferencesFactory {

	/**
	 * Returns the system root preferences node. Should always return the
	 * same object.
	 */
	Preferences systemRoot();

	/**
	 * Returns the user root preferences node. May return different objects
	 * depending on the user that called this method. The user may for example
	 * be determined by the current Thread or the Subject associated with the
	 * current AccessControllContext.
	 */
	Preferences userRoot();

}
