package org.jnode.net.wireless;

/** Authentication modes */
public final class AuthenticationMode {
    
    private AuthenticationMode() {}
    
    /** Open system mode */
    public static final AuthenticationMode OPENSYSTEM = new AuthenticationMode();
    /** Shared key mode */
    public static final AuthenticationMode SHAREDKEY = new AuthenticationMode();
}