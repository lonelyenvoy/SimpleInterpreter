package type;

import infrastructure.SimpleExpression;
import infrastructure.SimpleScope;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleFunction extends SimpleObject {
    private SimpleExpression body;
    private String[] parameters;
    private SimpleScope scope;

    public SimpleExpression getBody() {
        return body;
    }
    public String[] getParameters() {
        return parameters;
    }
    public SimpleScope getScope() {
        return scope;
    }

    public SimpleFunction(SimpleExpression body, String[] parameters, SimpleScope scope) {
        this.body = body;
        this.parameters = parameters;
        this.scope = scope;
    }

    public SimpleObject evaluate() {
        if (computeFilledParameters().size() < parameters.length) {
            return this;
        } else {
            return body.evaluate(this.scope);
        }
    }

    public SimpleFunction update(SimpleObject[] arguments) {
        Stream<SimpleObject> existingArguments =
                Arrays.stream(parameters)
                        .map(param -> scope.findLazily(param))
                        .filter(Objects::nonNull);
        List<SimpleObject> newArguments =
                Stream.concat(existingArguments, Arrays.stream(arguments)).collect(Collectors.toList());
        SimpleScope newScope = scope.getParent().spawnScopeWithVariables(parameters, newArguments.toArray(new SimpleObject[0]));
        return new SimpleFunction(body, parameters, newScope);
    }

    private List<String> computeFilledParameters() {
        return Arrays.stream(parameters).filter(param -> scope.findLazily(param) != null).collect(Collectors.toList());
    }

    public Boolean isPartial() {
        int filledParameterSize = computeFilledParameters().size();
        return filledParameterSize >= 1 && filledParameterSize < parameters.length;
    }

    @Override
    public String toString() {
        return String.format(
                "(function (%s) (%s))",
                String.join(
                        " ",
                        Arrays.stream(parameters).map(parameter -> {
                            SimpleObject value = null;
                            if ((value = scope.findLazily(parameter)) != null) {
                                return parameter + ":" + value;
                            }
                            return parameter;
                        }).collect(Collectors.toList())
                ), body);
    }
}
