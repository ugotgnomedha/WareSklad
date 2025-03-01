package tech.parameters;

import java.util.List;

public class ParameterSection {
    private String section;
    private boolean modifiable;
    private List<Parameter> parameters;

    public ParameterSection(String section, boolean modifiable, List<Parameter> parameters) {
        this.section = section;
        this.modifiable = modifiable;
        this.parameters = parameters;
    }

    public String getSection() { return section; }
    public boolean isModifiable() { return modifiable; }
    public List<Parameter> getParameters() { return parameters; }

    @Override
    public String toString() {
        return "Section: " + section + "\n" + parameters;
    }
}