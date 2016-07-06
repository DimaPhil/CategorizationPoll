package com.codefights.poll;

import java.io.File;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String path = ".";
        boolean onlyGeneralSelection = true;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--path=")) {
                path = args[i].substring(7);
            }
            if (args[i].equals("-p")) {
                path = args[i + 1];
                i++;
            }
            if (args[i].equals("--enable-is") || args[i].equals("-is")) {
                onlyGeneralSelection = false;
            }
        }

        String currentDir = System.getProperty("user.dir");
        File folder = new File(currentDir, path);
        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println(String.format("Incorrect path: %s. It should be correct directory", path));
            System.exit(1);
        }
        Arrays.sort(files, (file1, file2) -> file1.toString().compareTo(file2.toString()));
        Visualizer visualizer = new Visualizer();
        for (File file : files) {
            if (file.isDirectory()) {
                String[] dirs = file.toString().split(File.separator);
                String lastDir = dirs[dirs.length - 1];
                if (lastDir.startsWith("_") || lastDir.startsWith(".")) {
                    continue;
                }
                visualizer.loadTask(file, onlyGeneralSelection);
            }
        }
    }
}
