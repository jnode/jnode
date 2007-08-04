/*
 * $Id$
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
 
package org.jnode.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import org.apache.tools.ant.Project;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.Geometry;
import org.jnode.driver.block.MappedFSBlockDeviceSupport;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.fat.Fat;
import org.jnode.fs.fat.GrubBootSector;
import org.jnode.fs.fat.GrubFatFormatter;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BootDiskBuilder extends BootFloppyBuilder {

    private File plnFile;

    int bytesPerSector = 512;

    int spc = 1;

    private Geometry geom = new Geometry(64, 16, 32);

    private MappedFSBlockDeviceSupport part0;

    public BootDiskBuilder() {
    }

    /**
     * Create the actual bootfloppy
     * 
     * @throws IOException
     * @throws DriverException
     * @throws FileSystemException
     */
    public void createImage() throws IOException, DriverException,
            FileSystemException {
        super.createImage();

        FileWriter fw = new FileWriter(plnFile);
        PrintWriter pw = new PrintWriter(fw);
        pw.println("DRIVETYPE ide");
        pw.println("CYLINDERS " + geom.getCylinders());
        pw.println("HEADS     " + geom.getHeads());
        pw.println("SECTORS   " + geom.getSectors());
        pw.println("CAPACITY  " + geom.getTotalSectors());
        pw.println("ACCESS    \"" + getDestFile().getCanonicalPath()
                + "\" 0 102400");
        pw.flush();
        fw.flush();
        pw.close();
        fw.close();
        System.out.println("Wrote " + plnFile);
    }

    /**
     * Format the given device
     * 
     * @param device
     * @throws IOException
     */
    protected void formatDevice(Device device) throws IOException {

        /* Format the MBR & partitiontable */
        GrubBootSector mbr = (GrubBootSector) (createFormatter()
                .getBootSector());

        mbr.getPartition(0).clear();
        mbr.getPartition(1).clear();
        mbr.getPartition(2).clear();
        mbr.getPartition(3).clear();

        IBMPartitionTableEntry pte = mbr.getPartition(0);
        pte.setBootIndicator(true);
        pte.setStartLba(1);
        pte.setNrSectors(geom.getTotalSectors() - 1);
        pte.setSystemIndicator(IBMPartitionTypes.PARTTYPE_DOS_FAT16_LT32M);
        pte.setStartCHS(geom.getCHS(pte.getStartLba()));
        pte.setEndCHS(geom.getCHS(pte.getStartLba() + pte.getNrSectors() - 1));

        /*
         * System.out.println("partition table:"); for (int i = 0; i < 4; i++) {
         * System.out.println("" + i + " " + mbr.getPartition(i)); }
         */

        /* Format partition 0 */
        part0 = new MappedFSBlockDeviceSupport(device, pte.getStartLba()
                * bytesPerSector, pte.getNrSectors() * bytesPerSector);
        GrubFatFormatter ff = createFormatter();
        ff.setInstallPartition(0x0000FFFF);
        ff.format(part0);
        GrubBootSector part0bs = (GrubBootSector) ff.getBootSector();

        /* Fixup stage2 sector in MBR */
        mbr.setStage2Sector(pte.getStartLba() + part0bs.getStage2Sector());
        try {
            mbr.write(device.getAPI(BlockDeviceAPI.class));
        } catch (ApiNotFoundException ex) {
			final IOException ioe = new IOException("BlockDeviceAPI not found on device");
			ioe.initCause(ex);
			throw ioe;
        }
        //System.out.println("mbr stage2 sector=" + mbr.getStage2Sector());
    }

    /**
     * @see org.jnode.build.BootFloppyBuilder#createFormatter()
     * @return The formatter
     * @throws IOException
     */
    protected GrubFatFormatter createFormatter() throws IOException {
        return new GrubFatFormatter(bytesPerSector, spc, geom, Fat.FAT16, 1,
                getStage1ResourceName(), getStage2ResourceName());
    }

    /**
     * @see org.jnode.build.BootFloppyBuilder#getDeviceLength()
     * @return The device length
     */
    protected long getDeviceLength() {
        return geom.getTotalSectors() * bytesPerSector;
    }

    /**
     * @return File
     */
    public File getPlnFile() {
        return plnFile;
    }

    /**
     * Sets the plnFile.
     * 
     * @param plnFile
     *            The plnFile to set
     */
    public void setPlnFile(File plnFile) {
        this.plnFile = plnFile;
    }

    /**
     * @param rootDevice
     * @see org.jnode.build.BootFloppyBuilder#getSystemDevice(Device)
     * @return The device
     */
    protected Device getSystemDevice(Device rootDevice) {
        return part0;
    }

    /**
     * Used by ant to set the geometry property.
     * 
     * @param geometryString String in the format ' <cylinder>/ <heads>/ <sectors>', e.\u00f6g.
     *            '64/16/32'.
     */
    public void setGeometry(String geometryString) {
        try {
            log("Setting bootdisk geometry to " + geometryString, Project.MSG_VERBOSE);
            StringTokenizer tokenizer = new StringTokenizer(geometryString, "/");
            geom = new Geometry(Integer.parseInt(tokenizer.nextToken()),
                    Integer.parseInt(tokenizer.nextToken()), Integer
                            .parseInt(tokenizer.nextToken()));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid geometry "
                            + geometryString
                            + ". Must correspond to pattern '<cylinders>/<heads>/<sectors>' e.g. '64/16/32'.");
        }
    }
}
