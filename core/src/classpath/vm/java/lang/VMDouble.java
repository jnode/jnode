/*
 * $Id$
 */
package java.lang;

/**
 * VM specific double routines.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class VMDouble {
	private char[] chars;
	private int length;
	private int index;

	private int parseUnsignedInt() throws NumberFormatException {
		if (index >= length)
			throw new NumberFormatException();

		int start = index;
		int value = 0;

		for (; index < length; index++) {
			int d = Character.digit(chars[index], 10);
			if (d < 0)
				break;
			value *= 10;
			value += d;
		}

		if (index == start)
			throw new NumberFormatException();

		return value;
	}

	private int parseSignedInt() throws NumberFormatException {
		if (index >= length)
			throw new NumberFormatException();

		char sign = ' ';

		switch (chars[index]) {
			case '-' :
				sign = '-';
				index++;
				break;

			case '+' :
				sign = '+';
				index++;
				break;
		}

		int value = parseUnsignedInt();

		return (sign == '-') ? -value : value;
	}

	private double parseFractionalPart(boolean nonEmpty)
		throws NumberFormatException {
		if (index >= length)
			throw new NumberFormatException();

		int start = index;
		double value = 0.0d;

		for (; index < length; index++) {
			int d = Character.digit(chars[index], 10);
			if (d < 0)
				break;
			value += d;
			value /= 10;
		}

		if (nonEmpty && (index == start))
			throw new NumberFormatException();

		return value;
	}

	private double parseExponent(double value)
		throws NumberFormatException {
		if (index >= chars.length)
			return value;

		switch (chars[index]) {
			case 'e' :
			case 'E' :
				index++;
				break;

			default :
				throw new NumberFormatException();
		}

		int exponent = parseSignedInt();

		if (index < chars.length)
			throw new NumberFormatException();

		return value * Math.pow(10.0, exponent);
	}

	public double parse() throws NumberFormatException {
		if (index >= chars.length)
			throw new NumberFormatException();

		char sign = '+';

		switch (chars[index]) {
			case '-' :
				sign = '-';
				index++;
				break;

			case '+' :
				sign = '+';
				index++;
				break;
		}

		if (index >= chars.length)
			throw new NumberFormatException();

		double value;

		if (chars[index] == '.') {
			index++;
			value = parseFractionalPart(true);
			value = parseExponent(value);
		} else {
			value = parseUnsignedInt();

			if (index >= chars.length)
				throw new NumberFormatException();
			if (chars[index] != '.')
				throw new NumberFormatException();

			index++;

			value += parseFractionalPart(false);
			value = parseExponent(value);
		}

		return (sign == '-') ? -value : value;
	}

	public VMDouble(String s) {
		chars = s.toCharArray();
		length = chars.length;
		index = 0;
	}
}