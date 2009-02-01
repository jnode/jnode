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
 
package org.jnode.apps.jpartition.swingview;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.apps.jpartition.swingview.actions.AddPartitionAction;
import org.jnode.apps.jpartition.swingview.actions.FormatPartitionAction;
import org.jnode.apps.jpartition.swingview.actions.RemovePartitionAction;
import org.jnode.util.NumberUtils;

public class PartitionView extends DiskAreaView<Partition> {
    private final DeviceView deviceView;

    private PartitionState state = new IdleState();

    public PartitionView(ErrorReporter errorReporter, DeviceView deviceView, Partition partition) {
        super(errorReporter);
        this.deviceView = deviceView;
        this.bounded = partition;
        update();

        if (partition.isUsed()) {
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int cursor = Cursor.DEFAULT_CURSOR;
                    state = new IdleState();

                    /*
                     if(e.getX() < borderWidth) { 
                         cursor = Cursor.W_RESIZE_CURSOR; 
                     } else
                     */
                    if (e.getX() >= (getWidth() - borderWidth)) {
                        cursor = Cursor.W_RESIZE_CURSOR;
                        state = new ResizeState();
                    }
                    // else
                    // {
                    // cursor = Cursor.MOVE_CURSOR;
                    // }
                    setCursor(Cursor.getPredefinedCursor(cursor));
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    state.mouseDragged(PartitionView.this, e);
                }
            });
        }
    }

    protected Color getColor(Partition partition) {
        Color color = Constants.UNALLOCATED;
        if ((partition != null) && partition.isUsed()) {
            if ("JFAT".equals(partition.getFormat())) {
                color = Constants.FAT32;
            } else if ("FAT".equals(partition.getFormat())) {
                color = Constants.FAT16;
            } else if ("EXT2".equals(partition.getFormat())) {
                color = Constants.EXT2;
            } else if ("NTFS".equals(partition.getFormat())) {
                color = Constants.NTFS;
            }
        }

        return color;
    }

    public void update() {
        super.update();

        if (bounded != null) {
            setInfos(bounded.getFormat() + " " + NumberUtils.toBinaryByte(bounded.getSize()));

            Color color = getColor(bounded);
            setBackground(color);

            if (!bounded.isUsed()) {
                setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }

    public final Partition getPartition() {
        return bounded;
    }

    @Override
    protected Action[] getActions() {
        Action[] actions;
        if (bounded.isUsed()) {
            actions =
                    new Action[] {new FormatPartitionAction(errorReporter, this),
                        new RemovePartitionAction(errorReporter, this), };
        } else {
            actions = new Action[] {new AddPartitionAction(errorReporter, this), };
        }
        return actions;
    }

    public DeviceView getDeviceView() {
        return deviceView;
    }

    private abstract static class PartitionState {

        protected abstract void mouseDragged(PartitionView partitionView, MouseEvent e);
    }

    private static class IdleState extends PartitionState {
        @Override
        protected void mouseDragged(PartitionView partitionView, MouseEvent e) {
            // do nothing
        }
    }

    private static class ResizeState extends PartitionState {
        @Override
        protected void mouseDragged(PartitionView partitionView, MouseEvent e) {
            long size = (long) ((double) e.getX() / partitionView.pixelsPerByte);
            try {
                UserFacade.getInstance()
                        .resizePartition(partitionView.bounded.getStart() + 1, size);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
}
