package rafikov.uniqueWords;

import org.jsoup.Jsoup;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.Normalizer;
import java.util.*;

import static jdk.nashorn.internal.ir.debug.ObjectSizeCalculator.getObjectSize;

public class UniqueWords {
    private String url;
    private String path;
    private String fileName;
    private HashMap<String, Integer> wordsMap = new HashMap<>();

    public UniqueWords(String url, String path) {
        setURL(url);
        setPath(path);
    }

    public UniqueWords(String url) {
        this(url, "sites/");
    }

    public void setURL(String url) {
        if (url.matches(".*/")) {
            this.url = url.substring(0, url.length()-1);
        } else {
            this.url = url;
        }
        fileName = generateNameFromUrl(this.url);
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String generateNameFromUrl(String url) {
        return url.split("/")[2] + ".html";
    }

    public void loadPage() throws SiteConnectException {
        try {
            ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(path + fileName);
            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (MalformedURLException | FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            throw new SiteConnectException(url);
        }
    }

    public void parsePage() {
        HtmlReader reader = new HtmlReader() {
            List<String> formattingTags = Arrays.asList(
                    "strong", "b", "kbd",
                    "code", "samp", "big",
                    "small", "em", "i",
                    "dfn", "ins", "del",
                    "sub","sup");
            int countTags;
            final int limitTags = 20;
            StringBuilder partOfHTML = new StringBuilder();

            @Override
            public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) throws SAXException {
                partOfHTML.append(String.format("<%s>", qName));
                countTags++;
            }

            @Override
            public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
                partOfHTML.append(String.format("</%s>", qName));
                if (countTags >= limitTags && !formattingTags.contains(qName)) {
                    calculateWords();
                    countTags = 0;
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                partOfHTML.append(new String(ch, start, length));
            }

            @Override
            public void endDocument() throws SAXException {
                calculateWords();
            }

            private void calculateWords() {
                sendInDB(normalizeText(Jsoup.parse(partOfHTML.toString()).text()).split("\\s+"));
                partOfHTML.delete(0, partOfHTML.length());
            }
        };
        reader.read(path+fileName);
    }

    private String normalizeText(String line) {
        line = line.replaceAll("[,.!?\";:\\[\\]()\n\r\t«»]", " ");
        line = line.replaceAll("\\s{2,}", " ");
        return line.toUpperCase();
    }

    private void sendInDB(String[] words) {
        for (String word : words) {
            if (word.matches("[\\s-]") || word.length() == 0)
                continue;
            wordsMap.put(word, wordsMap.containsKey(word) ? wordsMap.get(word)+1 : 1);
        }
        // TODO: hashmap clear and DB
    }

    public String calculateUniqueWords() {
        System.out.printf("%f Кб\n", (double)getObjectSize(wordsMap) / 1024 );
        wordsMap.forEach((key,value) -> System.out.println(key + ": " + value));
        return "complete";
    }
}
