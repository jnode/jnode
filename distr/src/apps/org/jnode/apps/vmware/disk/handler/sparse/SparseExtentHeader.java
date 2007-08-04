package org.jnode.apps.vmware.disk.handler.sparse;


import org.apache.log4j.Logger;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class SparseExtentHeader 
{
	static final Logger LOG = Logger.getLogger(SparseExtentHeader.class);
		
	// flags
	private boolean validNewLineDetectionTest;
	private boolean redundantGrainTableWillBeUsed;
	
	private long capacity;
	private long grainSize;
	private long descriptorOffset;
	private long descriptorSize;
	private long rgdOffset;
	
	// additional/computed fields
	private long grainTableCoverage;
	
	private int numGTEsPerGT = 512;
	
	private long gdOffset;
	private long overHead;
	private boolean uncleanShutdown;

	public int getNumGTEsPerGT() {
		return numGTEsPerGT;
	}

	public long getCapacity() {
		return capacity;
	}
	
	/**
	 * 
	 * @return gtCoverage (grain table coverage) in sectors
	 */
	public long getGrainTableCoverage()
	{
		return grainTableCoverage;
	}

	public long getGrainSize() {
		return grainSize;
	}

	public long getDescriptorOffset() 
	{
		return descriptorOffset;
	}

	public boolean isValidNewLineDetectionTest() {
		return validNewLineDetectionTest;
	}

	public void setValidNewLineDetectionTest(boolean validNewLineDetectionTest) {
		this.validNewLineDetectionTest = validNewLineDetectionTest;
	}

	public boolean isRedundantGrainTableWillBeUsed() {
		return redundantGrainTableWillBeUsed;
	}

	public void setRedundantGrainTableWillBeUsed(
			boolean redundantGrainTableWillBeUsed) {
		this.redundantGrainTableWillBeUsed = redundantGrainTableWillBeUsed;
	}

	public long getDescriptorSize() {
		return descriptorSize;
	}

	public void setDescriptorSize(long descriptorSize) {
		this.descriptorSize = descriptorSize;
	}

	public long getRgdOffset() {
		return rgdOffset;
	}

	public void setRgdOffset(long rgdOffset) {
		this.rgdOffset = rgdOffset;
	}

	public long getGdOffset() {
		return gdOffset;
	}

	public void setGdOffset(long gdOffset) {
		this.gdOffset = gdOffset;
	}

	public long getOverHead() {
		return overHead;
	}

	public void setOverHead(long overHead) {
		this.overHead = overHead;
	}

	public boolean isUncleanShutdown() {
		return uncleanShutdown;
	}

	public void setUncleanShutdown(boolean uncleanShutdown) {
		this.uncleanShutdown = uncleanShutdown;
	}

	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}

	public void setGrainSize(long grainSize) {
		this.grainSize = grainSize;
	}

	public void setDescriptorOffset(long descriptorOffset) {
		this.descriptorOffset = descriptorOffset;
	}

	public void setGrainTableCoverage(long grainTableCoverage) {
		this.grainTableCoverage = grainTableCoverage;
	}

	public void setNumGTEsPerGT(int numGTEsPerGT) {
		this.numGTEsPerGT = numGTEsPerGT;
	}
	
	
	
	@Override
	public String toString() 
	{
		// flags
		return "SparseExtentHeader[validNewLineDetectionTest="+validNewLineDetectionTest+
		", redundantGrainTableWillBeUsed="+redundantGrainTableWillBeUsed+		
		", capacity="+capacity+
		", grainSize="+grainSize+
		", descriptorOffset="+descriptorOffset+
		", descriptorSize="+descriptorSize+
		", rgdOffset="+rgdOffset+
		", grainTableCoverage="+grainTableCoverage+
		", numGTEsPerGT="+numGTEsPerGT+
		", gdOffset="+gdOffset+
		", overHead="+overHead+
		", uncleanShutdown="+uncleanShutdown+"]";
	}
}
