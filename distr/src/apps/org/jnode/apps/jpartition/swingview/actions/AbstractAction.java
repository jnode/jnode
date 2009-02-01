/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.apps.jpartition.swingview.actions;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Bounded;
import org.jnode.apps.jpartition.swingview.DiskAreaView;

abstract class AbstractAction<T extends DiskAreaView<? extends Bounded>> extends
        javax.swing.AbstractAction {
    private static final long serialVersionUID = -8091888570743940797L;

    protected final Logger log = Logger.getLogger(getClass());

    protected final ErrorReporter errorReporter;
    protected final T view;

    public AbstractAction(String name, ErrorReporter errorReporter, T view) {
        super(name);
        this.errorReporter = errorReporter;
        this.view = view;
    }
}
