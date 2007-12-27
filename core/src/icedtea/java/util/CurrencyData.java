package java.util;

class CurrencyData {

    static final String version = "134";

    static final String mainTable = "\u007f\u007f\u007f\u0081CM\u0082\u007f\u0082\u007f\u007fKCF@\u007f\u0080R\u0083\u0081C\u007fF\u0081\u007f\u0084" +
                                    "LC\u007fS\u0081\u0085Mc\u0005\u0085\u007f\u007fCCA\u007f\u007fKCM\u007f\u0086O\u007f\u0011C" +
                                    "C\u007f\u0087E\u007f\u0088\u0088E\u0085\u007f\u0089\u000f\u0088XO\u007f\u007fBC\u007fOD\u007f\u0087OJ" +
                                    "\u007f\u007f\u007f\u007f\u0081\u007f\u007f\u007f\u007f\u0005J\u007f\u0082\u007fO\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007fC" +
                                    "\u007f\u007f\u0083\u007fJ\u007fO\u008a\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007fM\u0081A\u007f\u007f\u007f\u007f\u007f\u007f" +
                                    "\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u0081CO\u007f\u0083\u007f\u008b\u007f\u007f\u0081\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f" +
                                    "\u0088O\u007f\u0082K\u0081\u007fBO\u007f\u007f\u008bC\u0005\u007f\u0081\u0088\u0081\u008cP\u0083\u007f\u0085\u007fC\u007f" +
                                    "\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007fC\u007f\u0087K\u007f\u007f\u007fJ\u007fFE\u007f\u007f\u007f\u007f\u007f" +
                                    "\u007f\u007f\u007fQ\u0081\u007f\u007f\u007f\u007f\u007f\u007fR\u007fQ\u0083\u007fcQJ\u0081\u007f\u007f\u007f\u007f\u007f\u007f" +
                                    "\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007fC\u007fc\u0018\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f" +
                                    "\u007f\u007f\u007f\u007fR\u007fRQ\u0087\u007f\u007f\u007f\u0005\u0082\u007fV\u007f\u0016\u007f\u007f\u007f\u007fc\u007fCS" +
                                    "JO\u0082\u007f\u007f\u007f\u007f\u007f\u008d\u007fQ\u007f\u007f\u007f\u007f\u007f\u007fCKK\u0081K\u007f\u007fc\u007f" +
                                    "C\u007f\u0081K\u0081\u007f@\u0083\u007f\u007fC\u0085JSO\u0083\u0081N\u0082KQQJMQ\u008e" +
                                    "C\u007f\u008f\u007f\u0085\u0087M\u007fN\u007f\u007f\u0081\u007f\u007fJQ\u007f\u0087\u007f\u007f\u0089\u007f\u007f\u007f\u007fC" +
                                    "\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007fq\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f" +
                                    "A\u007f\u007f\u007fM\u008fJO\u007f\u007fQM\u0081\u0089\u007f\u007f\u007f\u0083\u0090\u0081\u007f\u007f\u0083\u007f\u0006\u007f" +
                                    "Q\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f" +
                                    "\u007f\u007f\u007f\u007f\u0081\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u0091\u007f\u007f\u007fC\u007fA\u007f\u0005\u007f\u007f\u007f" +
                                    "QCQCJ\u007fCO\u0081\u0086JK\u0081\u0085R\u007f\u007fC\u007fC\u007fB\u007f\u007fOK" +
                                    "\u007f\u007f\u0083\u0088\u007f\u0081\u0085A\u007fR\u0089\u0083LcO\u007f\u007f\u0092\u007fC\u007f\u0087C\u007f\u007fR" +
                                    "G\u007f\u007f\u007f\u007f\u007fW\u007f\u007f\u007f\u007f\u007f\u0083\u007f\u007f\u007f\u007f\u007fC\u007f\u007f\u007f\u007f\u007fTR" +
                                    "\u0081\u007f\u0082\u007fA\u007f\u0083\u007f\u0083\u007f\u007f\u007f\u007fC\u007f\u007f\u007f\u007f\u007f\u007f\u0015\u007f\u007f\u007f\u007f\u007f" +
                                    "\u007f\u007f\u007f\u007f\u007f\u008f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007fS\u007f\u007f\u007f\u007f\u007f\u007f\u007f" +
                                    "\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f" +
                                    "\u007f\u007f\u007f\u007fQ\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u0081\u007f\u007f\u007f\u007f\u007f\u007f" +
                                    "Q\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007fJ\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007f\u007fC\u007f\u007f\u007f";

    static final long[] scCutOverTimes = { 9223372036854775807L, 9223372036854775807L, 9223372036854775807L, 1136059200000L, 9223372036854775807L, 9223372036854775807L, 9223372036854775807L, 9223372036854775807L, 9223372036854775807L, 9223372036854775807L, 9223372036854775807L, 9223372036854775807L, 9223372036854775807L, 1151704800000L, 9223372036854775807L, 9223372036854775807L, 1120165200000L, 1104530400000L, };

    static final String[] scOldCurrencies = { "EUR", "XCD", "USD", "AZM", "XOF", "NOK", "AUD", "XAF", "NZD", "MAD", "DKK", "GBP", "CHF", "MZM", "XPF", "ILS", "ROL", "TRL", };

    static final String[] scNewCurrencies = { null, null, null, "AZN", null, null, null, null, null, null, null, null, null, "MZN", null, null, "RON", "TRY", };

    static final int[] scOldCurrenciesDFD = { 2, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 0, };

    static final int[] scNewCurrenciesDFD = { 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 2, };

    static final String otherCurrencies = "ADP-AFA-ATS-AYM-AZM-AZN-BEF-BGL-BOV-BYB-CLF-DEM-ESP-EUR-FIM-FRF-GRD-GWP-IEP-ITL-LUF-MGF-MXV-MZM-MZN-NLG-PTE-ROL-RON-RUR-SIT-SRG-TPE-TRL-TRY-USN-USS-XAF-XAG-XAU-XBA-XBB-XBC-XBD-XCD-XDR-XFO-XFU-XOF-XPD-XPF-XPT-XTS-XXX-YUM-ZWN";

    static final int[] otherCurrenciesDFD = { 0, 2, 2, 2, 2, 2, 0, 2, 2, 0, 0, 2, 0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 0, -1, -1, -1, -1, -1, -1, 2, -1, -1, -1, 0, -1, 0, -1, -1, -1, 2, 2, };

}

