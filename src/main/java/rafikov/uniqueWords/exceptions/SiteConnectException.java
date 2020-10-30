package rafikov.uniqueWords.exceptions;

public class SiteConnectException extends Exception {
    public SiteConnectException(String url) {
        super("Fail connection to " + url);
    }

    public SiteConnectException(String message, Throwable cause) {
        super(message, cause);
    }

    public SiteConnectException(Throwable cause) {
        super(cause);
    }

    public SiteConnectException() {
    }
}
