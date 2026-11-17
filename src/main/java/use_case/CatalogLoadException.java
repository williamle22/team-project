package use_case;

/**
 * An unchecked exception that signals the app can't read a catalog file, for example because the file is missing
 * or a row is malformed.
 */
public class CatalogLoadException extends RuntimeException {
    public CatalogLoadException(String message) {
        super(message);
    }

    public CatalogLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
