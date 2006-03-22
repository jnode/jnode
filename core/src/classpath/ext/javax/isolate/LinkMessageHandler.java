package javax.isolate;

/**
 * Listener interface for receiving messages and exceptions from an
 * IsolateMessageDispatcher.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface LinkMessageHandler {

    /**
     * Invoked when a message is received by a link managed by an
     * IsolateMessageDispatcher.
     * 
     * @param dispatcher
     * @param link
     * @param message
     */
    public void messageReceived(LinkMessageDispatcher dispatcher,
            Link link, LinkMessage message);

    /**
     * Invoked when an exception is thrown due to the given dispatcher
     * attempting to receive from the link registered with this listener.
     * 
     * @param dispatcher
     * @param link
     * @param throwable
     */
    public void receiveFailed(LinkMessageDispatcher dispatcher,
            Link link, Throwable throwable);
}