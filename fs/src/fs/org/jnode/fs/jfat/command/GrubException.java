package org.jnode.fs.jfat.command;

public class GrubException extends Exception {
	public GrubException(String message) {
		super(message);
	}

	public GrubException(String message, Throwable cause) {
		super(message, cause);
	}
}
