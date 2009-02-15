package org.jnode.driver.video.cirrus;

import org.jnode.vm.Unsafe;

/**
 * not really used yet.. extend..
 *  
 * @author peda
 */
public class CirrusVGAState implements CirrusConstants {

    private CirrusMMIO vgaIO;

    private int SR7, SRB, SRC, SRD, SRE, SRF;

    // private int SR10, SR11, SR12, SR13 <-- hardware cursor
    // private int SR14, SR15 <-- reserved for bios use, SR16 <-- crt fifo, never write to it!
    private int SR17, SR18, SR19, SR1A, SR1B, SR1C, SR1D;

    private int SR1E, SR1F;

    private int CR0, CR1, CR2, CR3, CR4, CR5, CR6, CR7, CR8, CR9;

    private int CRA, CRB, CRC, CRD, CRE, CRF, CR10, CR11, CR12;

    private int CR13, CR14, CR15, CR16, CR17, CR18, CR22, CR24;

    private int CR26, CR1B;

    public CirrusVGAState(CirrusMMIO vgaIO) {
        this.vgaIO = vgaIO;
    }

    private static String toHex(int value) {
        return Integer.toHexString(value);
    }

    public void display() {

        // TODO: make it better readable and comparable to the spec...
        
        // SR7
        Unsafe.debug("SR7: " + toHex(SR7) + ":: linear frame buffer "
                + ((SR7 >> 4) != 0) + ", sequencer and clocking control: ");
        switch ((SR7 >> 1) & 0x7) {
            case 0:
                Unsafe.debug("8-bpp");
                break;
            case 1:
                Unsafe.debug("reserved");
                break;
            case 2:
                Unsafe.debug("24-bpp");
                break;
            case 3:
                Unsafe.debug("16-bpp");
                break;
            case 4:
                Unsafe.debug("32-bpp");
                break;
            default:
                Unsafe.debug("reserved");
                break;
        }
        Unsafe.debug(", enable HighRes PackedPixel Modes " + ((SR7 & 1) != 0)
                + "\n");

        // SRB-SRE VCLK0-3 Numerator
        Unsafe.debug("SRB-SRE: " + toHex(SRB) + " " + toHex(SRC) + " "
                + toHex(SRD) + " " + toHex(SRE) + "\n");

        Unsafe.debug("//SRF: " + toHex(SRF) + "\n");
        Unsafe.debug("//SR17: " + toHex(SR17) + "\n");
        Unsafe.debug("//SR18: " + toHex(SR18) + "\n");
        Unsafe.debug("//SR19: " + toHex(SR19) + "\n");
        Unsafe.debug("//SR1A: " + toHex(SR1A) + "\n");

        // SR1B-SR1E: VCLK0-3 Denominator and PostScalar
        Unsafe.debug("SR1B-SR1E: " + toHex(SR1B >> 1) + " " + toHex(SR1C >> 1)
                + " " + toHex(SR1D >> 1) + " " + toHex(SR1E >> 1) + "\n");
        Unsafe.debug("PostScalar: " + ((SR1B & 1) == 1) + " "
                + ((SR1C & 1) == 1) + " " + ((SR1D & 1) == 1) + " "
                + ((SR1E & 1) == 1) + "\n");

        Unsafe.debug("SR1F: useMCLKasVCLK " + (((SR1F >> 6) & 1) == 1)
                + " MCLK_Freq: " + toHex(SR1F & 63) + "\n");

        // CR0
        Unsafe.debug("CR0: Horizontal Total (total charecters - 5) = " + CR0
                + "\n");
        // CR1
        Unsafe.debug("CR1: Horizontal Display End: " + CR1 + "\n");
        // CR2
        Unsafe
                .debug("CR2: Horizontal Blanking Start (must be larger than CR1!): "
                        + CR2 + "\n");

        // CR3
        Unsafe.debug("CR3: Compatible Read = " + (CR3 & 128)
                + " (0: CR10/11 are write-onle, 1: read/write)\n");
        Unsafe.debug("CR3: Display Enable Skew = " + toHex((CR3 >> 5) & 3)
                + "\n");
        Unsafe.debug("CR3: Horizontal Blanking End (4:0): " + toHex(CR3 & 31)
                + "\n");

        // CR4
        Unsafe.debug("CR4: Horizontal Sync Start: " + toHex(CR4)
                + " (extended by CR1A[4]\n");

        // CR5 TODO
        Unsafe.debug("CR5: " + toHex(CR5) + "\n");

        // CR6
        Unsafe.debug("CR6: Vertical Total " + CR6
                + " (extended by CR7[0,5], register value = scanlines - 2\n");

        // CR7 TODO
        Unsafe.debug("CR7: Overflow: " + toHex(CR7) + "\n");

        // CR8 TODO
        Unsafe.debug("CR8: ScreenAPresetRowScan: " + toHex(CR8) + "\n");

        // CR9 TODO
        Unsafe.debug("CR9: CharacterCellHeight + Overflow bits: " + toHex(CR9)
                + "\n");

        // CRA/CRB
        Unsafe.debug("//CRA: TextCursorStart " + toHex(CRA) + "\n");
        Unsafe.debug("//CRB: TextCursorEnd" + toHex(CRB) + "\n");

        // CRC/CRD
        Unsafe.debug("CRC: ScreenStartAddressHigh " + toHex(CRC) + " ("
                + (CRC << 8) + ")\n");
        Unsafe.debug("CRD: ScrrenStartAddressLow  " + toHex(CRD) + " (" + CRD
                + " => " + ((CRC << 8) + CRD) + ")\n");
        Unsafe
                .debug("CRC/CRD Extension bits 18:16 in CR1B and bit 19 in CR1D[7]");

        // CRE/CRF
        Unsafe.debug("//CRE: TextCursorLocationHigh " + toHex(CRE) + "\n");
        Unsafe.debug("//CRF: TextCursorLocationLow  " + toHex(CRF) + "\n");

        // CR10 TODO
        Unsafe.debug("CR10: VerticalSyncStart " + CR10 + " (extended by CR7)\n");
    }

