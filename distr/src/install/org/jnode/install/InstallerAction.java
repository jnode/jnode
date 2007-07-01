/*
 * $Id$
 */
package org.jnode.install;

/**
 * @author Levente Sántha
 */
public interface InstallerAction {
    ActionInput getInput(InputContext inContext);
    void execute() throws Exception;
    ActionOutput getOutput(OutputContext outContext);
}
