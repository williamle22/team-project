package data_access.catalog;

/**
 * An unchecked exception that signals a catalog JSON value doesn't match the dgx-backend schema.
 * {@link SqliteCatalogLoader} wraps it in a {@link use_case.CatalogLoadException} that names the row.
 */
public class CatalogFormatException extends RuntimeException {
    CatalogFormatException(String message) {
        super(message);
    }

    CatalogFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
