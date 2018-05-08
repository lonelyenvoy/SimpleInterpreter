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

}
