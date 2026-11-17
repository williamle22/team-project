package use_case;

public class CatalogLoadException extends RuntimeException {
    public CatalogLoadException(String message) {
        super(message);
    }

    public CatalogLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
