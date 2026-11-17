package use_case;

public class CatalogOutdatedException extends CatalogLoadException {
    public CatalogOutdatedException(String message) {
        super(message);
    }

    public CatalogOutdatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