    public void dump() {
        // Register dump...
        Unsafe.debug("\nCR0: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR0_CRTC_HORIZONTAL_TOTAL)));
        Unsafe.debug("\nCR1: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR1_CRTC_HORIZONTAL_DISPLAY_END)));
        Unsafe.debug("\nCR2: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR2_CRTC_HORIZONTAL_BLANKING_START)));
        Unsafe.debug("\nCR3: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR3_CRTC_HORIZONTAL_BLANKING_END)));
        Unsafe.debug("\nCR4: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR4_CRTC_HORIZONTAL_SYNC_START)));
        Unsafe.debug("\nCR5: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR5_CRTC_HORIZONTAL_SYNC_END)));
        Unsafe.debug("\nCR6: ");
        Unsafe
                .debug(Integer.toHexString(vgaIO
                        .getCRT(CR6_CRTC_VERTICAL_TOTAL)));
        Unsafe.debug("\nCR7: ");
        Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR7_CRTC_OVERFLOW)));
        Unsafe.debug("\nCRC: ");
        Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CRC_START_ADDRESS_HIGH)));
        Unsafe.debug("\nCRD: ");
        Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CRD_START_ADDRESS_LOW)));
        Unsafe.debug("\nCR10: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR10_CRTC_VERTICAL_SYNC_START)));
        Unsafe.debug("\nCR11: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR11_CRTC_VERTICAL_SYNC_END)));
        Unsafe.debug("\nCR12: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR12_CRTC_VERTICAL_DISPLAY_END)));
        Unsafe.debug("\nCR13: ");
        Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR13_CRTC_OFFSET)));
        Unsafe.debug("\nCR14: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR14_CRTC_UNDERLINE_ROW_SCANLINE)));
        Unsafe.debug("\nCR15: ");
        Unsafe.debug(Integer.toHexString(vgaIO
                .getCRT(CR15_CRTC_VERTICAL_BLANKING_START)));
        Unsafe.debug("\nCR17: ");
        Unsafe.debug(Integer.toBinaryString(vgaIO
                .getCRT(CR17_CRTC_MODE_CONTROL)));
        Unsafe.debug("\nCR18: ");
        Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR18_CRTC_LINE_COMPARE)));
        Unsafe.debug("\nCR1B: ");
        Unsafe.debug(Integer.toBinaryString(vgaIO
                .getCRT(CR1B_EXTENDED_DISPLAY_CONTROLS)));
        Unsafe.debug("\n");
    }
}
