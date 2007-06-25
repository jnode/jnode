package javax.xml.stream;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;
/**
 *
 * This is the top level interface for parsing XML Events.  It provides
 * the ability to peek at the next event and returns configuration 
 * information through the property interface.
 *
 * @version 1.0
 * @author Copyright (c) 2003 by BEA Systems. All Rights Reserved.
 * @see XMLInputFactory
 * @see XMLEventWriter
 * @since 1.6
 */
public interface XMLEventReader extends Iterator {
  /**
   * Get the next XMLEvent 
   * @see XMLEvent
   * @throws XMLStreamException if there is an error with the underlying XML.
   * @throws NoSuchElementException iteration has no more elements.
   */
  public XMLEvent nextEvent() throws XMLStreamException;

  /**
   * Check if there are more events.  
   * Returns true if there are more events and false otherwise.
   * @return true if the event reader has more events, false otherwise
   */
  public boolean hasNext();

  /**
   * Check the next XMLEvent without reading it from the stream.
   * Returns null if the stream is at EOF or has no more XMLEvents.
   * A call to peek() will be equal to the next return of next().
   * @see XMLEvent
   * @throws XMLStreamException
   */
  public XMLEvent peek() throws XMLStreamException;

  /**
   * Reads the content of a text-only element. Precondition:
   * the current event is START_ELEMENT. Postcondition:
   * The current event is the corresponding END_ELEMENT.
   * @throws XMLStreamException if the current event is not a START_ELEMENT
   * or if a non text element is encountered
   */
  public String getElementText() throws XMLStreamException;

  /**
   * Skips any insignificant space events until a START_ELEMENT or
   * END_ELEMENT is reached. If anything other than space characters are
   * encountered, an exception is thrown. This method should
   * be used when processing element-only content because
   * the parser is not able to recognize ignorable whitespace if
   * the DTD is missing or not interpreted.
   * @throws XMLStreamException if anything other than space characters are encountered
   */
  public XMLEvent nextTag() throws XMLStreamException;

  /**
   * Get the value of a feature/property from the underlying implementation
   * @param name The name of the property
   * @return The value of the property
   * @throws IllegalArgumentException if the property is not supported
   */
  public Object getProperty(java.lang.String name) throws java.lang.IllegalArgumentException;  

  /**
   * Frees any resources associated with this Reader.  This method does not close the
   * underlying input source.
   * @throws XMLStreamException if there are errors freeing associated resources
   */
  public void close() throws XMLStreamException;
}



