/**
 * 
 */
package org.jnode.configure.adapter;

import org.jnode.configure.ConfigureException;

/**
 * Encode / decode for property values in classic Java property files. (This
 * will be used when a property file is written by template expansion. If it is
 * read or written using a {@link java.util.Properties} "load" or "save" method,
 * the method will take care of encoding / decoding.)
 * 
 * @author crawley@jnode.org
 */
class PropertyValueCodec implements BasePropertyFileAdapter.ValueCodec {
    public String decodeText(String encoded) throws ConfigureException {
        throw new UnsupportedOperationException("decodeText not supported (or used)");
    }

    public String encodeText(String raw) throws ConfigureException {
        StringBuffer sb = new StringBuffer(raw.length());
        for (char ch : raw.toCharArray()) {
            switch (ch) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (ch < ' ' || (ch >= 127 && ch < 160) || ch > 255) {
                        String digits = Integer.toHexString(ch);
                        sb.append("\\u");
                        if (digits.length() < 4) {
                            sb.append("0000".substring(digits.length())).append(digits);
                        } else {
                            sb.append(digits);
                        }
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
}
