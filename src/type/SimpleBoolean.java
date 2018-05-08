package type;

import exception.RuntimeInternalError;

public class SimpleBoolean extends SimpleObject {
    public static final SimpleBoolean False = new SimpleBoolean();
    public static final SimpleBoolean True = new SimpleBoolean();

    private SimpleBoolean() { }

    public static SimpleBoolean valueOf(SimpleObject object) {
        if (object instanceof SimpleBoolean) {
            return object == True ? True : False;
        }
        if (object instanceof SimpleFunction) {
            return True;
        }
        if (object instanceof SimpleList) {
            SimpleList list = (SimpleList) object;
            return list.iterator().hasNext() ? True : False;
        }
        if (object instanceof SimpleNumber) {
            SimpleNumber number = (SimpleNumber) object;
            return number.getValue().equals(0L) ? False : True;
        }
        throw new RuntimeInternalError("Unidentified Type of " + object.toString());
    }

    public static SimpleBoolean valueOf(Boolean value) {
        return value ? True : False;
    }

    public static Boolean toPrimitive(SimpleBoolean value) {
        return value == True;
    }

    public SimpleBoolean negate() {
        return this == True ? False : True;
    }

    @Override
    public String toString() {
        return String.valueOf(this == SimpleBoolean.True);
    }
}
