package rafikov.uniqueWords;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import rafikov.uniqueWords.exceptions.DataBaseException;
import rafikov.uniqueWords.exceptions.SiteConnectException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.function.Consumer;

public class UniqueWords {
    private static final Logger logger = LoggerFactory.getLogger(UniqueWords.class);

    private String url;
    private String path;
    private String fileName;
    private DBHandler database;
    private HashMap<String, Integer> wordsMap = new HashMap<>();

    public UniqueWords(String url, String path) {
        setURL(url);
        setPath(path);
    }

    public UniqueWords(String url) {
        this(url, "sites/");
    }

    public void setURL(String url) {
        if (!url.matches("https?://.*")) {
            logger.debug("adding https:// to URL. URL={}", url);
            url = "https://" + url;
        }
        this.url = url;
        fileName = generateNameFromUrl(this.url);
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String generateNameFromUrl(String url) {
        logger.debug("generating filename for url={}", url);
        return url.split("/")[2] + ".html";
    }

    public void loadPage() throws SiteConnectException {
        try {
            logger.debug("open readableByteChannel");
            ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
            logger.debug("open fileOutputStream");
            FileOutputStream fileOutputStream = new FileOutputStream(path + fileName);
            logger.debug("transfer channel");
            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            logger.info("Page loaded!");
        } catch (MalformedURLException | FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            throw new SiteConnectException(url);
        }
    }

    private void databaseConnect() throws DataBaseException {
        database = DBHandler.getInstance(
                "unique words.db",
                "`" + fileName.replaceAll("\\.", "_") + "`");
        database.connect();
    }

    public void parsePage() throws DataBaseException {
        logger.debug("creating htmlreader");
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
        databaseConnect();
        database.createTable();
        long time = System.currentTimeMillis();
        logger.info("Page parse started");
        reader.read(path+fileName);
        logger.info("Page parsed and writing to database in: {}{}", (double) (System.currentTimeMillis() - time) / 1000, "s");
        database.close();
    }

    private String normalizeText(String line) {
        logger.debug("text started to normalize: {}", line);
        line = line.replaceAll("[,.!?\";:\\[\\]()\n\r\t«»]", " ");
        line = line.replaceAll("\\s{2,}", " ");
        return line.toUpperCase();
    }

    private void sendInDB(String[] words) {
        logger.debug("Send in db started");
        for (String word : words) {
            if (word.length() == 0)
                continue;
            if (word.length() == 1 && !word.matches("[A-ZА-Я\\d]")) {
                continue;
            }
            wordsMap.put(word, wordsMap.containsKey(word) ? wordsMap.get(word)+1 : 1);
        }
        wordsMap.forEach((word, count) -> {
                try {
                    logger.debug("in database sent word={}, count={}", word, count);
                    database.addWord(word, count);
                } catch (DataBaseException ex) {
                    logger.error("cant add word={}", word, ex);
                    System.err.println("Word " + word + " not added");
                }
        });
        wordsMap.clear();
        database.commit();
    }

    public void printUniqueWords(Consumer<String> printer) {
        printer.accept("ny tipa rezultat hyli");
    }
}
