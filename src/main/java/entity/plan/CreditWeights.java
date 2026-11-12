package entity.plan;

/**
 * Supplies a course code's credit weight: {@code 0.5} for a half course or {@code 1.0} for a full course.
 * {@code entity.plan} can't import {@code use_case.CatalogReader}, so it declares this interface, and the use case
 * passes in one backed by the catalog.
 */
public interface CreditWeights {
    double weightOf(String code);

    /**
     * Reads the weight from the code. A {@code Y} as the 7th character means {@code 1.0}, such as {@code PHL100Y1}.
     * Anything else means {@code 0.5}, such as {@code CSC148H1}.
     */
    CreditWeights FROM_CODE = code -> code.length() >= 7 && code.charAt(6) == 'Y' ? 1.0 : 0.5;
}
