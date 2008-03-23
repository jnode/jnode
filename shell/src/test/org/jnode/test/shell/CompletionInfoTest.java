package org.jnode.test.shell;

import java.util.Iterator;
import java.util.SortedSet;

import org.jnode.driver.console.CompletionInfo;

import junit.framework.TestCase;

/**
 * Test key methods of the CompletionInfo class.
 * 
 * @author crawley@jnode.org
 *
 */
public class CompletionInfoTest extends TestCase {
    
    public void testConstructor() {
        new CompletionInfo();
    }
    
    public void testAddCompletion() {
        CompletionInfo ci = new CompletionInfo();
        
        ci.addCompletion("full-1");
        ci.addCompletion("full-2", false);
        ci.addCompletion("partial", true);
        SortedSet<String> completions = ci.getCompletions();
        assertEquals(3, completions.size());
        
        Iterator<String> it = completions.iterator();
        assertEquals("full-1 ", it.next());
        assertEquals("full-2 ", it.next());
        assertEquals("partial", it.next());
    }
    
    public void testSetCompletionStart() {
        CompletionInfo ci = new CompletionInfo();
        assertEquals(-1, ci.getCompletionStart());
        ci.setCompletionStart(-1);
        assertEquals(-1, ci.getCompletionStart());
        ci.setCompletionStart(1);
        assertEquals(1, ci.getCompletionStart());
        ci.setCompletionStart(1);
        assertEquals(1, ci.getCompletionStart());
        try {
            ci.setCompletionStart(2);
            fail("no exception");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }
    
    public void testGetCompletion() {
        CompletionInfo ci = new CompletionInfo();
        assertEquals(null, ci.getCompletion());
        
        ci.addCompletion("full-1");
        assertEquals("full-1 ", ci.getCompletion());
        
        ci.addCompletion("full-2");
        assertEquals("full-", ci.getCompletion());
        
        ci.addCompletion("partial", true);
        assertEquals(null, ci.getCompletion());
    }
}
