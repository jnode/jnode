/* class PlaybackThread
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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * This class reads a scriptfile line by line, parses each line 
 * into a keystroke-specifier and a time-interval, and fires the
 * specified keystroke after the specified delay.
 */
public class PlaybackThread
    extends Thread
{
    PlaybackThread(BufferedReader scriptReader_) {
	_scriptReader = scriptReader_;
	_toolkit = Toolkit.getDefaultToolkit();
    }

    public void run() {
	String line;

	try {
	    while ((line = _scriptReader.readLine()) != null) {
		StringTokenizer st = new StringTokenizer(line);
		String keycodeToken = st.nextToken();
		int keycode = Integer.parseInt(keycodeToken, 16);

		String delayToken = st.nextToken();
		long delay = Long.parseLong(delayToken);

		if (delay != 0) {
		    try {
			Thread.sleep(delay);
		    }
		    catch (InterruptedException ei) {}
		}

		_toolkit.fireKeystroke(keycode);
	    }
	}
	catch (IOException ei) {
	    System.err.println("while reading script file: " +
		    ei.getMessage());
	}
    }

    //====================================================================
    // INSTANCE VARIABLES

    private BufferedReader _scriptReader;

    private Toolkit _toolkit;
}
