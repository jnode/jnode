/*
 * $Id $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jnode.configure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnode.configure.PropertySet.Value;

/**
 * This class represents a property type defined by a regex.  The constructor
 * allows you to provide an "emptyToken" value.  If provided, this value defines
 * a special token that will be mapped to the empty string by the {@link fromToken}
 * method.
 * 
 * @author crawley@jnode.org
 */
public class PatternType extends PropertyType {
    
	private final Pattern pattern;
	private final String emptyToken;

	/**
	 * Construct a Pattern type.
	 * @param name the type's name.
	 * @param pattern the Java regex that defines the type's value space
	 * @param emptyToken if non-null, this specifies a input token that will
	 *        be mapped to the empty string value.  You should avoid using
	 *        characters that would require escaping in a Java regex.
	 */
    public PatternType(String name, Pattern pattern, String emptyToken) {
        super(name);
        this.pattern = pattern;
        this.emptyToken = emptyToken;
    }

    @Override
    public Value fromInput(String token) {
    	if (token.equals(emptyToken)) {
    		return new Value(token, "");
    	} else {
    		return fromValue(token);
        }
    }

    @Override
    public Value fromValue(String value) {
    	Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
        	if (value.equals("") && emptyToken != null) {
                return new Value(emptyToken, value);
        	} else {
                return new Value(value, value);
        	}
        } else {
            return null;
        }
    }

    @Override
    public String describe(Value defaultValue) {
    	StringBuffer sb = new StringBuffer();
    	sb.append("(/").append(pattern.toString()).append("/");
    	if (emptyToken != null) {
    		sb.append(" or '").append(emptyToken).append("'");
    	}
        if (defaultValue != null) {
        	String dv = defaultValue.getToken();
        	sb.append(" [").append(dv).append("]");
        }
        sb.append(")");
        return sb.toString();
    }
}
