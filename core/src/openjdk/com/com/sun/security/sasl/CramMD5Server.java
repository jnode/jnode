/*
 * Copyright 2003-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.security.sasl; 

import javax.security.sasl.*;
import javax.security.auth.callback.*;
import java.util.Random;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
  * Implements the CRAM-MD5 SASL server-side mechanism. 
  * (<A HREF="ftp://ftp.isi.edu/in-notes/rfc2195.txt">RFC 2195</A>).
  * CRAM-MD5 has no initial response. 
  *
  * client <---- M={random, timestamp, server-fqdn} ------- server
  * client ----- {username HMAC_MD5(pw, M)} --------------> server
  *
  * CallbackHandler must be able to handle the following callbacks:
  * - NameCallback: default name is name of user for whom to get password
  * - PasswordCallback: must fill in password; if empty, no pw
  * - AuthorizeCallback: must setAuthorized() and canonicalized authorization id
  *      - auth id == authzid, but needed to get canonicalized authzid
  *
  * @author Rosanna Lee
  */
final class CramMD5Server extends CramMD5Base implements SaslServer {
    private String fqdn;
    private byte[] challengeData = null;
    private String authzid;
    private CallbackHandler cbh;

    /**
     * Creates a SASL mechanism with client credentials that it needs 
     * to participate in CRAM-MD5 authentication exchange with the server.
     *
     * @param authID A  non-null string representing the principal 
     * being authenticated.
     *
     * @param pw A non-null String or byte[]
     * containing the password. If it is an array, it is first cloned.
     */
    CramMD5Server(String protocol, String serverFqdn, Map props, 
	CallbackHandler cbh) throws SaslException {
	if (serverFqdn == null) {
	    throw new SaslException(
		"CRAM-MD5: fully qualified server name must be specified");
	}
	
	fqdn = serverFqdn;
	this.cbh = cbh;
    }

    /**
     * Generates challenge based on response sent by client.
     * 
     * CRAM-MD5 has no initial response.
     * First call generates challenge.
     * Second call verifies client response. If authentication fails, throws
     * SaslException.
     *
     * @param responseData A non-null byte array containing the response
     * 	      data from the client.
     * @return A non-null byte array containing the challenge to be sent to 
     * 	      the client for the first call; null when 2nd call is successful.
     * @throws SaslException If authentication fails.
     */
    public byte[] evaluateResponse(byte[] responseData)
	throws SaslException {

        // See if we've been here before
	if (completed) {
	    throw new IllegalStateException(
		"CRAM-MD5 authentication already completed");
	}
	
	if (aborted) {
	    throw new IllegalStateException(
		"CRAM-MD5 authentication previously aborted due to error");
	}

	try {
	    if (challengeData == null) {
		if (responseData.length != 0) {
		    aborted = true;
		    throw new SaslException(
			"CRAM-MD5 does not expect any initial response");
		}

		// Generate challenge {random, timestamp, fqdn}
		Random random = new Random();
		long rand = random.nextLong();
		long timestamp = System.currentTimeMillis();

		StringBuffer buf = new StringBuffer();
		buf.append('<');
		buf.append(rand);
		buf.append('.');
		buf.append(timestamp);
		buf.append('@');
		buf.append(fqdn);
		buf.append('>');
		String challengeStr = buf.toString();

		logger.log(Level.FINE, 
		    "CRAMSRV01:Generated challenge: {0}", challengeStr);
	    
		challengeData = challengeStr.getBytes("UTF8");
                return challengeData.clone();

	    } else {
		// Examine response to see if correctly encrypted challengeData
		if(logger.isLoggable(Level.FINE)) {
		    logger.log(Level.FINE, 
			"CRAMSRV02:Received response: {0}",
			new String(responseData, "UTF8"));
		}

		// Extract username from response
		int ulen = 0;
		for (int i = 0; i < responseData.length; i++) {
		    if (responseData[i] == ' ') {
			ulen = i;
			break;
		    }
		}
		if (ulen == 0) {
		    aborted = true;
		    throw new SaslException(
			"CRAM-MD5: Invalid response; space missing");
		}
		String username = new String(responseData, 0, ulen, "UTF8");

		logger.log(Level.FINE, 
		    "CRAMSRV03:Extracted username: {0}", username);
		
		// Get user's password
		NameCallback ncb =
		    new NameCallback("CRAM-MD5 authentication ID: ", username);
		PasswordCallback pcb = 
		    new PasswordCallback("CRAM-MD5 password: ", false);
		cbh.handle(new Callback[]{ncb,pcb});
		char pwChars[] = pcb.getPassword();
		if (pwChars == null || pwChars.length == 0) {
		    // user has no password; OK to disclose to server
		    aborted = true;
		    throw new SaslException(
			"CRAM-MD5: username not found: " + username);
		}
		pcb.clearPassword();
		String pwStr = new String(pwChars);
		for (int i = 0; i < pwChars.length; i++) {
		    pwChars[i] = 0;
		}
		pw = pwStr.getBytes("UTF8");
		
		// Generate a keyed-MD5 digest from the user's password and 
		// original challenge.
		String digest = HMAC_MD5(pw, challengeData);

		logger.log(Level.FINE, 
		    "CRAMSRV04:Expecting digest: {0}", digest);

		// clear pw when we no longer need it
		clearPassword();

		// Check whether digest is as expected
		byte [] expectedDigest = digest.getBytes("UTF8");
		int digestLen = responseData.length - ulen - 1;
		if (expectedDigest.length != digestLen) {
		    aborted = true;
		    throw new SaslException("Invalid response");
		}
		int j = 0;
		for (int i = ulen + 1; i < responseData.length ; i++) {
		    if (expectedDigest[j++] != responseData[i]) {
			aborted = true;
			throw new SaslException("Invalid response");
		    }
		}

		// All checks out, use AuthorizeCallback to canonicalize name
		AuthorizeCallback acb = new AuthorizeCallback(username, username);
		cbh.handle(new Callback[]{acb});
		if (acb.isAuthorized()) {
		    authzid = acb.getAuthorizedID();
		} else {
		    // Not authorized
		    aborted = true;
		    throw new SaslException(
			"CRAM-MD5: user not authorized: " + username);
		}

		logger.log(Level.FINE,
		    "CRAMSRV05:Authorization id: {0}", authzid);

		completed = true;
		return null;
	    }
	} catch (UnsupportedEncodingException e) {
	    aborted = true;
	    throw new SaslException("UTF8 not available on platform", e);
	} catch (NoSuchAlgorithmException e) {
	    aborted = true;
	    throw new SaslException("MD5 algorithm not available on platform", e);
	} catch (UnsupportedCallbackException e) {
	    aborted = true;
	    throw new SaslException("CRAM-MD5 authentication failed", e);
	} catch (SaslException e) {
	    throw e; // rethrow
	} catch (IOException e) {
	    aborted = true;
	    throw new SaslException("CRAM-MD5 authentication failed", e);
	}
    }

    public String getAuthorizationID() {
	if (completed) {
	    return authzid;
	} else {
	    throw new IllegalStateException(
		"CRAM-MD5 authentication not completed");
	}
    }
}
