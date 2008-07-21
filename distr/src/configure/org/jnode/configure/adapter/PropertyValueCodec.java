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
 * <p>
 * This codec supports the following modifiers:
 * <ul>
 * <li>'=' says to expand as &lt;propName&gt;=&lt;propValue&gt; rather than &lt;propValue&gt;.
 *     Properties that are not set (explicitly or by defaulting) will be suppressed.
 * <li>'#' (with '=') says to comment out suppressed properties rather than entirely omitting them.
 * <li>'!' (with '=') says to suppress a property whose value is an empty string.
 * </ul>
 * 
 * @author crawley@jnode.org
 */
class PropertyValueCodec implements BasePropertyFileAdapter.ValueCodec {

    public String encodeProperty(String propName, String propValue, String modifiers)  {
        if (modifiers.contains("=")) {
            if (propValue == null || propValue.equals("") && modifiers.contains("!")) {
                if (modifiers.contains("#")) {
                    return "#" + encodeText(propName) + "=";
                } else {
                    return "";
                }
            } else {
                return encodeText(propName) + "=" + encodeText(propValue);
            }
        } else {
            if (propValue == null) {
                return "";
            } else {
                return encodeText(propValue);
            }
        }
    }

    @Override
    public String getValidModifiers() {
        return "=!#";
    }

    private String encodeText(String raw) {
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
