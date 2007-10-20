package org.jnode.shell;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.CompletionException;

public interface Completable {
	
	void complete(CompletionInfo completion, CommandShell shell) throws CompletionException;

}
