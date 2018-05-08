package type;

public class SimpleBoolean extends SimpleObject {
    public static final SimpleBoolean False = new SimpleBoolean();
    public static final SimpleBoolean True = new SimpleBoolean();

    public static SimpleBoolean valueOf(Boolean value) {
        return value ? True : False;
    }

    public static Boolean toPrimitive(SimpleBoolean value) {
        return value == True;
    }

    @Override
    public String toString() {
        return String.valueOf(this == SimpleBoolean.True);
    }
}
