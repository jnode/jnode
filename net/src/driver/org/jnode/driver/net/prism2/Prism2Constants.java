/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface Prism2Constants {

    // Register offsets

    public static final int REG_CMD = 0x00;

    public static final int REG_PARAM0 = 0x04;

    public static final int REG_PARAM1 = 0x08;

    public static final int REG_PARAM2 = 0x0c;

    public static final int REG_STATUS = 0x10;

    public static final int REG_RESP0 = 0x14;

    public static final int REG_RESP1 = 0x18;

    public static final int REG_RESP2 = 0x1c;

    public static final int REG_INFOFID = 0x20;

    public static final int REG_RXFID = 0x40;

    public static final int REG_ALLOCFID = 0x44;

    public static final int REG_TXCOMPLFID = 0x48;

    public static final int REG_SELECT0 = 0x30;

    public static final int REG_OFFSET0 = 0x38;

    public static final int REG_DATA0 = 0x6c;

    public static final int REG_SELECT1 = 0x34;

    public static final int REGSET1 = 0x3c;

    public static final int REG_DATA1 = 0x70;

    public static final int REG_EVSTAT = 0x60;

    public static final int REG_INTEN = 0x64;

    public static final int REG_EVACK = 0x68;

    public static final int REG_CONTROL = 0x28;

    public static final int REG_SWSUPPORT0 = 0x50;

    public static final int REG_SWSUPPORT1 = 0x54;

    public static final int REG_SWSUPPORT2 = 0x58;

    public static final int REG_AUXPAGE = 0x74;

    public static final int REG_AUXOFFSET = 0x78;

    public static final int REG_AUXDATA = 0x7c;

    public static final int REG_PCICOR = 0x4c;

    public static final int REG_PCIHCR = 0x5c;

    public static final int REG_PCI_M0_ADDRH = 0x80;

    public static final int REG_PCI_M0_ADDRL = 0x84;

    public static final int REG_PCI_M0_LEN = 0x88;

    public static final int REG_PCI_M0_CTL = 0x8c;

    public static final int REG_PCI_STATUS = 0x98;

    public static final int REG_PCI_M1_ADDRH = 0xa0;

    public static final int REG_PCI_M1_ADDRL = 0xa4;

    public static final int REG_PCI_M1_LEN = 0xa8;

    public static final int REG_PCI_M1_CTL = 0xac;

    // Register fields

    public static final int CMD_BUSY = 0x8000;

    public static final int CMD_AINFO = 0x4000 | 0x2000 | 0x1000 | 0x0800
            | 0x0400 | 0x0200 | 0x0100;;

    public static final int CMD_MACPORT = 0x0400 | 0x0200 | 0x0100;;

    public static final int CMD_RECL = 0x0100;

    public static final int CMD_WRITE = 0x0100;

    public static final int CMD_PROGMODE = 0x0200 | 0x0100;;

    public static final int CMD_CMDCODE = 0x20 | 0x10 | 0x08 | 0x04 | 0x02
            | 0x01;;

    public static final int STATUS_RESULT = 0x4000 | 0x2000 | 0x1000 | 0x0800
            | 0x0400 | 0x0200 | 0x0100;

    public static final int STATUS_CMDCODE = 0x20 | 0x10 | 0x08 | 0x04 | 0x02
            | 0x01;;

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
    /*--- Controller Commands --------------------------*/
    public static final int CMDCODE_INIT = 0x00;

    public static final int CMDCODE_ENABLE = 0x01;

    public static final int CMDCODE_DISABLE = 0x02;

    public static final int CMDCODE_DIAG = 0x03;

    /*--- Buffer Mgmt Commands --------------------------*/
    public static final int CMDCODE_ALLOC = 0x0A;

    public static final int CMDCODE_TX = 0x0B;

    public static final int CMDCODE_CLRPRST = 0x12;

    /*--- Regulate Commands --------------------------*/
    public static final int CMDCODE_NOTIFY = 0x10;

    public static final int CMDCODE_INQ = 0x11;

    /*--- Configure Commands --------------------------*/
    public static final int CMDCODE_ACCESS = 0x21;

    public static final int CMDCODE_DOWNLD = 0x22;

    /*--- Debugging Commands -----------------------------*/
    public static final int CMDCODE_MONITOR = 0x38;

    public static final int MONITOR_ENABLE = 0x0b;

    public static final int MONITOR_DISABLE = 0x0f;

    /*--- Result Codes --------------------------*/
    public static final int RESULT_SUCCESS = 0x00;

    public static final int RESULT_CARD_FAIL = 0x01;

    public static final int RESULT_NO_BUFF = 0x05;

    public static final int RESULT_CMD_ERR = 0x7F;

    /*------ Constants --------------------------------------------*/
    /*--- Mins & Maxs -----------------------------------*/
    public static final int CMD_ALLOC_LEN_MIN = 4;

    public static final int CMD_ALLOC_LEN_MAX = 2400;

    public static final int BAP_DATALEN_MAX = 4096;

    public static final int BAP_OFFSET_MAX = 4096;

    public static final int PORTID_MAX = 7;

    public static final int NUMPORTS_MAX = PORTID_MAX + 1;;

    public static final int PDR_LEN_MAX = 512; /* in bytes, from EK */

    public static final int PDA_RECS_MAX = 200; /* a guess */

    public static final int PDA_LEN_MAX = 1024; /* in bytes, from EK */

    public static final int SCANRESULT_MAX = 31;

    public static final int HSCANRESULT_MAX = 31;

    public static final int CHINFORESULT_MAX = 16;

    public static final int DRVR_FIDSTACKLEN_MAX = 10;

    /*--- Record ID Constants --------------------------*/
    /*--------------------------------------------------------------------
     Configuration RIDs: Network Parameters, Static Configuration Entities
     --------------------------------------------------------------------*/
    public static final int RID_CNFPORTTYPE = 0xFC00;

    public static final int RID_CNFOWNMACADDR = 0xFC01;

    public static final int RID_CNFDESIREDSSID = 0xFC02;

    public static final int RID_CNFOWNCHANNEL = 0xFC03;

    public static final int RID_CNFOWNSSID = 0xFC04;

    public static final int RID_CNFOWNATIMWIN = 0xFC05;

    public static final int RID_CNFSYSSCALE = 0xFC06;

    public static final int RID_CNFMAXDATALEN = 0xFC07;

    public static final int RID_CNFWDSADDR = 0xFC08;

    public static final int RID_CNFPMENABLED = 0xFC09;

    public static final int RID_CNFPMEPS = 0xFC0A;

    public static final int RID_CNFMULTICASTRX = 0xFC0B;

    public static final int RID_CNFMAXSLEEPDUR = 0xFC0C;

    public static final int RID_CNFPMHOLDDUR = 0xFC0D;

    public static final int RID_CNFOWNNAME = 0xFC0E;

    public static final int RID_CNFOWNDTIMPER = 0xFC10;

    public static final int RID_CNFWDSADDR1 = 0xFC11;

    public static final int RID_CNFWDSADDR2 = 0xFC12;

    public static final int RID_CNFWDSADDR3 = 0xFC13;

    public static final int RID_CNFWDSADDR4 = 0xFC14;

    public static final int RID_CNFWDSADDR5 = 0xFC15;

    public static final int RID_CNFWDSADDR6 = 0xFC16;

    public static final int RID_CNFMCASTPMBUFF = 0xFC17;

    /*--------------------------------------------------------------------
     Configuration RID lengths: Network Params, Static Config Entities
     This is the length of JUST the DATA part of the RID (does not 
     include the len or code fields;
     --------------------------------------------------------------------*/
    /* TODO: fill in the rest of these */
    public static final int RID_CNFPORTTYPE_LEN = 2;

    public static final int RID_CNFOWNMACADDR_LEN = 6;

    public static final int RID_CNFDESIREDSSID_LEN = 34;

    public static final int RID_CNFOWNCHANNEL_LEN = 2;

    public static final int RID_CNFOWNSSID_LEN = 34;

    public static final int RID_CNFOWNATIMWIN_LEN = 2;

    public static final int RID_CNFSYSSCALE_LEN = 0;

    public static final int RID_CNFMAXDATALEN_LEN = 0;

    public static final int RID_CNFWDSADDR_LEN = 6;

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

    /*--------------------------------------------------------------------
     Configuration RIDs: Network Parameters, Dynamic Configuration Entities
     --------------------------------------------------------------------*/
    public static final int RID_GROUPADDR = 0xFC80;

    public static final int RID_CREATEIBSS = 0xFC81;

    public static final int RID_FRAGTHRESH = 0xFC82;

    public static final int RID_RTSTHRESH = 0xFC83;

    public static final int RID_TXRATECNTL = 0xFC84;

    public static final int RID_PROMISCMODE = 0xFC85;

    public static final int RID_FRAGTHRESH0 = 0xFC90;

    public static final int RID_FRAGTHRESH1 = 0xFC91;

    public static final int RID_FRAGTHRESH2 = 0xFC92;

    public static final int RID_FRAGTHRESH3 = 0xFC93;

    public static final int RID_FRAGTHRESH4 = 0xFC94;

    public static final int RID_FRAGTHRESH5 = 0xFC95;

    public static final int RID_FRAGTHRESH6 = 0xFC96;

    public static final int RID_RTSTHRESH0 = 0xFC97;

    public static final int RID_RTSTHRESH1 = 0xFC98;

    public static final int RID_RTSTHRESH2 = 0xFC99;

    public static final int RID_RTSTHRESH3 = 0xFC9A;

    public static final int RID_RTSTHRESH4 = 0xFC9B;

    public static final int RID_RTSTHRESH5 = 0xFC9C;

    public static final int RID_RTSTHRESH6 = 0xFC9D;

    public static final int RID_TXRATECNTL0 = 0xFC9E;

    public static final int RID_TXRATECNTL1 = 0xFC9F;

    public static final int RID_TXRATECNTL2 = 0xFCA0;

    public static final int RID_TXRATECNTL3 = 0xFCA1;

    public static final int RID_TXRATECNTL4 = 0xFCA2;

    public static final int RID_TXRATECNTL5 = 0xFCA3;

    public static final int RID_TXRATECNTL6 = 0xFCA4;

    /*--------------------------------------------------------------------
     API ENHANCEMENTS (NOT ALREADY IMPLEMENTED)
     --------------------------------------------------------------------*/
    public static final int RID_CNFWEPDEFAULTKEYID = 0xFC23;

    public static final int RID_CNFWEPDEFAULTKEY0 = 0xFC24;

    public static final int RID_CNFWEPDEFAULTKEY1 = 0xFC25;

    public static final int RID_CNFWEPDEFAULTKEY2 = 0xFC26;

    public static final int RID_CNFWEPDEFAULTKEY3 = 0xFC27;

    public static final int RID_CNFWEPFLAGS = 0xFC28;

    public static final int RID_CNFWEPKEYMAPTABLE = 0xFC29;

    public static final int RID_CNFAUTHENTICATION = 0xFC2A;

    public static final int RID_CNFMAXASSOCSTATIONS = 0xFC2B;

    public static final int RID_CNFTXCONTROL = 0xFC2C;

    public static final int RID_CNFROAMINGMODE = 0xFC2D;

    public static final int RID_CNFHOSTAUTH = 0xFC2E;

    public static final int RID_CNFRCVCRCERROR = 0xFC30;

    // public static final int RID_CNFMMLIFE =0xFC31;
    public static final int RID_CNFALTRETRYCNT = 0xFC32;

    public static final int RID_CNFAPBCNINT = 0xFC33;

    public static final int RID_CNFAPPCFINFO = 0xFC34;

    public static final int RID_CNFSTAPCFINFO = 0xFC35;

    public static final int RID_CNFPRIORITYQUSAGE = 0xFC37;

    public static final int RID_CNFTIMCTRL = 0xFC40;

    public static final int RID_CNFTHIRTY2TALLY = 0xFC42;

    public static final int RID_CNFENHSECURITY = 0xFC43;

    public static final int RID_CNFDBMADJUST = 0xFC46; // NEW

    public static final int RID_CNFSHORTPREAMBLE = 0xFCB0;

    public static final int RID_CNFEXCLONGPREAMBLE = 0xFCB1;

    public static final int RID_CNFAUTHRSPTIMEOUT = 0xFCB2;

    public static final int RID_CNFBASICRATES = 0xFCB3;

    public static final int RID_CNFSUPPRATES = 0xFCB4;

    public static final int RID_CNFFALLBACKCTRL = 0xFCB5; // NEW

    public static final int RID_WEPKEYDISABLE = 0xFCB6; // NEW

    public static final int RID_WEPKEYMAPINDEX = 0xFCB7; // NEW AP

    public static final int RID_BROADCASTKEYID = 0xFCB8; // NEW AP

    public static final int RID_ENTSECFLAGEYID = 0xFCB9; // NEW AP

    public static final int RID_CNFPASSIVESCANCTRL = 0xFCB9; // NEW STA

    public static final int RID_SCANREQUEST = 0xFCE1;

    public static final int RID_JOINREQUEST = 0xFCE2;

    public static final int RID_AUTHENTICATESTA = 0xFCE3;

    public static final int RID_CHANNELINFOREQUEST = 0xFCE4;

    public static final int RID_HOSTSCAN = 0xFCE5; // NEW STA

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
     Configuration RIDs: Behavior Parameters
     --------------------------------------------------------------------*/
    public static final int RID_ITICKTIME = 0xFCE0;

    /*--------------------------------------------------------------------
     Configuration RID Lengths: Behavior Parameters
     This is the length of JUST the DATA part of the RID (does not 
     include the len or code fields;
     --------------------------------------------------------------------*/
    public static final int RID_ITICKTIME_LEN = 2;

    /*----------------------------------------------------------------------
     Information RIDs: NIC Information
     --------------------------------------------------------------------*/
    public static final int RID_MAXLOADTIME = 0xFD00;

    public static final int RID_DOWNLOADBUFFER = 0xFD01;

    public static final int RID_PRIIDENTITY = 0xFD02;

    public static final int RID_PRISUPRANGE = 0xFD03;

    public static final int RID_PRI_CFIACTRANGES = 0xFD04;

    public static final int RID_NICSERIALNUMBER = 0xFD0A;

    public static final int RID_NICIDENTITY = 0xFD0B;

    public static final int RID_MFISUPRANGE = 0xFD0C;

    public static final int RID_CFISUPRANGE = 0xFD0D;

    public static final int RID_CHANNELLIST = 0xFD10;

    public static final int RID_REGULATORYDOMAINS = 0xFD11;

    public static final int RID_TEMPTYPE = 0xFD12;

    public static final int RID_CIS = 0xFD13;

    public static final int RID_STAIDENTITY = 0xFD20;

    public static final int RID_STASUPRANGE = 0xFD21;

    public static final int RID_STA_MFIACTRANGES = 0xFD22;

    public static final int RID_STA_CFIACTRANGES = 0xFD23;

    public static final int RID_BUILDSEQ = 0xFFFE;

    public static final int RID_FWID = 0xFFFF;

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
     Information RIDs:  MAC Information
     --------------------------------------------------------------------*/
    public static final int RID_PORTSTATUS = 0xFD40;

    public static final int RID_CURRENTSSID = 0xFD41;

    public static final int RID_CURRENTBSSID = 0xFD42;

    public static final int RID_COMMSQUALITY = 0xFD43;

    public static final int RID_CURRENTTXRATE = 0xFD44;

    public static final int RID_CURRENTBCNINT = 0xFD45;

    public static final int RID_CURRENTSCALETHRESH = 0xFD46;

    public static final int RID_PROTOCOLRSPTIME = 0xFD47;

    public static final int RID_SHORTRETRYLIMIT = 0xFD48;

    public static final int RID_LONGRETRYLIMIT = 0xFD49;

    public static final int RID_MAXTXLIFETIME = 0xFD4A;

    public static final int RID_MAXRXLIFETIME = 0xFD4B;

    public static final int RID_CFPOLLABLE = 0xFD4C;

    public static final int RID_AUTHALGORITHMS = 0xFD4D;

    public static final int RID_PRIVACYOPTIMP = 0xFD4F;

    public static final int RID_DBMCOMMSQUALITY = 0xFD51;

    public static final int RID_CURRENTTXRATE1 = 0xFD80;

    public static final int RID_CURRENTTXRATE2 = 0xFD81;

    public static final int RID_CURRENTTXRATE3 = 0xFD82;

    public static final int RID_CURRENTTXRATE4 = 0xFD83;

    public static final int RID_CURRENTTXRATE5 = 0xFD84;

    public static final int RID_CURRENTTXRATE6 = 0xFD85;

    public static final int RID_OWNMACADDRESS = 0xFD86;

    // public static final int RID_PCFINFO = 0xFD87;
    public static final int RID_SCANRESULTS = 0xFD88; // NEW

    public static final int RID_HOSTSCANRESULTS = 0xFD89; // NEW

    public static final int RID_AUTHENTICATIONUSED = 0xFD8A; // NEW

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
     Information RIDs:  Modem Information
     --------------------------------------------------------------------*/
    public static final int RID_PHYTYPE = 0xFDC0;

    public static final int RID_CURRENTCHANNEL = 0xFDC1;

    public static final int RID_CURRENTPOWERSTATE = 0xFDC2;

    public static final int RID_CCAMODE = 0xFDC3;

    public static final int RID_SUPPORTEDDATARATES = 0xFDC6;

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

    // ------ Information types -----------------
    public static final int IT_HANDOVERADDR = 0xF000;

    public static final int IT_COMMTALLIES = 0xF100;

    public static final int IT_SCANRESULTS = 0xF101;

    public static final int IT_CHINFORESULTS = 0xF102;

    public static final int IT_HOSTSCANRESULTS = 0xF103;// NEW

    public static final int IT_LINKSTATUS = 0xF200;

    public static final int IT_ASSOCSTATUS = 0xF201;

    public static final int IT_AUTHREQ = 0xF202;

    public static final int IT_PSUSERCNT = 0xF203;

    public static final int IT_KEYIDCHANGED = 0xF204;// NEW AP

    // ------ Link status values ----------------
    public static final int LINK_NOTCONNECTED = 0;

    public static final int LINK_CONNECTED = 1;

    public static final int LINK_DISCONNECTED = 2;

    public static final int LINK_AP_CHANGE = 3;

    public static final int LINK_AP_OUTOFRANGE = 4;

    public static final int LINK_AP_INRANGE = 5;

    public static final int LINK_ASSOCFAIL = 6;

}
