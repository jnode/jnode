/* 
 * $Id$
 */
package org.jnode.apps.debug;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;

/**
 * @author blind
 *
 */
public abstract class RootObjectPanel extends ListPanel {
	/**
	 * @param cols
	 * @param rows
	 */
	public RootObjectPanel(int cols, int rows) {
		super(cols, rows);
	}

	public void keyEntered(char c) {
		if(c=='+'){
			//get the currently selected element
	    	int selectedIndex = jlist.getSelectedIndex();
	    	if((selectedIndex>=0) && (selectedIndex<list.size())) {	//need to this check because 
	    		//something is screwed: sometimes strange values are returned by getSelectedIndex()
	    		Object o   = list.elementAt(jlist.getSelectedIndex());
	    		Object res = ((ListElement)o).getValue();
	    		if(res instanceof Collection)
	    			fill((Collection)res);
	    		//if(res instanceof Naming)
	    		//	fill(); //TODO: ...

				//if res is an array...	    			
	    	}
		}
	}
	
	protected void fill() {
		Set allNames = InitialNaming.nameSet();
		final Vector list = new Vector();
		try {
			for (Iterator it = allNames.iterator(); it.hasNext();) {
				Object key = it.next();
				Object namedObject = InitialNaming.lookup((Class) key);
				final ListElement element = new ListElement(namedObject, key.toString());
				list.addElement(element);
			}
		} catch (NameNotFoundException nnfe) {
			nnfe.printStackTrace();
		}
		setList(list);
	}
	
	protected void fill(Collection coll){
		final Vector list = new Vector();
		for(Iterator it = coll.iterator(); it.hasNext(); ){
			Object o = it.next();
			final ListElement element = new ListElement(o, getElementLabel(o));
			list.addElement(element);
		}
		setList(list);
	}
}
