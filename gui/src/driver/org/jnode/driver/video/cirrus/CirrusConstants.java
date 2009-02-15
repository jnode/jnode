package org.jnode.driver.video.cirrus;

/**
 * @author peda
 */
public interface CirrusConstants {

    final int CIRRUS_FIRST_PORT = 0x3C0;

    final int CIRRUS_LAST_PORT = 0x3DF;

    /* STAT register, not indexed */
    final int STAT_INPUT_STATUS_REGISTER1 = 0x3DA;

    /* HDR Hidden DAC Register */
    final int HDR_HIDDEN_DAC_REGISTER = 0x3C6;

    /* MISC register, not indexed */
    final int MISC_MISCELLANEOUS_OUTPUT_WRITE = 0x3C2;

    final int MISC_MISCELLANEOUS_OUTPUT_READ = 0x3CC;

    final int SRX_SEQUENCER_INDEX = 0x3C4;

    final int SRX_SEQUENCER_DATA = 0x3C5;

    /* Used registers in sequencer SRC */
    final byte SR0_SEQUENCER_RESET = 0x00;

    final byte SR7_EXTENDED_SEQUENCER_MODE = 0x07;

    final byte SR8_DDC2B = 0x08;

    final byte SRB_VCLK0_NUMERATOR = 0x0b;

    final byte SR1B_VCLK0_DENOMINATOR_AND_POST_SCALAR = 0x1b;

    final int CRX_CRTC_INDEX = 0x3D4;

    final int CRX_CRTC_DATA = 0x3D5;

    /* Used registers in CRT controller */
    final int CR0_CRTC_HORIZONTAL_TOTAL = 0x00;

    final int CR1_CRTC_HORIZONTAL_DISPLAY_END = 0x01;

    final int CR2_CRTC_HORIZONTAL_BLANKING_START = 0x02;

    final int CR3_CRTC_HORIZONTAL_BLANKING_END = 0x03;

    final int CR4_CRTC_HORIZONTAL_SYNC_START = 0x04;

    final int CR5_CRTC_HORIZONTAL_SYNC_END = 0x05;

    final int CR6_CRTC_VERTICAL_TOTAL = 0x06;

    final int CR7_CRTC_OVERFLOW = 0x07;

    // CR8, CR9, CRA, CRB,
    final int CRC_START_ADDRESS_HIGH = 0x0C;

    final int CRD_START_ADDRESS_LOW = 0x0D;

    // CRE, CRF
    final int CR10_CRTC_VERTICAL_SYNC_START = 0x10;

    final int CR11_CRTC_VERTICAL_SYNC_END = 0x11;

    final int CR12_CRTC_VERTICAL_DISPLAY_END = 0x12;

    final int CR13_CRTC_OFFSET = 0x13;

    final int CR14_CRTC_UNDERLINE_ROW_SCANLINE = 0x14;

    final int CR15_CRTC_VERTICAL_BLANKING_START = 0x15;

    final int CR17_CRTC_MODE_CONTROL = 0x17;

    final int CR18_CRTC_LINE_COMPARE = 0x18;

    // CR19, CR1A
    final int CR1B_EXTENDED_DISPLAY_CONTROLS = 0x1B;

}
