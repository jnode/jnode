/*
 * $Id$
 */
package org.jnode.driver.net.lance;

/**
 * @author epr
 */
public interface LanceConstants {

	// I/O port offsets from iobase.	
	public static final int R_ETH_ADDR_OFFSET	= 0x00;
	public static final int RW_RDATA_OFFSET		= 0x10; // Register data (RDP)
	public static final int RW_ADDR_OFFSET		= 0x14; // RAP
	public static final int RW_RESET_OFFSET		= 0x18; // RESET
	public static final int RW_BDATA_OFFSET		= 0x1c; // Bus data (BDP)

	// CSR0 bits
	public static final int CSR0_INIT			= 0x0001;
	public static final int CSR0_STRT			= 0x0002;
	public static final int CSR0_STOP			= 0x0004;
	public static final int CSR0_TDMD			= 0x0008;
	public static final int CSR0_TXON			= 0x0010;
	public static final int CSR0_RXON			= 0x0020;
	public static final int CSR0_IENA			= 0x0040;
	public static final int CSR0_INTR			= 0x0080;
	public static final int CSR0_IDON			= 0x0100;
	public static final int CSR0_TINT			= 0x0200;
	public static final int CSR0_RINT			= 0x0400;
	public static final int CSR0_MERR			= 0x0800;
	public static final int CSR0_MISS			= 0x1000;
	public static final int CSR0_CERR			= 0x2000;
	public static final int CSR0_BABL			= 0x4000;
	public static final int CSR0_ERR				= 0x8000;

	// CSR3 bits
	public static final int CSR3_BSWP			= 0x0004;
	public static final int CSR3_EMBA			= 0x0008;
	public static final int CSR3_DXMT2PD			= 0x0010;
	public static final int CSR3_LAPPEN			= 0x0020;
	public static final int CSR3_DXSUFLO			= 0x0040;
	public static final int CSR3_IDONM			= 0x0100;
	public static final int CSR3_TINTM			= 0x0200;
	public static final int CSR3_RINTM			= 0x0400;
	public static final int CSR3_MERRM			= 0x0800;
	public static final int CSR3_MISSM			= 0x1000;
	public static final int CSR3_BABLM			= 0x4000;

	// CSR4 bits
	public static final int CSR4_JABM			= 0x0001;
	public static final int CSR4_JAB				= 0x0002;
	public static final int CSR4_TXSTRTM			= 0x0004;
	public static final int CSR4_TXSTRT			= 0x0008;
	public static final int CSR4_RCVCCOM			= 0x0010;
	public static final int CSR4_RCVCCO			= 0x0020;
	public static final int CSR4_UINT			= 0x0040;
	public static final int CSR4_UINTCMD			= 0x0080;
	public static final int CSR4_MFCOM			= 0x0100;
	public static final int CSR4_MFCO			= 0x0200;
	public static final int CSR4_ASTRP_RCV		= 0x0400;
	public static final int CSR4_APAD_XMT		= 0x0800;
	public static final int CSR4_DPOLL			= 0x1000;
	public static final int CSR4_TIMER			= 0x2000;
	public static final int CSR4_DMAPLUS			= 0x4000;
	public static final int CSR4_EN124			= 0x8000;

	// CSR15 bits
	public static final int CSR15_DRX			= 0x0001;
	public static final int CSR15_DTX			= 0x0002;
	public static final int CSR15_LOOP			= 0x0004;
	public static final int CSR15_DXMTFCS		= 0x0008;
	public static final int CSR15_FCOLL			= 0x0010;
	public static final int CSR15_DRTY			= 0x0020;
	public static final int CSR15_INTL			= 0x0040;
	public static final int CSR15_PROM			= 0x8000;

	// BCR2 bits	
	public static final int BCR2_XMAUSEL			= 0x0001;
	public static final int BCR2_ASEL			= 0x0002;
	public static final int BCR2_AWAKE			= 0x0004;
	public static final int BCR2_EADISEL			= 0x0008;
	public static final int BCR2_DXCVRPOL		= 0x0010;
	public static final int BCR2_DXCVRCTL		= 0x0020;
	public static final int BCR2_INTLEVEL		= 0x0080;
	public static final int BCR2_APROMWE			= 0x0100;
	public static final int BCR2_TMAULOOP		= 0x4000;
}
