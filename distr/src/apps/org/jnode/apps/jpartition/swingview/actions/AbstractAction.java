package org.jnode.apps.jpartition.swingview.actions;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Bounded;
import org.jnode.apps.jpartition.swingview.DiskAreaView;

abstract class AbstractAction<T extends DiskAreaView<? extends Bounded>>  extends javax.swing.AbstractAction {
	protected final Logger log = Logger.getLogger(getClass());

	protected final ErrorReporter errorReporter;
	protected final T view;

	public AbstractAction(String name, ErrorReporter errorReporter, T view) {
		super(name);
		this.errorReporter = errorReporter;
		this.view = view;
	}
}
