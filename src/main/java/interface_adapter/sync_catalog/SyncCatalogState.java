package interface_adapter.sync_catalog;

import java.util.Objects;

/** What the catalog status bar shows. */
public final class SyncCatalogState {
    /** How serious the message is. The status bar sets the message's color from it. */
    public enum Severity { INFO, WARNING, ERROR }

    private boolean loading;
    private String message = "";
    private Severity severity = Severity.INFO;
    private boolean retryAvailable;

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public boolean isRetryAvailable() {
        return retryAvailable;
    }

    public void setRetryAvailable(boolean retryAvailable) {
        this.retryAvailable = retryAvailable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SyncCatalogState)) {
            return false;
        }
        SyncCatalogState that = (SyncCatalogState) o;
        return loading == that.loading && Objects.equals(message, that.message) && severity == that.severity
                && retryAvailable == that.retryAvailable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(loading, message, severity, retryAvailable);
    }

    @Override
    public String toString() {
        return "SyncCatalogState{" + severity + ", " + message + (loading ? ", loading" : "")
                + (retryAvailable ? ", retry" : "") + "}";
    }
}
