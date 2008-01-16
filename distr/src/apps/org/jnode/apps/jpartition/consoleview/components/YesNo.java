package org.jnode.apps.jpartition.consoleview.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;

public class YesNo extends Component {
	private static final Logger log = Logger.getLogger(YesNo.class);
		
	public YesNo(Context context) {
		super(context);
	}

	public boolean show(String question) throws IOException {
		checkNonNull("question", question);
		
		print(question);
		
		return readBoolean(false);
	}
}
