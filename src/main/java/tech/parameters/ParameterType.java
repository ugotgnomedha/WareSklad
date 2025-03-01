package tech.parameters;

public enum ParameterType {
    BOOLEAN("boolean"),
    STRING("string"),
    NUMBER("number");

    private final String type;

    ParameterType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}