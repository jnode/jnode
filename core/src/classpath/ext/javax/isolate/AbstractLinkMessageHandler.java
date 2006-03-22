/*
 * $Id$
 */
package javax.isolate;

public abstract class AbstractLinkMessageHandler implements LinkMessageHandler {

    /**
     * Silently ignore this message.
     * 
     * @see javax.isolate.LinkMessageHandler#receiveFailed(javax.isolate.LinkMessageDispatcher, javax.isolate.Link, java.lang.Throwable)
     */
    public void receiveFailed(LinkMessageDispatcher dispatcher, Link link, Throwable throwable) {
        // Silently ignored        
    }

}
