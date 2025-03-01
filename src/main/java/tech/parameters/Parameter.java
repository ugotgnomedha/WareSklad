package tech.parameters;

public class Parameter {
    private String name;
    private ParameterType type;
    private String unit;
    private Object defaultValue;

    public Parameter(String name, ParameterType type, String unit, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.defaultValue = defaultValue;
    }

    public String getName() { return name; }
    public ParameterType getType() { return type; }
    public String getUnit() { return unit; }
    public Object getDefaultValue() { return defaultValue; }

    @Override
    public String toString() {
        return name + " (" + unit + ") [" + type + "]: " + defaultValue;
    }
}