package com.codefights.poll;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Created by dmitry on 09.07.16.
 */
public class Storage {
    private TreeMap<String, List<String>> tasksThemes;
    private File lastTask;
    private final File log = new File("choices.log");

    Storage() {
        restore();
    }

    private void restore() {
        lastTask = null;
        tasksThemes = new TreeMap<>();
        if (!log.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(log))) {
            ArrayList<String> lines = reader.lines().collect(Collectors.toCollection(ArrayList::new));
            if (lines.size() > 0) {
                lastTask = new File(lines.get(0));
                for (String line : lines.subList(1, lines.size())) {
                    String[] themes = line.split(" ");
                    tasksThemes.put(themes[0], new ArrayList<>());
                    for (int i = 1; i < themes.length; i++) {
                        tasksThemes.get(themes[0]).add(themes[i]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addTask(String task, List<String> themes) {
        tasksThemes.put(task, themes);
    }

    void setLastTask(File task) {
        lastTask = task;
    }

    boolean haveThemes(String task) {
        return tasksThemes.containsKey(task);
    }

    List<String> getThemes(String task) {
        return tasksThemes.get(task);
    }

    File getLastTask() {
        return lastTask;
    }

    void dump() {
        try (PrintWriter writer = new PrintWriter(log)) {
            writer.write(lastTask + "\n");
            for (String task : tasksThemes.keySet()) {
                writer.write(task + " " + String.join("\n", tasksThemes.get(task)) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
