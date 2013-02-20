/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.jnode.util.LittleEndian;

/**
 * @author Daniel Noll (daniel@noll.id.au)
 */
public final class CompressedDataRun implements DataRunInterface {
	/**
	 * Size of a compressed block in NTFS. This is always the same even if the cluster size is not 4k.
	 */
	private static final int BLOCK_SIZE = 0x1000;

	/**
	 * Logger.
	 */
	private static final Logger log = Logger.getLogger(CompressedDataRun.class);

	/**
	 * The underlying data run containing the compressed data.
	 */
	private final DataRun compressedRun;

	/**
	 * The number of clusters which make up a compression unit.
	 */
	private final int compressionUnitSize;

	/**
	 * Constructs a compressed run which when read, will decrypt data found in the provided data run.
	 * @param compressedRun the compressed data run.
	 * @param compressionUnitSize the number of clusters which make up a compression unit.
	 */
	public CompressedDataRun(DataRun compressedRun, int compressionUnitSize) {
		this.compressedRun = compressedRun;
		this.compressionUnitSize = compressionUnitSize;
	}

	/**
	 * Gets the length of the data run in clusters.
	 * @return the length of the run in clusters.
	 */
	public int getLength() {
		return compressionUnitSize;
	}

	/**
	 * Reads clusters from this datarun.
	 * @param vcn the VCN to read, offset from the start of the entire file.
	 * @param dst destination buffer.
	 * @param dstOffset offset into destination buffer.
	 * @param nrClusters number of clusters to read.
	 * @param clusterSize size of each cluster.
	 * @param volume reference to the NTFS volume structure.
	 * @return the number of clusters read.
	 * @throws IOException if an error occurs reading.
	 */
	public int readClusters(long vcn, byte[] dst, int dstOffset, int nrClusters, int clusterSize, NTFSVolume volume)
			throws IOException {

		// Logic to determine whether we own the VCN which has been requested.
		// XXX: Lifted from DataRun. Consider moving to some good common location.
		final long myFirstVcn = compressedRun.getFirstVcn();
		final int myLength = getLength();
		final long myLastVcn = myFirstVcn + myLength - 1;
		final long reqLastVcn = vcn + nrClusters - 1;
		log.debug("me:" + myFirstVcn + "-" + myLastVcn + ", req:" + vcn + "-" + reqLastVcn);
		if ((vcn > myLastVcn) || (myFirstVcn > reqLastVcn)) {
			// Not my region
			return 0;
		}

		// Now we know it's in our data run, here's the actual fragment to read.
		final long actFirstVcn = Math.max(myFirstVcn, vcn);
		final int actLength = (int) (Math.min(myLastVcn, reqLastVcn) - actFirstVcn + 1);

		// This is the actual number of stored clusters after compression.
		// If the number of stored clusters is the same as the compression unit size,
		// then the data can be read directly without decompressing it.
		final int compClusters = compressedRun.getLength();
		if (compClusters == compressionUnitSize) {
			return compressedRun.readClusters(vcn, dst, dstOffset, compClusters, clusterSize, volume);
		}

		// Now we know the data is compressed. Read in the compressed block...
		final int vcnOffsetWithinUnit = (int) (actFirstVcn % compressionUnitSize);
		final long compFirstVcn = actFirstVcn - vcnOffsetWithinUnit;
		final byte[] tempCompressed = new byte[compClusters * clusterSize];
		final int read = compressedRun.readClusters(compFirstVcn, tempCompressed, 0, compClusters, clusterSize, volume);
		if (read != compClusters) {
			throw new IOException("Needed " + compClusters + " clusters but could " + "only read " + read);
		}

		// Uncompress it, and copy into the destination.
		final byte[] tempUncompressed = new byte[compressionUnitSize * clusterSize];
		// XXX: We could potentially reduce the overhead by modifying the compression
		// routine such that it's capable of skipping chunks that aren't needed.
		unCompressUnit(tempCompressed, tempUncompressed);

		System.arraycopy(tempUncompressed, vcnOffsetWithinUnit * clusterSize, dst, dstOffset
				+ (int) (actFirstVcn - vcn) * clusterSize, actLength * clusterSize);

		return actLength;
	}

	/**
	 * Uncompresses a single unit of multiple compressed blocks.
	 * @param compressed the compressed data (in.)
	 * @param uncompressed the uncompressed data (out.)
	 * @throws IOException if the decompression fails.
	 */
	private static void unCompressUnit(final byte[] compressed, final byte[] uncompressed) throws IOException {

		// This is just a convenient way to simulate the original code's pointer arithmetic.
		// I tried using buffers but positions in those are always from the beginning and
		// I had to also maintain a position from the start of the current block.
		final OffsetByteArray compressedData = new OffsetByteArray(compressed);
		final OffsetByteArray uncompressedData = new OffsetByteArray(uncompressed);

		for(int i = 0; i * BLOCK_SIZE < uncompressed.length; i++) {
			final int consumed = uncompressBlock(compressedData, uncompressedData);

			// Apple's code had this as an error but to me it looks like this simply
			// terminates the sequence of compressed blocks.
			if (consumed == 0) {
				// At the current point in time this is already zero but if the code
				// changes in the future to reuse the temp buffer, this is a good idea.
				uncompressedData.zero(0, uncompressed.length - uncompressedData.offset);
				break;
			}

			compressedData.offset += consumed;
			uncompressedData.offset += BLOCK_SIZE;
		}
	}

