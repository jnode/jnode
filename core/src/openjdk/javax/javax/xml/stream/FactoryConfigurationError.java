package javax.xml.stream;

/**
 * An error class for reporting factory configuration errors.
 *
 * @version 1.0
 * @author Copyright (c) 2003 by BEA Systems. All Rights Reserved.
 * @since 1.6
 */
public class FactoryConfigurationError extends Error {

  Exception nested;

  /**
   * Default constructor
   */
  public FactoryConfigurationError(){}

  /**
   * Construct an exception with a nested inner exception
   *
   * @param e the exception to nest
   */
  public FactoryConfigurationError(java.lang.Exception e){
    nested = e;
  }

  /**
   * Construct an exception with a nested inner exception
   * and a message
   *
   * @param e the exception to nest
   * @param msg the message to report
   */
  public FactoryConfigurationError(java.lang.Exception e, java.lang.String msg){
    super(msg);
    nested = e;
  }

  /**
   * Construct an exception with a nested inner exception
   * and a message
   *
   * @param msg the message to report
   * @param e the exception to nest
   */
  public FactoryConfigurationError(java.lang.String msg, java.lang.Exception e){
    super(msg);
    nested = e;
  }

  /**
   * Construct an exception with associated message
   *
   * @param msg the message to report
   */
  public FactoryConfigurationError(java.lang.String msg) {
    super(msg);
  }

  /**
   * Return the nested exception (if any)
   *
   * @return the nested exception or null
   */
  public Exception getException() {
    return nested;
  }
  

  /**
   * Report the message associated with this error
   * 
   * @return the string value of the message
   */
  public String getMessage() {
    String msg = super.getMessage();
    if(msg != null)
      return msg;
    if(nested != null){
      msg = nested.getMessage();
      if(msg == null)
        msg = nested.getClass().toString();
    }
    return msg;  
  }
  

  
}

