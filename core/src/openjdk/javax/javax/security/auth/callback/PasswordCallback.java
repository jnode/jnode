/*
 * Copyright 1999-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package javax.security.auth.callback;

/**
 * <p> Underlying security services instantiate and pass a
 * <code>PasswordCallback</code> to the <code>handle</code>
 * method of a <code>CallbackHandler</code> to retrieve password information.
 *
 * @see javax.security.auth.callback.CallbackHandler
 */
public class PasswordCallback implements Callback, java.io.Serializable {

    private static final long serialVersionUID = 2267422647454909926L;

    /**
     * @serial
     * @since 1.4
     */
    private String prompt;
    /**
     * @serial
     * @since 1.4
     */
    private boolean echoOn;
    /**
     * @serial
     * @since 1.4
     */
    private char[] inputPassword;

    /**
     * Construct a <code>PasswordCallback</code> with a prompt
     * and a boolean specifying whether the password should be displayed
     * as it is being typed.
     *
     * <p>
     *
     * @param prompt the prompt used to request the password. <p>
     *
     * @param echoOn true if the password should be displayed
     *			as it is being typed.
     *
     * @exception IllegalArgumentException if <code>prompt</code> is null or
     *			if <code>prompt</code> has a length of 0.
     */
    public PasswordCallback(String prompt, boolean echoOn) {
	if (prompt == null || prompt.length() == 0)
	    throw new IllegalArgumentException();

	this.prompt = prompt;
	this.echoOn = echoOn;
    }

    /**
     * Get the prompt.
     *
     * <p>
     *
     * @return the prompt.
     */
    public String getPrompt() {
	return prompt;
    }

    /**
     * Return whether the password
     * should be displayed as it is being typed.
     *
     * <p>
     *
     * @return the whether the password
     *		should be displayed as it is being typed.
     */
    public boolean isEchoOn() {
	return echoOn;
    }

    /**
     * Set the retrieved password.
     *
     * <p> This method makes a copy of the input <i>password</i>
     * before storing it.
     * 
     * <p>
     *
     * @param password the retrieved password, which may be null.
     *
     * @see #getPassword
     */
    public void setPassword(char[] password) {
        this.inputPassword = (password == null ? null : password.clone());
    }

    /**
     * Get the retrieved password.
     *
     * <p> This method returns a copy of the retrieved password.
     *
     * <p>
     *
     * @return the retrieved password, which may be null.
     *
     * @see #setPassword
     */
    public char[] getPassword() {
        return (inputPassword == null ? null : inputPassword.clone());
    }

    /**
     * Clear the retrieved password.
     */
    public void clearPassword() {
	if (inputPassword != null) {
	    for (int i = 0; i < inputPassword.length; i++)
		inputPassword[i] = ' ';
	}
    }
}
