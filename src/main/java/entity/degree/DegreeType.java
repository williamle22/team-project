package entity.degree;

/** The three degrees a plan can target. */
public enum DegreeType {
    HBA("Honours Bachelor of Arts"), HBSC("Honours Bachelor of Science"), BCOM("Bachelor of Commerce");

    private final String title;

    DegreeType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
