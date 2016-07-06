package com.codefights.poll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Created by dmitry on 01.07.16.
 */
class PKT {
    private int root;
    private ArrayList<Integer>[] tree;
    private int[] parent;
    private HashMap<String, Integer> map;
    private String[] vertexNames;

    @SuppressWarnings("unchecked")
    PKT(Config config, int treeIndex) {
        map = new HashMap<>();
        int counter = 0;
        for (String[] edge : config.edges.get(treeIndex)) {
            for (int i = 0; i < 2; i++) {
                if (!map.containsKey(edge[i])) {
                    map.put(edge[i], counter);
                    counter++;
                }
            }
        }
        String rootString = config.roots.get(treeIndex);
        if (!map.containsKey(rootString)) {
            throw new IllegalArgumentException(String.format("Wrong root for tree #%d: %s", treeIndex, rootString));
        }
        root = map.get(rootString);
        tree = new ArrayList[counter];
        for (int i = 0; i < counter; i++) {
            tree[i] = new ArrayList<>();
        }
        parent = new int[counter];
        vertexNames = new String[counter];
        Arrays.fill(parent, -1);
        for (String[] edge : config.edges.get(treeIndex)) {
            vertexNames[map.get(edge[0])] = edge[0];
            vertexNames[map.get(edge[1])] = edge[1];
            tree[map.get(edge[0])].add(map.get(edge[1]));
            if (parent[map.get(edge[1])] != -1) {
                throw new IllegalArgumentException(String.format("Given in config PKT %s is not a tree", vertexNames[root]));
            }
            parent[map.get(edge[1])] = map.get(edge[0]);
        }
    }

    PKT(int treeIndex) throws ConfigException {
        this(Config.loadPKT(), treeIndex);
    }

    private String lca(String vs, String us) {
        int v = map.get(vs);
        int u = map.get(us);
        boolean[] used = new boolean[tree.length];
        while (v != root) {
            used[v] = true;
            v = parent[v];
        }
        used[root] = true;
        while (u != root) {
            if (used[u]) {
                return vertexNames[u];
            }
            u = parent[u];
        }
        return null;
    }

    private String checkGeneralSelection(String[] selected) {
        if (selected.length == 0) {
            return "Please, select one vertex";
        }
        if (selected.length > 1) {
            return String.format("One vertex should be selected for general selection, but %d found", selected.length);
        }
        if (!map.containsKey(selected[0])) {
            return String.format("There is no such vertex in %s PKT: %s", vertexNames[root], selected[0]);
        }
        return null;
    }

    private String checkImplementationSpecificSelection(String[] selected) {
        if (selected.length == 0) {
            return "Please, select at least one vertex";
        }
        if (selected.length > 3) {
            return "Please, select at most 3 vertices. " +
                    "If the task corresponds to more than 3 different categories, " +
                    "then instead of them you should take their LCA as the only category for this set";
        }
        for (String element : selected) {
            if (!map.containsKey(element)) {
                return String.format("There is no such vertex in %s PKT: %s", vertexNames[root], element);
            }
        }
        for (int i = 0; i < selected.length; i++) {
            for (int j = i + 1; j < selected.length; j++) {
                String lcaVertex = lca(selected[i], selected[j]);
                if (lcaVertex == null) {
                    throw new AssertionError(String.format("LCA works wrong: null was returned for vertices %s and %s", selected[i], selected[j]));
                }
                if (lcaVertex.equals(selected[i]) || lcaVertex.equals(selected[j])) {
                    return String.format("Vertices %s and %s lie on one path, please fix it", selected[i], selected[j]);
                }
            }
        }
        return null;
    }

    String checkSelected(String[] selected, String type) {
        switch (type) {
            case "general":
                return checkGeneralSelection(selected);
            case "implementation-specific":
                return checkImplementationSpecificSelection(selected);
            default:
                throw new IllegalArgumentException("Unsupported selection type: " + type);
        }
    }

    String getRoot() {
        return vertexNames[root];
    }

    ArrayList<String> getChildren(String vertex) {
        if (!map.containsKey(vertex)) {
            return null;
        }
        return tree[map.get(vertex)].stream().map(child -> vertexNames[child]).collect(Collectors.toCollection(ArrayList::new));
    }
}
