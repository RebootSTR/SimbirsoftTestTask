package rafikov.uniqueWords;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

public class HtmlReader extends DefaultHandler {
    public HtmlReader() {
    }

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
