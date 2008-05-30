/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.net.prism2;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface Prism2Constants {

    // Register offsets

    public enum Register {
        CMD(0x00), PARAM0(0x04), PARAM1(0x08), PARAM2(0x0c), STATUS(0x10), RESP0(
        0x14), RESP1(0x18), RESP2(0x1c), INFOFID(0x20), RXFID(0x40), ALLOCFID(
        0x44), TXCOMPLFID(0x48), SELECT0(0x30), OFFSET0(0x38), DATA0(
        0x6c), SELECT1(0x34), REGSET1(0x3c), DATA1(0x70), EVSTAT(0x60), INTEN(
        0x64), EVACK(0x68), CONTROL(0x28), SWSUPPORT0(0x50), SWSUPPORT1(
        0x54), SWSUPPORT2(0x58), AUXPAGE(0x74), AUXOFFSET(0x78), AUXDATA(
        0x7c), PCICOR(0x4c), PCIHCR(0x5c), PCI_M0_ADDRH(0x80), PCI_M0_ADDRL(
        0x84), PCI_M0_LEN(0x88), PCI_M0_CTL(0x8c), PCI_STATUS(0x98), PCI_M1_ADDRH(
        0xa0), PCI_M1_ADDRL(0xa4), PCI_M1_LEN(0xa8), PCI_M1_CTL(0xac);

        private final int offset;

        private Register(int offset) {
            this.offset = offset;
        }

        /**
         * Gets the offset of this register in the register space.
         *
         * @return Returns the offset.
         */
        public final int getOffset() {
            return offset;
        }
    }

    // Register fields

    public static final int CMD_BUSY = 0x8000;

    public static final int CMD_AINFO = 0x4000 | 0x2000 | 0x1000 | 0x0800
        | 0x0400 | 0x0200 | 0x0100;
    ;

    public static final int CMD_MACPORT = 0x0400 | 0x0200 | 0x0100;
    ;

    public static final int CMD_RECL = 0x0100;

    public static final int CMD_WRITE = 0x0100;

    public static final int CMD_PROGMODE = 0x0200 | 0x0100;
    ;

    public static final int CMD_CMDCODE = 0x20 | 0x10 | 0x08 | 0x04 | 0x02
        | 0x01;
    ;

    public static final int STATUS_RESULT = 0x4000 | 0x2000 | 0x1000 | 0x0800
        | 0x0400 | 0x0200 | 0x0100;

    public static final int STATUS_CMDCODE = 0x20 | 0x10 | 0x08 | 0x04 | 0x02
        | 0x01;
    ;

    public static final int OFFSET_BUSY = 0x8000;

    public static final int OFFSET_ERR = 0x4000;

    public static final int OFFSET_DATAOFF = 0x0800 | 0x0400 | 0x0200 | 0x0100
        | 0x80 | 0x40 | 0x20 | 0x10 | 0x08 | 0x04 | 0x02;

    public static final int EVSTAT_TICK = 0x8000;

    public static final int EVSTAT_WTERR = 0x4000;

    public static final int EVSTAT_INFDROP = 0x2000;

    public static final int EVSTAT_INFO = 0x80;

    public static final int EVSTAT_DTIM = 0x20;

    public static final int EVSTAT_CMD = 0x10;

    public static final int EVSTAT_ALLOC = 0x08;

    public static final int EVSTAT_TXEXC = 0x04;

    public static final int EVSTAT_TX = 0x02;

    public static final int EVSTAT_RX = 0x01;

    public static final int INTEN_TICK = 0x8000;

    public static final int INTEN_WTERR = 0x4000;

    public static final int INTEN_INFDROP = 0x2000;

    public static final int INTEN_INFO = 0x80;

    public static final int INTEN_DTIM = 0x20;

    public static final int INTEN_CMD = 0x10;

    public static final int INTEN_ALLOC = 0x08;

    public static final int INTEN_TXEXC = 0x04;

    public static final int INTEN_TX = 0x02;

    public static final int INTEN_RX = 0x01;

    public static final int EVACK_TICK = 0x8000;

    public static final int EVACK_WTERR = 0x4000;

    public static final int EVACK_INFDROP = 0x2000;

    public static final int EVACK_INFO = 0x80;

    public static final int EVACK_DTIM = 0x20;

    public static final int EVACK_CMD = 0x10;

    public static final int EVACK_ALLOC = 0x08;

    public static final int EVACK_TXEXC = 0x04;

    public static final int EVACK_TX = 0x02;

    public static final int EVACK_RX = 0x01;

    public static final int CONTROL_AUXEN = 0x8000 | 0x4000;

    /*--- Command Code Constants --------------------------*/

    public enum Command {
        /*--- Controller Commands --------------------------*/
        INIT(0x00), ENABLE(0x01), DISABLE(0x02), DIAG(0x03),

        /*--- Buffer Mgmt Commands --------------------------*/
        ALLOC(0x0A), TX(0x0B), CLRPRST(0x12),

        /*--- Regulate Commands --------------------------*/
        NOTIFY(0x10), INQ(0x11),

        /*--- Configure Commands --------------------------*/
        ACCESS(0x21), DOWNLD(0x22),

        /*--- Debugging Commands -----------------------------*/
        MONITOR(0x38);

        private final int code;

        private Command(int code) {
            this.code = code;
        }

        /**
         * @return Returns the code.
         */
        public final int getCode() {
            return code;
        }
    }

    /**
     * Result Codes
     */
    public enum Result {
        SUCCESS(0x00), CARD_FAIL(0x01), NO_BUFF(0x05), CMD_ERR(0x7F);

        private final int code;

        private Result(int code) {
            this.code = code;
        }

        /**
         * Gets the code value.
         *
         * @return
         */
        public final int getCode() {
            return code;
        }

        /**
         * Gets the result value by its code.
         *
         * @param code
         * @return
         */
        public static final Result getByCode(int code) {
            for (Result r : Result.values()) {
                if (r.code == code) {
                    return r;
                }
            }
            throw new IllegalArgumentException("Unknown code " + code);
        }
    }

    /*------ Constants --------------------------------------------*/
    /*--- Mins & Maxs -----------------------------------*/
    public static final int CMD_ALLOC_LEN_MIN = 4;

    public static final int CMD_ALLOC_LEN_MAX = 2400;

    public static final int BAP_DATALEN_MAX = 4096;

    public static final int BAP_OFFSET_MAX = 4096;

    public static final int PORTID_MAX = 7;

    public static final int NUMPORTS_MAX = PORTID_MAX + 1;

    public static final int PDR_LEN_MAX = 512; /* in bytes, from EK */

    public static final int PDA_RECS_MAX = 200; /* a guess */

    public static final int PDA_LEN_MAX = 1024; /* in bytes, from EK */

    public static final int SCANRESULT_MAX = 31;

    public static final int HSCANRESULT_MAX = 31;

    public static final int CHINFORESULT_MAX = 16;

    public static final int DRVR_FIDSTACKLEN_MAX = 10;

    /*--- Record ID Constants --------------------------*/

    public enum RecordID {
        /*--------------------------------------------------------------------
         Configuration RIDs: Network Parameters, Static Configuration Entities
         --------------------------------------------------------------------*/
        CNFPORTTYPE(0xFC00, 2), CNFOWNMACADDR(0xFC01, 6), CNFDESIREDSSID(
        0xFC02, 34), CNFOWNCHANNEL(0xFC03, 2), CNFOWNSSID(0xFC04, 34), CNFOWNATIMWIN(
        0xFC05, 2), CNFSYSSCALE(0xFC06), CNFMAXDATALEN(0xFC07), CNFWDSADDR(
        0xFC08, 6), CNFPMENABLED(0xFC09), CNFPMEPS(0xFC0A), CNFMULTICASTRX(
        0xFC0B), CNFMAXSLEEPDUR(0xFC0C), CNFPMHOLDDUR(0xFC0D), CNFOWNNAME(
        0xFC0E), CNFOWNDTIMPER(0xFC10), CNFWDSADDR1(0xFC11), CNFWDSADDR2(
        0xFC12), CNFWDSADDR3(0xFC13), CNFWDSADDR4(0xFC14), CNFWDSADDR5(
        0xFC15), CNFWDSADDR6(0xFC16), CNFMCASTPMBUFF(0xFC17),
        /*--------------------------------------------------------------------
         Configuration RIDs: Network Parameters, Dynamic Configuration Entities
         --------------------------------------------------------------------*/
        GROUPADDR(0xFC80), CREATEIBSS(0xFC81), FRAGTHRESH(0xFC82), RTSTHRESH(
        0xFC83), TXRATECNTL(0xFC84), PROMISCMODE(0xFC85), FRAGTHRESH0(
        0xFC90), FRAGTHRESH1(0xFC91), FRAGTHRESH2(0xFC92), FRAGTHRESH3(
        0xFC93), FRAGTHRESH4(0xFC94), FRAGTHRESH5(0xFC95), FRAGTHRESH6(
        0xFC96), RTSTHRESH0(0xFC97), RTSTHRESH1(0xFC98), RTSTHRESH2(
        0xFC99), RTSTHRESH3(0xFC9A), RTSTHRESH4(0xFC9B), RTSTHRESH5(
        0xFC9C), RTSTHRESH6(0xFC9D), TXRATECNTL0(0xFC9E), TXRATECNTL1(
        0xFC9F), TXRATECNTL2(0xFCA0), TXRATECNTL3(0xFCA1), TXRATECNTL4(
        0xFCA2), TXRATECNTL5(0xFCA3), TXRATECNTL6(0xFCA4),

        /*--------------------------------------------------------------------
         API ENHANCEMENTS (NOT ALREADY IMPLEMENTED)
         --------------------------------------------------------------------*/
        CNFWEPDEFAULTKEYID(0xFC23), CNFWEPDEFAULTKEY0(0xFC24), CNFWEPDEFAULTKEY1(
        0xFC25), CNFWEPDEFAULTKEY2(0xFC26), CNFWEPDEFAULTKEY3(0xFC27), CNFWEPFLAGS(
        0xFC28), CNFWEPKEYMAPTABLE(0xFC29), CNFAUTHENTICATION(0xFC2A), CNFMAXASSOCSTATIONS(
        0xFC2B), CNFTXCONTROL(0xFC2C), CNFROAMINGMODE(0xFC2D), CNFHOSTAUTH(
        0xFC2E), CNFRCVCRCERROR(0xFC30), CNFMMLIFE(0xFC31), CNFALTRETRYCNT(
        0xFC32), CNFAPBCNINT(0xFC33), CNFAPPCFINFO(0xFC34), CNFSTAPCFINFO(
        0xFC35), CNFPRIORITYQUSAGE(0xFC37), CNFTIMCTRL(0xFC40), CNFTHIRTY2TALLY(
        0xFC42), CNFENHSECURITY(0xFC43), CNFDBMADJUST(0xFC46), // NEW
        CNFSHORTPREAMBLE(0xFCB0), CNFEXCLONGPREAMBLE(0xFCB1), CNFAUTHRSPTIMEOUT(
        0xFCB2), CNFBASICRATES(0xFCB3), CNFSUPPRATES(0xFCB4), CNFFALLBACKCTRL(
        0xFCB5), // NEW
        WEPKEYDISABLE(0xFCB6), // NEW
        WEPKEYMAPINDEX(0xFCB7), // NEW AP
        BROADCASTKEYID(0xFCB8), // NEW AP
        ENTSECFLAGEYID(0xFCB9), // NEW AP
        CNFPASSIVESCANCTRL(0xFCB9), // NEW STA
        SCANREQUEST(0xFCE1), JOINREQUEST(0xFCE2), AUTHENTICATESTA(0xFCE3), CHANNELINFOREQUEST(
        0xFCE4), HOSTSCAN(0xFCE5), // NEW STA
        /*--------------------------------------------------------------------
         Configuration RIDs: Behavior Parameters
         --------------------------------------------------------------------*/
        ITICKTIME(0xFCE0),

        /*----------------------------------------------------------------------
         Information RIDs: NIC Information
         --------------------------------------------------------------------*/
        MAXLOADTIME(0xFD00), DOWNLOADBUFFER(0xFD01), PRIIDENTITY(0xFD02), PRISUPRANGE(
        0xFD03), PRI_CFIACTRANGES(0xFD04), NICSERIALNUMBER(0xFD0A), NICIDENTITY(
        0xFD0B), MFISUPRANGE(0xFD0C), CFISUPRANGE(0xFD0D), CHANNELLIST(
        0xFD10), REGULATORYDOMAINS(0xFD11), TEMPTYPE(0xFD12), CIS(
        0xFD13), STAIDENTITY(0xFD20), STASUPRANGE(0xFD21), STA_MFIACTRANGES(
        0xFD22), STA_CFIACTRANGES(0xFD23), BUILDSEQ(0xFFFE), FWID(
        0xFFFF),

        /*--------------------------------------------------------------------
         Information RIDs:  MAC Information
         --------------------------------------------------------------------*/
        PORTSTATUS(0xFD40), CURRENTSSID(0xFD41), CURRENTBSSID(0xFD42), COMMSQUALITY(
        0xFD43), CURRENTTXRATE(0xFD44), CURRENTBCNINT(0xFD45), CURRENTSCALETHRESH(
        0xFD46), PROTOCOLRSPTIME(0xFD47), SHORTRETRYLIMIT(0xFD48), LONGRETRYLIMIT(
        0xFD49), MAXTXLIFETIME(0xFD4A), MAXRXLIFETIME(0xFD4B), CFPOLLABLE(
        0xFD4C), AUTHALGORITHMS(0xFD4D), PRIVACYOPTIMP(0xFD4F), DBMCOMMSQUALITY(
        0xFD51), CURRENTTXRATE1(0xFD80), CURRENTTXRATE2(0xFD81), CURRENTTXRATE3(
        0xFD82), CURRENTTXRATE4(0xFD83), CURRENTTXRATE5(0xFD84), CURRENTTXRATE6(
        0xFD85), OWNMACADDRESS(0xFD86), PCFINFO(0xFD87), SCANRESULTS(
        0xFD88), // NEW
        HOSTSCANRESULTS(0xFD89), // NEW
        AUTHENTICATIONUSED(0xFD8A), // NEW

        /*--------------------------------------------------------------------
         Information RIDs:  Modem Information
         --------------------------------------------------------------------*/
        PHYTYPE(0xFDC0), CURRENTCHANNEL(0xFDC1), CURRENTPOWERSTATE(0xFDC2), CCAMODE(
        0xFDC3), SUPPORTEDDATARATES(0xFDC6),;

        private final int id;

        private final int recordLength;

        private RecordID(int id) {
            this.id = id;
            this.recordLength = 0;
        }

        private RecordID(int id, int recordLength) {
            this.id = id;
            this.recordLength = recordLength;
        }

        /**
         * @return Returns the id.
         */
        public final int getId() {
            return id;
        }

        /**
         * @return Returns the recordLength.
         */
        public final int getRecordLength() {
            return recordLength;
        }
    }

    /*--------------------------------------------------------------------
     Configuration RID lengths: Network Params, Static Config Entities
     This is the length of JUST the DATA part of the RID (does not 
     include the len or code fields;
     --------------------------------------------------------------------*/
    /* TODO: fill in the rest of these */
    public static final int RID_CNFPMENABLED_LEN = 0;

    public static final int RID_CNFPMEPS_LEN = 0;

    public static final int RID_CNFMULTICASTRX_LEN = 0;

    // public static final int RID_CNFMAXSLEEPDUR_LEN = 0;
    public static final int RID_CNFPMHOLDDUR_LEN = 0;

    public static final int RID_CNFOWNNAME_LEN = 34;

    public static final int RID_CNFOWNDTIMPER_LEN = 0;

    public static final int RID_CNFWDSADDR1_LEN = 6;

    public static final int RID_CNFWDSADDR2_LEN = 6;

    public static final int RID_CNFWDSADDR3_LEN = 6;

    public static final int RID_CNFWDSADDR4_LEN = 6;

    public static final int RID_CNFWDSADDR5_LEN = 6;

    public static final int RID_CNFWDSADDR6_LEN = 6;

    public static final int RID_CNFMCASTPMBUFF_LEN = 0;

    // public static final int RID_CNFAUTHENTICATION_LEN = sizeof(UINT16;;
    // public static final int RID_CNFMAXSLEEPDUR_LEN = 0;

    public static final int RID_CNFWEPDEFAULTKEY_LEN = 6;

    public static final int RID_CNFWEP128DEFAULTKEY_LEN = 14;

    public static final int RID_CNFPRIOQUSAGE_LEN = 4;

    /*--------------------------------------------------------------------
     Configuration RID Lengths: Network Param, Dynamic Config Entities
     This is the length of JUST the DATA part of the RID (does not 
     include the len or code fields;
     --------------------------------------------------------------------*/
    /* TODO: fill in the rest of these */
    // public static final int RID_GROUPADDR_LEN = 16 * WLAN_ADDR_LEN;
    public static final int RID_CREATEIBSS_LEN = 0;

    public static final int RID_FRAGTHRESH_LEN = 0;

    public static final int RID_RTSTHRESH_LEN = 0;

    public static final int RID_TXRATECNTL_LEN = 4;

    public static final int RID_PROMISCMODE_LEN = 2;

    public static final int RID_FRAGTHRESH0_LEN = 0;

    public static final int RID_FRAGTHRESH1_LEN = 0;

    public static final int RID_FRAGTHRESH2_LEN = 0;

    public static final int RID_FRAGTHRESH3_LEN = 0;

    public static final int RID_FRAGTHRESH4_LEN = 0;

    public static final int RID_FRAGTHRESH5_LEN = 0;

    public static final int RID_FRAGTHRESH6_LEN = 0;

    public static final int RID_RTSTHRESH0_LEN = 0;

    public static final int RID_RTSTHRESH1_LEN = 0;

    public static final int RID_RTSTHRESH2_LEN = 0;

    public static final int RID_RTSTHRESH3_LEN = 0;

    public static final int RID_RTSTHRESH4_LEN = 0;

    public static final int RID_RTSTHRESH5_LEN = 0;

    public static final int RID_RTSTHRESH6_LEN = 0;

    public static final int RID_TXRATECNTL0_LEN = 0;

    public static final int RID_TXRATECNTL1_LEN = 0;

    public static final int RID_TXRATECNTL2_LEN = 0;

    public static final int RID_TXRATECNTL3_LEN = 0;

    public static final int RID_TXRATECNTL4_LEN = 0;

    public static final int RID_TXRATECNTL5_LEN = 0;

    public static final int RID_TXRATECNTL6_LEN = 0;

    /*--------------------------------------------------------------------
     Configuration RID Lengths: Behavior Parameters
     This is the length of JUST the DATA part of the RID (does not 
     include the len or code fields;
     --------------------------------------------------------------------*/
    public static final int RID_ITICKTIME_LEN = 2;

    /*----------------------------------------------------------------------
     Information RID Lengths: NIC Information
     This is the length of JUST the DATA part of the RID (does not 
     include the len or code fields;
     --------------------------------------------------------------------*/
    public static final int RID_MAXLOADTIME_LEN = 0;

    // public static final int RID_DOWNLOADBUFFER_LEN =
    // sizeof(downloadbuffer_t;;
    public static final int RID_PRIIDENTITY_LEN = 8;

    public static final int RID_PRISUPRANGE_LEN = 10;

    public static final int RID_CFIACTRANGES_LEN = 10;

    public static final int RID_NICSERIALNUMBER_LEN = 12;

    public static final int RID_NICIDENTITY_LEN = 8;

    public static final int RID_MFISUPRANGE_LEN = 10;

    public static final int RID_CFISUPRANGE_LEN = 10;

    public static final int RID_CHANNELLIST_LEN = 0;

    public static final int RID_REGULATORYDOMAINS_LEN = 12;

    public static final int RID_TEMPTYPE_LEN = 0;

    public static final int RID_CIS_LEN = 480;

    public static final int RID_STAIDENTITY_LEN = 8;

    public static final int RID_STASUPRANGE_LEN = 10;

    public static final int RID_MFIACTRANGES_LEN = 10;

    public static final int RID_CFIACTRANGES2_LEN = 10;

    // public static final int RID_BUILDSEQ_LEN = sizeof(BuildSeq_t;;
    // public static final int RID_FWID_LEN = sizeof(FWID_t;;

    /*--------------------------------------------------------------------
     Information RID Lengths:  MAC Information
     This is the length of JUST the DATA part of the RID (does not 
     include the len or code fields;
     --------------------------------------------------------------------*/
    public static final int RID_PORTSTATUS_LEN = 0;

    public static final int RID_CURRENTSSID_LEN = 34;

    // public static final int RID_CURRENTBSSID_LEN = WLAN_BSSID_LEN;
    // public static final int RID_COMMSQUALITY_LEN = sizeof(commsquality_t;;
    // public static final int RID_DBMCOMMSQUALITY_LEN =
    // sizeof(dbmcommsquality_t;;
    public static final int RID_CURRENTTXRATE_LEN = 0;

    public static final int RID_CURRENTBCNINT_LEN = 0;

    public static final int RID_STACURSCALETHRESH_LEN = 12;

    public static final int RID_APCURSCALETHRESH_LEN = 6;

    public static final int RID_PROTOCOLRSPTIME_LEN = 0;

    public static final int RID_SHORTRETRYLIMIT_LEN = 0;

    public static final int RID_LONGRETRYLIMIT_LEN = 0;

    public static final int RID_MAXTXLIFETIME_LEN = 0;

    public static final int RID_MAXRXLIFETIME_LEN = 0;

    public static final int RID_CFPOLLABLE_LEN = 0;

    public static final int RID_AUTHALGORITHMS_LEN = 4;

    public static final int RID_PRIVACYOPTIMP_LEN = 0;

    public static final int RID_CURRENTTXRATE1_LEN = 0;

    public static final int RID_CURRENTTXRATE2_LEN = 0;

    public static final int RID_CURRENTTXRATE3_LEN = 0;

    public static final int RID_CURRENTTXRATE4_LEN = 0;

    public static final int RID_CURRENTTXRATE5_LEN = 0;

    public static final int RID_CURRENTTXRATE6_LEN = 0;

    public static final int RID_OWNMACADDRESS_LEN = 6;

    public static final int RID_PCFINFO_LEN = 6;

    // public static final int RID_CNFAPPCFINFO_LEN = sizeof(PCFInfo_data_t;;
    // public static final int RID_SCANREQUEST_LEN = sizeof(ScanRequest_data_t;;
    // public static final int RID_JOINREQUEST_LEN = sizeof(JoinRequest_data_t;;
    // public static final int RID_AUTHENTICATESTA_LEN =
    // sizeof(authenticateStation_data_t;;
    // public static final int RID_CHANNELINFOREQUEST_LEN =
    // sizeof(ChannelInfoRequest_data_t;;
    /*--------------------------------------------------------------------
     Information RID Lengths:  Modem Information 
     This is the length of JUST the DATA part of the RID (does not 
     include the len or code fields;
     --------------------------------------------------------------------*/
    public static final int RID_PHYTYPE_LEN = 0;

    public static final int RID_CURRENTCHANNEL_LEN = 0;

    public static final int RID_CURRENTPOWERSTATE_LEN = 0;

    public static final int RID_CCAMODE_LEN = 0;

    public static final int RID_SUPPORTEDDATARATES_LEN = 10;

    /*-- Configuration Record: cnfAuthentication --*/
    public static final int CNFAUTHENTICATION_OPENSYSTEM = 0x0001;

    public static final int CNFAUTHENTICATION_SHAREDKEY = 0x0002;

    /**
     * Information types
     */
    public enum InformationType {
        HANDOVERADDR(0xF000), COMMTALLIES(0xF100), SCANRESULTS(0xF101), CHINFORESULTS(
        0xF102), HOSTSCANRESULTS(0xF103), LINKSTATUS(0xF200), ASSOCSTATUS(
        0xF201), AUTHREQ(0xF202), PSUSERCNT(0xF203), KEYIDCHANGED(
        0xF204);

        private final int value;

        private InformationType(int value) {
            this.value = value;
        }

        /**
         * @return Returns the value.
         */
        public final int getValue() {
            return value;
        }

        /**
         * Get the enum by its value.
         *
         * @param value
         * @return
         */
        public static InformationType getByValue(int value) {
            for (InformationType it : values()) {
                if (it.value == value) {
                    return it;
                }
            }
            throw new IllegalArgumentException("Invalid value " + value);
        }
    }

    /**
     * Link status values
     */
    public enum LinkStatus {
        NOTCONNECTED(0), CONNECTED(1), DISCONNECTED(2), AP_CHANGE(3), AP_OUTOFRANGE(
        4), AP_INRANGE(5), ASSOCFAIL(6);

        private final int value;

        private LinkStatus(int value) {
            this.value = value;
        }

        /**
         * @return Returns the value.
         */
        public final int getValue() {
            return value;
        }

        /**
         * Get the enum by its value.
         *
         * @param value
         * @return
         */
        public static LinkStatus getByValue(int value) {
            for (LinkStatus ls : values()) {
                if (ls.value == value) {
                    return ls;
                }
            }
            throw new IllegalArgumentException("Invalid value " + value);
        }
    }
}
