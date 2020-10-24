package rafikov.uniqueWords;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class HtmlReader extends DefaultHandler {
    public HtmlReader() {
    }

    public void read(String filePath) {
        Parser parser = new Parser();
        try {
            parser.setContentHandler(this);
            parser.setErrorHandler(this);
            InputSource source = new InputSource(filePath);
            parser.parse(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes attributes) throws SAXException {
        System.out.print("End element: ");
        System.out.println(qName);
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        System.out.print("End element: ");
        System.out.println(qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        System.out.print("characters: ");
        System.out.println(new String(ch, start, length));
    }
}
