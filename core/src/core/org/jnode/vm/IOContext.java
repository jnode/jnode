/*
 * $Id$
 */
package org.jnode.vm;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Levente S\u00e1ntha
 */
public interface IOContext {
    void setGlobalInStream(InputStream in);
    InputStream getGlobalInStream();
    void setGlobalOutStream(PrintStream out);
    PrintStream getGlobalOutStream();
    void setGlobalErrStream(PrintStream err);
    PrintStream getGlobalErrStream();
    void setSystemIn(InputStream in);
    void setSystemOut(PrintStream out);
    void setSystemErr(PrintStream err);
    void enterContext();
    void exitContext();
}
