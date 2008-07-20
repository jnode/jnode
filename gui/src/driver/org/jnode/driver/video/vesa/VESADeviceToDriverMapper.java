package org.jnode.driver.video.vesa;

import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIDeviceConfig;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.Device;
import org.jnode.vm.x86.UnsafeX86;
import org.vmmagic.unboxed.Address;

/**
 * Custom device Mapper for the VESA driver.
 * 
 * @author Levente S\u00e1ntha
 */
public class VESADeviceToDriverMapper implements DeviceToDriverMapper {
    private static final int DISPLAY_CONTROLLER_PCI_DEVICE_CLASS = 0x03;

    public Driver findDriver(Device device) {
        //PCI device needed
        if (!(device instanceof PCIDevice))
            return null;

        //checking display controller device class
        final PCIDevice pciDev = (PCIDevice) device;
        final PCIDeviceConfig cfg = pciDev.getConfig();
        if ((cfg.getBaseClass() & 0xFFFFFF) != DISPLAY_CONTROLLER_PCI_DEVICE_CLASS)
            return null;

        //checking the VESA mode set up by GRUB
        Address vbeControlInfo = UnsafeX86.getVbeControlInfos();
        VbeInfoBlock vbeInfoBlock = new VbeInfoBlock(vbeControlInfo);
        if (vbeInfoBlock.isEmpty())
            return null;

        Address vbeModeInfo = UnsafeX86.getVbeModeInfos();
        ModeInfoBlock modeInfoBlock = new ModeInfoBlock(vbeModeInfo);
        if (modeInfoBlock.isEmpty())
            return null;

        //OK
        return new VESADriver();
    }

    public int getMatchLevel() {
        return DeviceToDriverMapper.MATCH_DEVICE_PREDEFINED;
    }
}
