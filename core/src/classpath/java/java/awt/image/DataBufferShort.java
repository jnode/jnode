/*
 * $Id$
 */
package java.awt.image;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DataBufferShort extends DataBuffer {
	private short[] data;
	private short[][] bankData;

	public DataBufferShort(int size) {
		super(TYPE_SHORT, size);
		data = new short[size];
	}

	public DataBufferShort(int size, int numBanks) {
		super(TYPE_USHORT, size, numBanks);
		bankData = new short[numBanks][size];
		data = bankData[0];
	}

	public DataBufferShort(short[] dataArray, int size) {
		super(TYPE_SHORT, size);
		data = dataArray;
	}

	public DataBufferShort(short[] dataArray, int size, int offset) {
		super(TYPE_SHORT, size, 1, offset);
		data = dataArray;
	}

	public DataBufferShort(short[][] dataArray, int size) {
		super(TYPE_SHORT, size, dataArray.length);
		bankData = dataArray;
		data = bankData[0];
	}

	public DataBufferShort(short[][] dataArray, int size, int[] offsets) {
		super(TYPE_SHORT, size, dataArray.length, offsets);
		bankData = dataArray;
		data = bankData[0];
	}

	public short[] getData() {
		return data;
	}

	public short[] getData(int bank) {
		return bankData[bank];
	}

	public short[][] getBankData() {
		return bankData;
	}

	public int getElem(int i) {
		return data[i + offset] & 0xffff; // get unsigned short as int
	}

	public int getElem(int bank, int i) {
		// get unsigned short as int
		return bankData[bank][i + offsets[bank]] & 0xffff;
	}

	public void setElem(int i, int val) {
		data[i + offset] = (short) val;
	}

	public void setElem(int bank, int i, int val) {
		bankData[bank][i + offsets[bank]] = (short) val;
	}
}
