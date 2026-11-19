package use_case.sync_catalog;

/**
 * A checked exception that signals the server couldn't provide the catalog, for example because the app is offline or
 * the server returned an error.
 */
public class CatalogUnavailableException extends Exception {
    public CatalogUnavailableException(String message) {
        super(message);
    }

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
