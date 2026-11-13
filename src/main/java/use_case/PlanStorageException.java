package use_case;

/**
 * An unchecked exception that signals the app can't read or write a plan, for example because the disk is full or the
 * app lacks permissions.
 */
public class PlanStorageException extends RuntimeException {
    public PlanStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
