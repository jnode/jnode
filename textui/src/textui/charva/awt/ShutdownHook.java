/* class ShutdownHook
 *
 * Copyright (C) 2001  R M Pitman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package charva.awt;

/**
 * This class is used to clean up the ncurses environment and restore the
 * terminal settings when the application terminates, whether the program
 * exits intentionally or because of a signal such as SIGTERM, SIGHUP etc.
 */
public class ShutdownHook
    implements Runnable
{
    /** The constructor (which does nothing) is package-private because 
     * it is not intended to be called by application programs.
     */
    ShutdownHook() {
    }

    /** Implement the Runnable interface; shuts down the ncurses environment
     * and restores the previous terminal settings.
     */
    public void run() {
	Toolkit.getDefaultToolkit().close();
    }
}
