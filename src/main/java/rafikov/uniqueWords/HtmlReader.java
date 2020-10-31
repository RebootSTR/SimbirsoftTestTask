package rafikov.uniqueWords;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

/**
 * Class Extends DefaultHandler SAX classes.
 *
 * @author Aydar Rafikov
 */
public class HtmlReader extends DefaultHandler {

    public HtmlReader() {
    }

    /**
     * Start parsing html file in filepath.
     *
     * @param filePath path to html file
     * @throws IOException  Cant open file.
     * @throws SAXException Problems with parsing.
     */
    public void read(String filePath) throws IOException, SAXException {
        Parser parser = new Parser();
        parser.setContentHandler(this);
        parser.setErrorHandler(this);
        InputSource source = new InputSource(filePath);
        source.setEncoding("UTF-8");
        parser.parse(source);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }
}
