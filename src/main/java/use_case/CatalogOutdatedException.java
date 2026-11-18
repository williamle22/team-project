package use_case;

/**
 * A {@link CatalogLoadException} that signals a catalog file has a lower schema version than the app supports. To
 * recover, download a fresh copy.
 */
public class CatalogOutdatedException extends CatalogLoadException {
    public CatalogOutdatedException(String message) {
        super(message);
    }

    public CatalogOutdatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
