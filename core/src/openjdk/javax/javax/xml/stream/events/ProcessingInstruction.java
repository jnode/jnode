package javax.xml.stream.events;
/**
 * An interface that describes the data found in processing instructions
 * 
 * @version 1.0
 * @author Copyright (c) 2003 by BEA Systems. All Rights Reserved.
 * @since 1.6
 */
public interface ProcessingInstruction extends XMLEvent {

  /**
   * The target section of the processing instruction
   *
   * @return the String value of the PI or null
   */
  public String getTarget();

  /**
   * The data section of the processing instruction
   *
   * @return the String value of the PI's data or null
   */
  public String getData();
}
