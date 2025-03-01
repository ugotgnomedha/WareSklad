package tech.parameters;

import UndoRedo.UndoManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jme3.scene.Spatial;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParameterManager {
    private static ParameterManager instance;
    private final String filePath;
    private List<ParameterSection> sections;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ParameterManager(String filePath) {
        this.filePath = filePath;
        this.sections = ParameterParser.loadSections(filePath);
    }

    public static ParameterManager getInstance(String filePath) {
        if (instance == null) {
            instance = new ParameterManager(filePath);
        }
        return instance;
    }

    public List<ParameterSection> getSections() {
        return sections;
    }

    public List<Parameter> getParametersBySection(String sectionName) {
        return sections.stream()
                .filter(s -> s.getSection().equalsIgnoreCase(sectionName))
                .flatMap(s -> s.getParameters().stream())
                .collect(Collectors.toList());
    }

    public Optional<Parameter> findParameterByName(String name) {
        return sections.stream()
                .flatMap(s -> s.getParameters().stream())
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public boolean addParameter(String sectionName, Parameter newParam) {
        for (ParameterSection section : sections) {
            if (section.getSection().equalsIgnoreCase(sectionName) && section.isModifiable()) {
                section.getParameters().add(newParam);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean deleteParameter(String paramName) {
        for (ParameterSection section : sections) {
            if (section.isModifiable()) {
                List<Parameter> params = section.getParameters();
                Optional<Parameter> paramToRemove = params.stream()
                        .filter(p -> p.getName().equalsIgnoreCase(paramName))
                        .findFirst();

                if (paramToRemove.isPresent()) {
                    params.remove(paramToRemove.get());
                    saveToFile();
                    return true;
                }
            }
        }
        return false;
    }

    public void applyPreset(String presetName, Spatial object, UndoManager undoManager) {
        Optional<ParameterSection> presetSection = sections.stream()
                .filter(s -> s.getSection().equalsIgnoreCase(presetName))
                .findFirst();

        if (presetSection.isPresent()) {
            for (Parameter parameter : presetSection.get().getParameters()) {
                undoManager.addParameterToSpatial(object, parameter);
            }
        }
    }

    private void saveToFile() {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(sections, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}