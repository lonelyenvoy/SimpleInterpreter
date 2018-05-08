package type;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SimpleList extends SimpleObject implements Iterable<SimpleObject> {
    private final Iterable<SimpleObject> values;

    public SimpleList(Iterable<SimpleObject> values) {
        this.values = values;
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
