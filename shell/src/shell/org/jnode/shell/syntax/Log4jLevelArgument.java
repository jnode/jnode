/*
 * $Id: Log4jCommand.java 4053 2008-05-04 13:57:57Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 package org.jnode.shell.syntax;

import java.util.HashMap;

import org.apache.log4j.Level;

/**
 * This Argument class accepts and completes (case insensitive) log4j logging levels.
 * 
 * @author crawley@jnode.org
 */
public class Log4jLevelArgument extends MappedArgument<Level> {
    private static HashMap<String, Level> LEVEL_MAP = new HashMap<String, Level>();
    
    static {
        LEVEL_MAP.put("all", Level.ALL);
        // LEVEL_MAP.put("trace", Level.TRACE);  // introduce in log4j 1.2.12
        LEVEL_MAP.put("debug", Level.DEBUG);
        LEVEL_MAP.put("info", Level.INFO);
        LEVEL_MAP.put("warn", Level.WARN);
        LEVEL_MAP.put("error", Level.ERROR);
        LEVEL_MAP.put("fatal", Level.FATAL);
        LEVEL_MAP.put("off", Level.OFF);
    }

    public Log4jLevelArgument(String label, int flags, String description) {
        super(label, flags, new Level[0], LEVEL_MAP, true, description);
    }

    @Override
    protected String argumentKind() {
        return "logging level";
    }

}
