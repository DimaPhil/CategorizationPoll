package com.codefights.poll;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        File[] allFiles = folder.listFiles();
        if (allFiles == null) {
            System.out.println(String.format("Incorrect path: %s. It should be correct directory", path));
            System.exit(1);
        }
        Arrays.sort(allFiles, (file1, file2) -> file1.toString().compareTo(file2.toString()));
        ArrayList<File> filesList = new ArrayList<>();
        for (File file : allFiles) {
            if (file.isDirectory()) {
                String taskname = file.getName();
                if (!taskname.startsWith("_") && !taskname.startsWith(".")) {
                    filesList.add(file);
                }
            }
        }
        File[] files = filesList.toArray(new File[filesList.size()]);
        Visualizer visualizer = new Visualizer();
        Storage storage = new Storage();
        File lastFile = storage.getLastTask();
        int fileIndex = 0;
        for (int i = 0; i < files.length; i++) {
            if (lastFile != null && files[i].equals(lastFile)) {
                fileIndex = i;
            }
        }
        while (fileIndex < files.length) {
            File file = files[fileIndex];
            String taskname = file.getName();
            String[] selected = visualizer.loadTask(file, onlyGeneralSelection);
            if (selected == null) {
                continue;
            }
            if (selected.length == 1 && selected[0] == null) {
                fileIndex = Math.max(fileIndex - 1, 0);
                storage.setLastTask(files[fileIndex]);
                storage.dump();
                continue;
            } else {
                fileIndex++;
            }
            List<String> themes = Arrays.asList(selected);
            storage.addTask(taskname, themes);
            storage.setLastTask(files[fileIndex]);
            storage.dump();
        }
    }
}
