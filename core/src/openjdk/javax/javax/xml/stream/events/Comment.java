package javax.xml.stream.events;

/**
 * An interface for comment events
 * 
 * @version 1.0
 * @author Copyright (c) 2003 by BEA Systems. All Rights Reserved.
 * @since 1.6
 */
public interface Comment extends XMLEvent {

  /**
   * Return the string data of the comment, returns empty string if it
   * does not exist
   */
  public String getText();
}
