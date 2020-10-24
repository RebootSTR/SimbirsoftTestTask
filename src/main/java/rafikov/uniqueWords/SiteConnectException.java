package rafikov.uniqueWords;

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

    public SiteConnectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SiteConnectException() {
    }
}
