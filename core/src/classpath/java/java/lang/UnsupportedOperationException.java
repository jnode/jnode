/**
 * $Id$
 */
package java.lang;

/**
 * <description>
 * 
 * @author epr
 */
public class UnsupportedOperationException extends RuntimeException {

	/**
	 * Constructor for UnsupportedOperationException.
	 */
	public UnsupportedOperationException() {
		super();
	}

	/**
	 * Constructor for UnsupportedOperationException.
	 * @param message
	 * @param cause
	 */
	public UnsupportedOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for UnsupportedOperationException.
	 * @param cause
	 */
	public UnsupportedOperationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for UnsupportedOperationException.
	 * @param s
	 */
	public UnsupportedOperationException(String s) {
		super(s);
	}

}
