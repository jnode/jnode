/*
 * $Id$
 */
package org.jnode.system;

import org.jnode.vm.VmAddress;

/**
 * Interface for searching data in a memory region.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface MemoryScanner {
    
    /**
     * Find a given 8-bit int (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     * @param start The address where to start searching.
     * @param size The length of the region to search (in bytes)
     * @param match The value to search for
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public VmAddress findInt8(VmAddress start, int size, int match, int stepSize);

    /**
     * Find a given 16-bit int (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     * @param start The address where to start searching.
     * @param size The length of the region to search (in bytes)
     * @param match The value to search for
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public VmAddress findInt16(VmAddress start, int size, int match, int stepSize);

    /**
     * Find a given 32-bit int (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     * @param start The address where to start searching.
     * @param size The length of the region to search (in bytes)
     * @param match The value to search for
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public VmAddress findInt32(VmAddress start, int size, int match, int stepSize);
    
    /**
     * Find a given 64-bit int (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     * @param start The address where to start searching.
     * @param size The length of the region to search (in bytes)
     * @param match The value to search for
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public VmAddress findInt64(VmAddress start, int size, long match, int stepSize);

    /**
     * Find a given 8-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     * @param start The address where to start searching.
     * @param size The length of the region to search (in bytes)
     * @param match The value to search for
     * @param matchOffset 
     * @param matchLength 
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public VmAddress findInt8Array(VmAddress start, int size, byte[] match, int matchOffset, int matchLength, int stepSize);

    /**
     * Find a given 16-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     * @param start The address where to start searching.
     * @param size The length of the region to search (in bytes)
     * @param match The value to search for
     * @param matchOffset 
     * @param matchLength 
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public VmAddress findInt16Array(VmAddress start, int size, char[] match, int matchOffset, int matchLength, int stepSize);
    
    /**
     * Find a given 16-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     * @param start The address where to start searching.
     * @param size The length of the region to search (in bytes)
     * @param match The value to search for
     * @param matchOffset 
     * @param matchLength 
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public VmAddress findInt16Array(VmAddress start, int size, short[] match, int matchOffset, int matchLength, int stepSize);
    
    /**
     * Find a given 32-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     * @param start The address where to start searching.
     * @param size The length of the region to search (in bytes)
     * @param match The value to search for
     * @param matchOffset 
     * @param matchLength 
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public VmAddress findInt32Array(VmAddress start, int size, int[] match, int matchOffset, int matchLength, int stepSize);
    
    /**
     * Find a given 64-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     * @param start The address where to start searching.
     * @param size The length of the region to search (in bytes)
     * @param match The value to search for
     * @param matchOffset 
     * @param matchLength 
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public VmAddress findInt64Array(VmAddress start, int size, long[] match, int matchOffset, int matchLength, int stepSize);
}
