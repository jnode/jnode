/*
 * $Id$
 */
package org.jnode.util;

import org.jnode.system.BootLog;

/**
 * @author epr
 */
public class QueueProcessorThread extends Thread
{

  /**
   * The queue i'm processing
   */
  private final Queue queue;
  /**
   * The actual processor
   */
  private final QueueProcessor processor;
  private boolean stop;

  /**
   * Create a new instance
   *
   * @param name
   * @param queue
   * @param processor
   */
  public QueueProcessorThread(String name, Queue queue, QueueProcessor processor)
  {
    super(name);
    this.queue = queue;
    this.processor = processor;
    this.stop = false;
  }

  /**
   * Create a new instance. A new queue is automatically created.
   *
   * @param name
   * @param processor
   * @see #getQueue()
   */
  public QueueProcessorThread(String name, QueueProcessor processor)
  {
    this(name, new Queue(), processor);
  }

  /**
   * Stop the processor
   */
  public void stopProcessor()
  {
    this.stop = true;
    //this.interrupt();
  }

  /**
   * Handle an exception thrown during the processing of the object.
   *
   * @param ex
   */
  protected void handleException(Exception ex)
  {
    BootLog.error("Exception in QueueProcessor: " + getName(), ex);
  }

  /**
   * Handle an exception thrown during the processing of the object.
   *
   * @param ex
   */
  protected void handleError(Error ex)
  {
    BootLog.error("Error in QueueProcessor: " + getName(), ex);
  }

  /**
   * Thread runner
   *
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    while (!stop)
    {
      try
      {
        final Object object = queue.get(false);
        if (object != null)
        {
          processor.process(object);
        }
      }
      catch (Exception ex)
      {
        handleException(ex);
      }
      catch (Error ex)
      {
        handleError(ex);
      }
    }
  }

  /**
   * Gets this queue this thread works on.
   *
   * @return The queue
   */
  public Queue getQueue()
  {
    return queue;
  }
}
