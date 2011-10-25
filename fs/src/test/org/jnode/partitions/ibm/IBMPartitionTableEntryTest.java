package org.jnode.partitions.ibm;

import static org.junit.Assert.*;

import org.jnode.util.LittleEndian;
import org.junit.Before;
import org.junit.Test;

public class IBMPartitionTableEntryTest {

    @Test
    public void testhasChildPartitionTable(){
        byte[] bootSector = getBootSector();
        LittleEndian.setInt8(bootSector, 450, 0x85);
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null,bootSector,0);
        assertTrue(pte.hasChildPartitionTable());
    }
    
    @Test
    public void testhasNoChildPartitionTable(){
        byte[] bootSector = getBootSector();
        LittleEndian.setInt8(bootSector, 450, 0x84);
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null,bootSector,0);
        assertFalse(pte.hasChildPartitionTable());
    }
    
    @Test
    public void testIsValid() {
        byte[] bootSector = getBootSector();
        LittleEndian.setInt8(bootSector, 450, 0x85);
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null,bootSector,0);
        assertTrue(pte.isValid());
    }
    
    @Test
    public void testIsNotValidEmptyBootSector() {
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null,getBootSector(),0);
        assertFalse(pte.isValid());
    }

    @Test
    public void testIsEmpty() {
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null,getBootSector(),0);
        assertTrue(pte.isEmpty());
    }

    private byte[] getBootSector(){
        byte[] bs = new byte[500];
        for (int i = 0; i < bs.length; i++) {
            bs[i] = 0;
        }
        return bs;
    }
    
}
