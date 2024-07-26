package com.manager.vpm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AddToPackage {

    private String addPackage;
    private String packageName;
    private String packageVersion;
    private File packageFile;

    AddToPackage(String addPackage) {
        this.addPackage = addPackage;
    }

    public void verifyInput() throws IllegalAccessException {
        packageFile = new File(Vpm.PACKAGE_FILE_NAME);

        if (packageFile.exists()) {
            if (addPackage == null || addPackage.trim().isEmpty()) {
                throw new IllegalAccessException("Package name is empty");
            }
            if (!addPackage.contains("@")) {
                throw new IllegalAccessException("Package name format must be: <package_name>@<package_version>");
            }
            String[] tmp = addPackage.trim().split("@");
            packageName = tmp[0];
            packageVersion = tmp[1];
        } else {
            String dir = packageFile.getAbsoluteFile().getParent();
            if (dir.endsWith(".")) {
                dir = dir.substring(0, dir.length() - 1);
            }
            throw new IllegalAccessException("File \"" + Vpm.PACKAGE_FILE_NAME + "\" does not exist in \"" + dir + "\" directory.\n" +
                    "Please create it first: manually, or by running \"npm inint\"");
        }
    }

    public void addToPackage() throws IllegalAccessException, IOException, ParseException {
        if (packageFile == null) {
            verifyInput();
        }

        JSONParser parser = new JSONParser();
        JSONObject jo = (JSONObject) parser.parse(new FileReader(packageFile));

        // Get the dependencies section from the file, or create it if not exists
        JSONObject deps = (JSONObject) jo.get(Utils.DEPENDENCIES);
        if (deps == null) {
            deps = new JSONObject();
            jo.put(Utils.DEPENDENCIES, deps);
        }

        if (deps.containsKey(packageName)) {
            deps.remove(packageName);
        }
        deps.put(packageName, packageVersion);

        packageFile.delete();

        packageFile.createNewFile();
        FileWriter writer = new FileWriter(packageFile);
        String jsonString = formatJSONString(jo);

        writer.write(jsonString);
        writer.flush();
    }

    private String formatJSONString(JSONObject jo) {
        String jsonString = jo.toJSONString();
        //jsonString = jsonString.replaceAll("\\{", "{\n");
        //jsonString = jsonString.replaceAll("}", "\n}");
        //jsonString = jsonString.replaceAll("\\[", "[\n");
        //jsonString = jsonString.replaceAll("]", "\n]");
        //jsonString = jsonString.replaceAll(",", ",\n");
        //jsonString = jsonString.replaceAll(/\//g,'');

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(jsonString);
        String prettyJsonString = gson.toJson(je);
        return prettyJsonString;
    }

}