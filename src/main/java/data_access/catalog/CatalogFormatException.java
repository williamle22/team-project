package data_access.catalog;

public class CatalogFormatException extends RuntimeException {
    CatalogFormatException(String message) {
        super(message);
    }

    CatalogFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