	/**
	 * Uncompresses a single block.
	 * @param compressed the compressed buffer (in.)
	 * @param uncompressed the uncompressed buffer (out.)
	 * @return the number of bytes consumed from the compressed buffer.
	 */
	private static int uncompressBlock(final OffsetByteArray compressed, final OffsetByteArray uncompressed) {

		int pos = 0, cpos = 0;

		final int rawLen = compressed.getShort(cpos);
		cpos += 2;
		final int len = rawLen & 0xFFF;
		log.debug("ntfs_uncompblock: block length: " + len + " + 3, 0x" + Integer.toHexString(len) + ",0x"
				+ Integer.toHexString(rawLen));

		if (rawLen == 0) {
			// End of sequence, rest is zero. For some reason there is nothing
			// of the sort documented in the Linux kernel's description of compression.
			return 0;
		}

		if ((rawLen & 0x8000) == 0) {
			// Uncompressed chunks store length as 0xFFF always.
			if ((len + 1) != BLOCK_SIZE) {
				log.debug("ntfs_uncompblock: len: " + len + " instead of 0xfff");
			}

			// Copies the entire compression block as-is, need to skip the compression flag,
			// no idea why they even stored it given that it isn't used.
			// Darwin's version I was referring to doesn't skip this, which seems be a bug.
			cpos++;
			uncompressed.copyFrom(compressed, cpos, 0, len + 1);
			uncompressed.zero(len + 1, BLOCK_SIZE - 1 - len);
			return len + 3;
		}

		while (cpos < len + 3 && pos < BLOCK_SIZE) {
			byte ctag = compressed.get(cpos++);
			for(int i = 0; i < 8 && pos < BLOCK_SIZE; i++) {
				if ((ctag & 1) != 0) {
					int j, lmask, dshift;
					for(j = pos - 1, lmask = 0xFFF, dshift = 12; j >= 0x10; j >>= 1) {
						dshift--;
						lmask >>= 1;
					}
					final int tmp = compressed.getShort(cpos);
					cpos += 2;
					final int boff = -1 - (tmp >> dshift);
					final int blen = Math.min(3 + (tmp & lmask), BLOCK_SIZE - pos);

					// Note that boff is negative.
					uncompressed.copyFrom(uncompressed, pos + boff, pos, blen);
					pos += blen;
				} else {
					uncompressed.put(pos++, compressed.get(cpos++));
				}
				ctag >>= 1;
			}
		}

		return len + 3;
	}

	@Override
	public long mapVcnToLcn(long vcn) {
        // This is the actual number of stored clusters after compression.
        // If the number of stored clusters is the same as the compression unit size,
        // then the data can be read directly without decompressing it.
        final int compClusters = compressedRun.getLength();
        if (compClusters == compressionUnitSize) {
            return compressedRun.mapVcnToLcn(vcn);
        }

        // Now we know the data is compressed. Map the VCN to the corresponding compressed block...
        final long actFirstVcn = Math.max(compressedRun.getFirstVcn(), vcn);
        final int vcnOffsetWithinUnit = (int) (actFirstVcn % compressionUnitSize);
        final long compFirstVcn = actFirstVcn - vcnOffsetWithinUnit;
        return compressedRun.mapVcnToLcn(compFirstVcn);
	}

	/**
	 * Convenience class wrapping an array with its offset. An alternative to pointer arithmetic without going to the
	 * level of using an NIO buffer.
	 */
	private static class OffsetByteArray {

		/**
		 * The contained array.
		 */
		private final byte[] array;

		/**
		 * The current offset.
		 */
		private int offset;

		/**
		 * Constructs the offset byte array. The offset begins at zero.
		 * @param array the contained array.
		 */
		private OffsetByteArray(final byte[] array) {
			this.array = array;
		}

		/**
		 * Gets a single byte from the array.
		 * @param offset the offset from the contained offset.
		 * @return the byte.
		 */
		private byte get(int offset) {
			return array[this.offset + offset];
		}

		/**
		 * Puts a single byte into the array.
		 * @param offset the offset from the contained offset.
		 * @param value the byte.
		 */
		private void put(int offset, byte value) {
			array[this.offset + offset] = value;
		}

		/**
		 * Gets a 16-bit little-endian value from the array.
		 * @param offset the offset from the contained offset.
		 * @return the short.
		 */
		private int getShort(int offset) {
			return LittleEndian.getUInt16(array, this.offset + offset);
		}

		/**
		 * Copies a slice from the provided array into our own array. Uses {@code System.arraycopy} where possible; if
		 * the slices overlap, copies one byte at a time to avoid a problem with using {@code System.arraycopy} in this
		 * situation.
		 * @param src the source offset byte array.
		 * @param srcOffset offset from the source array's offset.
		 * @param destOffset offset from our own offset.
		 * @param length the number of bytes to copy.
		 */
		private void copyFrom(OffsetByteArray src, int srcOffset, int destOffset, int length) {
			int realSrcOffset = src.offset + srcOffset;
			int realDestOffset = offset + destOffset;
			byte[] srcArray = src.array;
			byte[] destArray = array;

			// If the arrays are the same and the slices overlap we can't use the optimisation
			// because System.arraycopy effectively copies to a temp area. :-(
			if (srcArray == destArray
					&& (realSrcOffset < realDestOffset && realSrcOffset + length > realDestOffset || realDestOffset < realSrcOffset
							&& realDestOffset + length > realSrcOffset)) {

				System.arraycopy(srcArray, realSrcOffset + 0, destArray, realDestOffset + 0, length);

				return;
			}

			System.arraycopy(srcArray, realSrcOffset, destArray, realDestOffset, length);
		}

		/**
		 * Zeroes out elements of the array.
		 * @param offset the offset from the contained offset.
		 * @param length the number of sequential bytes to zero out.
		 */
		private void zero(int offset, int length) {
			Arrays.fill(array, this.offset + offset, this.offset + offset + length, (byte) 0);
		}
	}
}
