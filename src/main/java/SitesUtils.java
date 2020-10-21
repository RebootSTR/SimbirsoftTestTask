import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SitesUtils {
    private Connection connection;
    private String url;

    public SitesUtils(String url) {
        this.url = url;
        connection = Jsoup.connect(url);
    }

    public SitesUtils setURL(String url) {
        this.url = url;
        connection = connection.url(url);
        return this;
    }

    public SitesUtils setUserAgent(String userAgent) {
        connection = connection.userAgent(userAgent);
        return this;
    }

    public SitesUtils setCookie(String key, String value) {
        connection = connection.cookie(key, value);
        return this;
    }

    public String getAllWordsFromSelector(String selector) throws SiteConnectException {
        Document doc = connect(connection);
        Elements elements = doc.select(selector);
        StringBuilder result = new StringBuilder();
        for (Element element : elements ) {
            result.append(element.text());
        }
        return result.toString();
    }

    public String getAllWords() throws SiteConnectException {
        return getAllWordsFromSelector("body");
    }

    private Document connect(Connection connection) throws SiteConnectException {
        try {
            return connection.get();
        } catch (java.io.IOException ex) {
            throw new SiteConnectException(this.url);
        }
    }
}
