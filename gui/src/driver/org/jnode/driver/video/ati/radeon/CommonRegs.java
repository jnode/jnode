/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class CommonRegs implements RadeonConstants {

	private int ovr_clr;

	private int ovr_wid_left_right;

	private int ovr_wid_top_bottom;

	private int ov0_scale_cntl;

	private int subpic_cntl;

	private int viph_control;

	private int i2c_cntl_1;

	private int gen_int_cntl;

	private int cap0_trig_cntl;

	private int cap1_trig_cntl;

	private int bus_cntl;

	private int surface_cntl;

	/**
	 * Initialize from a given IO.
	 */
	public void saveFromVGA(RadeonVgaIO io) {
		this.ovr_clr = io.getReg32(OVR_CLR);
		this.ovr_wid_left_right = io.getReg32(OVR_WID_LEFT_RIGHT);
		this.ovr_wid_top_bottom = io.getReg32(OVR_WID_TOP_BOTTOM);
		this.ov0_scale_cntl = io.getReg32(OV0_SCALE_CNTL);
		this.subpic_cntl = io.getReg32(SUBPIC_CNTL);
		this.viph_control = io.getReg32(VIPH_CONTROL);
		this.i2c_cntl_1 = io.getReg32(I2C_CNTL_1);
		this.gen_int_cntl = io.getReg32(GEN_INT_CNTL);
		this.cap0_trig_cntl = io.getReg32(CAP0_TRIG_CNTL);
		this.cap1_trig_cntl = io.getReg32(CAP1_TRIG_CNTL);
		this.bus_cntl = io.getReg32(BUS_CNTL);
		this.surface_cntl = io.getReg32(SURFACE_CNTL);
	}

	/**
	 * Save this state to VGA.
	 */
	public final void restoreToVGA(RadeonVgaIO io) {
		io.setReg32(OVR_CLR, this.ovr_clr);
		io.setReg32(OVR_WID_LEFT_RIGHT, this.ovr_wid_left_right);
		io.setReg32(OVR_WID_TOP_BOTTOM, this.ovr_wid_top_bottom);
		io.setReg32(OV0_SCALE_CNTL, this.ov0_scale_cntl);
		io.setReg32(SUBPIC_CNTL, this.subpic_cntl);
		io.setReg32(VIPH_CONTROL, this.viph_control);
		io.setReg32(I2C_CNTL_1, this.i2c_cntl_1);
		io.setReg32(GEN_INT_CNTL, this.gen_int_cntl);
		io.setReg32(CAP0_TRIG_CNTL, this.cap0_trig_cntl);
		io.setReg32(CAP1_TRIG_CNTL, this.cap1_trig_cntl);
		io.setReg32(BUS_CNTL, this.bus_cntl);
		io.setReg32(SURFACE_CNTL, this.surface_cntl);
	}

	/**
	 * Set this state up for a given configuration;
	 * 
	 * @param config
	 * @param io
	 */
	final void calcForConfiguration() {
		this.ovr_clr = 0;
		this.ovr_wid_left_right = 0;
		this.ovr_wid_top_bottom = 0;
		this.ov0_scale_cntl = 0;
		this.subpic_cntl = 0;
		this.i2c_cntl_1 = 0;
		this.surface_cntl = SURF_TRANSLATION_DIS;
	}

}