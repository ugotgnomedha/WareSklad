package tech.parameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ParameterParser {
    public static List<ParameterSection> loadSections(String filePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type sectionListType = new TypeToken<List<ParameterSection>>() {}.getType();
            return gson.fromJson(reader, sectionListType);
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}