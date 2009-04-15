/*
 * $Id: UptimeCommand.java 4977 2009-02-02 09:09:41Z lsantha $
 *
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jnode.shell.command;

import java.io.PrintWriter;
import org.jnode.shell.AbstractCommand;
import org.jnode.vm.VmSystem;

/**
 * @author Petri Airio (petriai@gmailcom)
 */
public class UptimeCommand extends AbstractCommand {

    public static void main(String[] args) throws Exception {
        new UptimeCommand().execute(args);
    }

    public UptimeCommand() {
        super("shows system uptime");
    }

    /**
     * @throws Exception if something happens
     */
    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        long ut_secs = VmSystem.currentKernelMillis() / 1000;

        int dd = (int) ut_secs / (60 * 60 * 24);
        int hh = (int) (ut_secs / (60 * 60)) - (dd * 24);
        int mm = (int) (ut_secs / 60) - (dd * 1440) - (hh * 60);
        int ss = (int) ut_secs - (dd * 24 * 60 * 60) - (hh * 60 * 60) - (mm * 60);

        String sss = "";
        if (dd > 1) {
            sss = "s";
        }
        out.format(" up %d day%s, %02d:%02d:%02d\n", dd, sss, hh, mm, ss);
    }
}
