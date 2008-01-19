/*
 * $
 */
package gnu.java.net;

import java.net.DatagramSocketImplFactory;

/**
 *
 * @author Levente S\u00e1ntha
 */
public interface PlainDatagramSocketImplFactory extends DatagramSocketImplFactory {
    public PlainDatagramSocketImpl createPlainDatagramSocketImpl();
}
