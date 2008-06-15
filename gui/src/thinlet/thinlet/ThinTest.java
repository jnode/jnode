package thinlet;

import java.io.ByteArrayInputStream;
import java.awt.Frame;
import java.awt.BorderLayout;

/**
 * A simple test for Thinlet
 * 
 * @author Levente S\u00e1ntha
 */
public class ThinTest extends Thinlet {
    private static final String MARKUP =
            "<panel gap=\"4\" top=\"4\" left=\"4\">" + "<button text=\"Button Test\"/>"
                    + "<textarea text=\"TextArea\" wrap=\"true\" columns=\"30\" rows=\"10\" />"
                    + "<tree selection=\"multiple\">" + "<node text=\"Node A\">"
                    + "<node text=\"Node B\" selected=\"true\" />" + "<node text=\"Node C\" />"
                    + "</node>" + "<node text=\"Node D\" expanded=\"false\">"
                    + "<node text=\"Node E\" />" + "</node>" + "</tree>" + "</panel>";

    /*
     * "<panel gap=\"4\" top=\"4\" left=\"4\">" + "<textfield name=\"number1\"
     * columns=\"4\" />"+ "<label text=\"+\" />"+ "<textfield name=\"number2\"
     * columns=\"4\" />"+ "<button text=\"=\" action=\"calculate(number1.text,
     * number2.text, result)\" />"+ "<textfield name=\"result\"
     * editable=\"false\" />"+ "</panel>";
     */

    public ThinTest() throws Exception {
        add(parse(new ByteArrayInputStream(MARKUP.getBytes())));
    }

    public static void main(String[] args) throws Exception {
        Frame f = new Frame();
        f.setSize(200, 200);
        f.setLocation(200, 200);
        ThinTest test = new ThinTest();
        f.add(test, BorderLayout.CENTER);
        f.setVisible(true);
        test.requestFocus();
    }
}
