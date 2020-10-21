public class SiteConnectException extends Exception {
    public SiteConnectException(String url) {
        super("Fail connection to " + url);
    }
}
