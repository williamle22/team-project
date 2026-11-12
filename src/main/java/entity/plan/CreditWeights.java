package entity.plan;

public interface CreditWeights {
    double weightOf(String code);

    CreditWeights FROM_CODE = code -> code.length() >= 7 && code.charAt(6) == 'Y' ? 1.0 : 0.5;
}
