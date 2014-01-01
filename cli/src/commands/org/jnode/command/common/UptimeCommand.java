/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.command.common;

import java.io.PrintWriter;
import org.jnode.shell.AbstractCommand;
import org.jnode.vm.VmSystem;

/**
 * Show the system uptime.
 * @author Petri Airio (petriai@gmailcom)
 */
public class UptimeCommand extends AbstractCommand {

    private static final String help_super = "Show the system uptime";
    private static final String fmt_time   = " up %d day%s, %02d:%02d:%02d%n";
    
    public static void main(String[] args) throws Exception {
        new UptimeCommand().execute(args);
    }

    public UptimeCommand() {
        super(help_super);
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
        out.format(fmt_time, dd, sss, hh, mm, ss);
    }
}
