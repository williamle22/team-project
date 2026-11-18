package entity.requirement;

import java.util.Objects;

/**
 * A {@code profile} node: a fact about the student, such as year of study, faculty, or admission category, named by
 * {@code attr} and compared with {@code value}.
 */
public final class ProfileReq extends Requirement {
    private final String attr;
    private final Object value;
    private final String text;

    /** @throws IllegalArgumentException if {@code value} isn't a {@code String}, {@code Integer}, or {@code Boolean} */
    public ProfileReq(String attr, Object value, String text, boolean concurrent) {
        super(concurrent);
        this.attr = Objects.requireNonNull(attr, "attr");
        if (!(value instanceof String || value instanceof Integer || value instanceof Boolean)) {
            throw new IllegalArgumentException(attr + " value must be a String, Integer or Boolean, was " + value);
        }
        this.value = value;
        this.text = Objects.requireNonNull(text, "text");
    }

    public String getAttr() {
        return attr;
    }

    public Object getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    /** Returns how to compare the fact with {@code value}, derived from the attribute name. */
    public ProfileComparison getComparison() {
        return ProfileComparison.forAttribute(attr);
    }

    /** @throws IllegalStateException if {@code value} isn't an {@code Integer} */
    public int getIntValue() {
        if (!(value instanceof Integer)) {
            throw new IllegalStateException(attr + " is not an integer attribute");
        }
        return (Integer) value;
    }

    /** @throws IllegalStateException if {@code value} isn't a {@code Boolean} */
    public boolean getBooleanValue() {
        if (!(value instanceof Boolean)) {
            throw new IllegalStateException(attr + " is not a boolean attribute");
        }
        return (Boolean) value;
    }

    /** @throws IllegalStateException if {@code value} isn't a {@code String} */
    public String getStringValue() {
        if (!(value instanceof String)) {
            throw new IllegalStateException(attr + " is not a string attribute");
        }
        return (String) value;
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitProfile(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ProfileReq that = (ProfileReq) o;
        return attr.equals(that.attr) && value.equals(that.value) && text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attr, value, text);
    }
}
