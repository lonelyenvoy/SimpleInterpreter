package type;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class SimpleList extends SimpleObject implements Iterable<SimpleObject> {
    private final Iterable<SimpleObject> values;

    private SimpleList(Iterable<SimpleObject> values) {
        this.values = values;
    }

    public static SimpleList of(Iterable<SimpleObject> values) {
        return new SimpleList(values);
    }

    @Override
    public String toString() {
        return "(list " + String.join(" ", convertValuesToStrings()) + ")";
    }

    private Iterable<String> convertValuesToStrings() {
        return StreamSupport.stream(values.spliterator(), false).map(SimpleObject::toString).collect(Collectors.toList());
    }

    @Override
    public Iterator<SimpleObject> iterator() {
        return values.iterator();
    }
}
