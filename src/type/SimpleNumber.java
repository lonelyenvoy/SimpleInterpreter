package type;

public class SimpleNumber extends SimpleObject {
    private final Long value;
    public Long getValue() {
        return value;
    }

    public SimpleNumber(Long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof SimpleNumber && this.getValue().equals(((SimpleNumber) object).getValue());
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
