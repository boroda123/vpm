package com.manager.vpm;

import org.apache.commons.compress.archivers.ArchiveException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DownloadPackages {

    private File packageFile;
    JSONObject deps;
    Map<String, String> depsMap = new HashMap<>();
    Map<String, String> depsDepsMap = new HashMap<>();

    DownloadPackages() {}

    public void verifyPackages() throws IllegalAccessException, IOException, ParseException {
        packageFile = new File(Vpm.PACKAGE_FILE_NAME);

        if (packageFile.exists()) {
            JSONParser parser = new JSONParser();
            JSONObject jo = (JSONObject) parser.parse(new FileReader(packageFile));

            deps = (JSONObject) jo.get(Utils.DEPENDENCIES);
            if (deps == null || deps.isEmpty()) {
                String filePath = packageFile.getAbsoluteFile().getPath();
                throw new IllegalAccessException("No dependencies found in file: \"" + filePath + "\"");
            }

        } else {
            String dir = packageFile.getAbsoluteFile().getParent();
            if (dir.endsWith(".")) {
                dir = dir.substring(0, dir.length() - 1);
            }
            throw new IllegalAccessException("File \"" + Vpm.PACKAGE_FILE_NAME + "\" does not exist in \"" + dir + "\" directory");
        }
    }

    public void downloadPackages() throws IllegalAccessException, IOException, ParseException, ArchiveException {
        resolveDependencies();

        // Merge all dependencies into one Map
        if (!depsDepsMap.isEmpty()) {
            Set<String> keys = depsDepsMap.keySet();
            for (String name : keys) {
                String v1 = depsDepsMap.get(name);
                if (!depsMap.containsKey(name)) {
                    depsMap.put(name, v1);
                    continue;
                }
                String v2 = depsMap.remove(name);

                v1 = Utils.resolveVersionConflict(v1, v2);
                depsMap.put(name, v1);
            }
        }

        // Download and un-tar packages one by one
        Set<String> keys = depsMap.keySet();
        for (String name : keys) {
            String v1 = depsMap.get(name);
            downloadOnePackage(name, v1);
        }
    }

    private void resolveDependencies() throws IllegalAccessException, IOException, ParseException {
        if (deps == null) {
            verifyPackages();
        }

        Set<String> keys = deps.keySet();
        for (String name : keys) {
            String version = (String) deps.get(name);
            System.out.println("Deps: " + name +" :: " + version);

            version = Utils.formatVersion(version);
            depsMap.put(name, version);
            addPackageDeps(name, version);
        }

    }

    /*
     * Add and try to "Map-Reduce" sub-dependencies in Utils.resolveVersionConflict
     * Needs much more works here to handle all npm version notations:
     * ~, ^, *, <, <=, >, >=, =, -, ||, x.x.x, latest, etc.
     *
     * For now assume a simple (and not correct) rule - always pick the highest version.
     *
     * TODO: Replace this algorithm with something more meaningful.
     */
    private void addPackageDeps(String name, String version) throws IllegalAccessException, IOException, ParseException {
        JSONObject info = Utils.getPackageInfo(name, version);

        JSONObject deps = (JSONObject) info.get(Utils.DEPENDENCIES);
        if (deps == null || deps.isEmpty()) {
            return;
        }

        Set<String> keys = deps.keySet();
        for (String pkgName : keys) {
            String pkgVersion = (String) deps.get(pkgName);
            System.out.println("    Sub-Deps: " + pkgName +" :: " + pkgVersion);

            pkgVersion = Utils.formatVersion(pkgVersion);
            if (depsDepsMap.containsKey(pkgName)) {
                String v1 = depsDepsMap.get(pkgName);
                v1 = Utils.resolveVersionConflict(v1, pkgVersion);
                depsDepsMap.put(pkgName, v1);
            } else {
                depsDepsMap.put(pkgName, pkgVersion);
            }

        }
    }


    private void downloadOnePackage(String name, String version) throws IllegalAccessException, IOException, ParseException, ArchiveException {

        Utils.downloadAndUntarPackage(name, version);

    }
 }