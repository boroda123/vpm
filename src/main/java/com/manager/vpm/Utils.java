package com.manager.vpm;

import org.apache.commons.compress.archivers.ArchiveException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Utils {

    static final String PACKAGE_INFO_URL = "https://registry.npmjs.org/";

    static final String DEPENDENCIES = "dependencies";
    static final String DISTRIBUTION = "dist";
    static final String TAR_BALL = "tarball";

    /*
     * A very simple package version conflict resolver,
     * needs more work.
     */
    public static String resolveVersionConflict(String v1, String v2) {
        v1 = formatVersion(v1);
        v2 = formatVersion(v2);

        if (v1.equals(v2)) {
            return v1;
        }

        return (v1.compareTo(v2) > 0) ? v1 : v2;
    }

    public static String formatVersion(String v) {
        if (v == null) {
            return "";
        }

        if (v.startsWith("^") || v.endsWith("~") || v.endsWith("=") || v.contains("<") || v.contains(">")) {
            v = v.substring(1);
        } else if (v.startsWith("<=") || v.startsWith(">=")) {
            v = v.substring(2);
        }

        return v;
    }

    /*
     * Get only dependencies here, skip devDependencies and peerDependencies
     */
    public static JSONObject getPackageInfo(String name, String version) throws IllegalArgumentException, IOException, ParseException {
        if (name == null || version == null) {
            throw new IllegalArgumentException("Both package name and version are required");
        }

        version = formatVersion(version);

        URL url = new URL(PACKAGE_INFO_URL + name + "/" + version);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        //Get the response code
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new IllegalArgumentException("Invalid response code (" + code + ") for URL: " + url.toString());
        }

        String inline = "";
        Scanner scanner = new Scanner(url.openStream());

        //Write all the JSON data into a string using a scanner
        while (scanner.hasNext()) {
            inline += scanner.nextLine();
        }

        //Close the scanner
        scanner.close();

        //Using the JSON simple library parse the string into a json object
        JSONParser parser = new JSONParser();
        JSONObject jo = (JSONObject) parser.parse(inline);

        JSONObject result = new JSONObject();

        JSONObject deps = (JSONObject) jo.get(DEPENDENCIES);
        result.put(DEPENDENCIES, deps);

        JSONObject dist = (JSONObject) jo.get(DISTRIBUTION);
        if (dist != null) {
            result.put(TAR_BALL, dist.get(TAR_BALL));
        }

        return result;
    }

    public static void downloadAndUntarPackage(String name, String version) throws IllegalAccessException, IOException, ParseException, ArchiveException {
        JSONObject info = getPackageInfo(name, version);

        String tarUrl = (String) info.get(TAR_BALL);

        savePackage(name, tarUrl);

    }

    private static void savePackage(String name, String tarUrl) throws IOException, ArchiveException {

        URL url = new URL(tarUrl);
        File file = new File(Vpm.NODE_MODULES_FOLDER_NAME + name + ".tar");
        if (file.exists()) {
            file.delete();
        }
        System.out.println("Saving package to file: " + url.toString() +" => "+ file.getAbsolutePath());

        FileUtils.copyURLToFile(url, file);
        File outDir = new File(Vpm.NODE_MODULES_FOLDER_NAME);

        Path destination = Paths.get(Vpm.NODE_MODULES_FOLDER_NAME + name);
        unpackPackage(file, destination);
    }

    private static void unpackPackage(File tarFile, Path destination) throws IOException, ArchiveException {

        System.out.println("Unpacking file file: " + tarFile.getAbsolutePath());

        if (destination.toFile().exists()) {
            destination.toFile().delete();
        }

        //InputStream is = new FileInputStream(tarFile);
        //new TarExtractorCommonsCompress(is, true, destination).untar();
    }

}
