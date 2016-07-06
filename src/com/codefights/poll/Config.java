package com.codefights.poll;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by dmitry on 01.07.16.
 */
class Config {
    ArrayList<String> roots;
    ArrayList<ArrayList<String[]>> edges;

    private Config(ArrayList<ArrayList<String[]>> trees) {
        roots = new ArrayList<>();
        edges = new ArrayList<>();
        for (int i = 0; i < trees.size(); i++) {
            roots.add(trees.get(i).get(0)[0]);
            edges.add(new ArrayList<>());
            for (String[] edge : trees.get(i).subList(1, trees.get(i).size())) {
                edges.get(i).add(edge);
            }
        }
    }

    static Config loadPKT() throws ConfigException {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("config.tree")))) {
            ArrayList<String> lines = new ArrayList<>();
            lines.addAll(reader.lines().collect(Collectors.toList()));
            ArrayList<ArrayList<String[]>> trees = new ArrayList<>();
            ArrayList<String[]> tree = new ArrayList<>();
            for (String line : lines) {
                if (line.startsWith("Tree ")) {
                    if (tree.size() > 0) {
                        trees.add(tree);
                    }
                    tree = new ArrayList<>();
                    String treeName = line.substring(5);
                    tree.add(new String[]{treeName, null});
                } else if (line.length() > 0) {
                    String[] parts = line.split(" -> ");
                    if (parts.length != 2) {
                        throw new ConfigException("Expected line \"vertexName1 -- vertexName2\" in config file");
                    }
                    tree.add(new String[]{parts[0], parts[1]});
                }
            }
            if (tree.size() > 0) {
                trees.add(tree);
            }
            return new Config(trees);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
