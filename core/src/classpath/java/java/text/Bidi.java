/*
 * $Id$
 */
package java.text;

/**
 * This class implements the Unicode Version 3.0 Bidirectional Algorithm.
 * 
 * A Bidi object provides information on the bidirectional reordering of the text used to create it. This is required, for example, to properly display Arabic or Hebrew text. These languages are
 * inherently mixed directional, as they order numbers from left-to-right while ordering most other text from right-to-left.
 * 
 * Once created, a Bidi object can be queried to see if the text it represents is all left-to-right or all right-to-left. Such objects are very lightweight and this text is relatively easy to
 * process.
 * 
 * If there are multiple runs of text, information about the runs can be accessed by indexing to get the start, limit, and level of a run. The level represents both the direction and the 'nesting
 * level' of a directional run. Odd levels are right-to-left, while even levels are left-to-right. So for example level 0 represents left-to-right text, while level 1 represents right-to-left text,
 * and level 2 represents left-to-right text embedded in a right-to-left run.
 * 
 * THIS IS A STUBS ONLY IMPLEMENTATION
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @since 1.4
 */
public class Bidi {
	/**
	 * Constant indicating that the base direction depends on the first strong directional character in the text according to the Unicode Bidirectional Algorithm.
	 */
	public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = -2;

	/**
	 * Constant indicating that the base direction depends on the first strong directional character in the text according to the Unicode Bidirectional Algorithm.
	 */
	public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = -1;

	/**
	 * Constant indicating base direction is left-to-right.
	 */
	public static final int DIRECTION_LEFT_TO_RIGHT = 0;

	/**
	 * Constant indicating base direction is right-to-left.
	 */
	public static final int DIRECTION_RIGHT_TO_LEFT = 1;

	/**
	 * Create Bidi from the given paragraph of text.
	 * 
	 * @param paragraph
	 */
	public Bidi(AttributedCharacterIterator paragraph) {
		// TODO implement me
	}

	/**
	 * Create Bidi from the given text, embedding, and direction information.
	 * 
	 * @param text
	 * @param textStart
	 * @param embeddings
	 * @param embStart
	 * @param paragraphLength
	 * @param flags
	 */
	public Bidi(char[] text, int textStart, byte[] embeddings, int embStart, int paragraphLength, int flags) {
		// TODO implement me
	}

	/**
	 * Create Bidi from the given paragraph of text and base direction.
	 * 
	 * @param paragraph
	 * @param flags
	 */
	public Bidi(String paragraph, int flags) {
		// TODO implement me
	}

	/**
	 * Return true if the base direction is left-to-right.
	 * 
	 * @return
	 */
	public boolean baseIsLeftToRight() {
		// TODO implement me
		return false;
	}

	/**
	 * Create a Bidi object representing the bidi information on a line of text within the paragraph represented by the current Bidi.
	 * 
	 * @param lineStart
	 * @param lineLimit
	 * @return
	 */
	public Bidi createLineBidi(int lineStart, int lineLimit) {
		// TODO implement me
		return null;
	}

	/**
	 * Return the base level (0 if left-to-right, 1 if right-to-left).
	 * 
	 * @return
	 */
	public int getBaseLevel() {
		// TODO implement me
		return 0;
	}

	/**
	 * Return the length of text in the line.
	 * 
	 * @return
	 */
	public int getLength() {
		// TODO implement me
		return 0;
	}

	/**
	 * Return the resolved level of the character at offset.
	 * 
	 * @param offset
	 * @return
	 */
	public int getLevelAt(int offset) {
		// TODO implement me
		return 0;
	}

	/**
	 * Return the number of level runs.
	 */
	public int getRunCount() {
		// TODO implement me
		return 0;
	}

	/**
	 * Return the level of the nth logical run in this line.
	 * 
	 * @param run
	 * @return
	 */
	public int getRunLevel(int run) {
		// TODO implement me
		return 0;
	}

	/**
	 * Return the index of the character past the end of the nth logical run in this line, as an offset from the start of the line.
	 * 
	 * @param run
	 * @return
	 */
	public int getRunLimit(int run) {
		// TODO implement me
		return 0;
	}

	/**
	 * Return the index of the character at the start of the nth logical run in this line, as an offset from the start of the line.
	 * 
	 * @param run
	 * @return
	 */
	public int getRunStart(int run) {
		// TODO implement me
		return 0;
	}

	/**
	 * Return true if the line is all left-to-right text and the base direction is left-to-right.
	 * 
	 * @return
	 */
	public boolean isLeftToRight() {
		// TODO implement me
		return false;
	}

	/**
	 * Return true if the line is not left-to-right or right-to-left.
	 * 
	 * @return
	 */
	public boolean isMixed() {
		// TODO implement me
		return false;
	}

	/**
	 * Return true if the line is all right-to-left text, and the base direction is right-to-left.
	 * 
	 * @return
	 */
	public boolean isRightToLeft() {
		// TODO implement me
		return false;
	}

	/**
	 * Reorder the objects in the array into visual order based on their levels.
	 * 
	 * @param levels
	 * @param levelStart
	 * @param objects
	 * @param objectStart
	 * @param count
	 */
	public static void reorderVisually(byte[] levels, int levelStart, Object[] objects, int objectStart, int count) {
		// TODO implement me
	}

	/**
	 * Return true if the specified text requires bidi analysis.
	 * 
	 * @param text
	 * @param start
	 * @param limit
	 * @return boolean
	 */
	public static boolean requiresBidi(char[] text, int start, int limit) {
		// TODO implement me
		return false;
	}

	/**
	 * Display the bidi internal state, used in debugging.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// TODO implement me
		return super.toString();
	}
}
