//
// BuildException.java
//

package org.jnode.build;

public class BuildException
extends org.apache.tools.ant.BuildException
{
	/**
	 * Constructor for BuildException.
	 */
	public BuildException() {
		super();
	}

	/**
	 * Constructor for BuildException.
	 * @param message
	 * @param cause
	 */
	public BuildException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for BuildException.
	 * @param cause
	 */
	public BuildException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for BuildException.
	 * @param s
	 */
	public BuildException(String s) {
		super(s);
	}

}