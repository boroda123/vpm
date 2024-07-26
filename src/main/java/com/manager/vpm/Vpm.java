package com.manager.vpm;

public class Vpm {
    static final String PACKAGE_FILE_NAME = "./package.json";
    static final String NODE_MODULES_FOLDER_NAME = "node_modules/";

    private static final String HELP =
            "\nUsage: \n" +
                    "     java -jar vpm.jar add <package_name>@<package_version>\n" +
                    "     java -jar vpm.jar add react-dom@^18.3.1\n" +
                    "     java -jar vpm.jar install\n";

    private static enum COMMANDS {
        add, install;
    }

    public static void main(String[] args) {

        if (args == null || args.length == 0 || "-h".equals(args[0]) || "--help".equals(args[0])){
            System.out.println(HELP);
            return;
        }

        try {
            if (COMMANDS.add.toString().equals(args[0])) {
                if (args.length < 2) {
                    throw new IllegalAccessException("Package name is missing");
                }
                AddToPackage atp = new AddToPackage(args[1]);
                atp.verifyInput();
                atp.addToPackage();
            } else if (COMMANDS.install.toString().equals(args[0])) {
                DownloadPackages dp = new DownloadPackages();
                dp.verifyPackages();
                dp.downloadPackages();
            } else {
                System.out.println(HELP);
            }

        } catch (Exception e) {
            System.out.println("\n" + e.getMessage());
            System.out.println(HELP);
            e.printStackTrace();
        }

    }

}
