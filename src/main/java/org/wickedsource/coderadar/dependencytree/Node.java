package org.wickedsource.coderadar.dependencytree;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String filename;
    private String path;
    private String packageName;
    private List<Node> children;
    private List<Node> dependencies;
    private int layer;

    public Node(List<Node> children, String path, String filename, String packageName) {
        this.children = children;
        this.path = path;
        this.filename = filename;
        this.packageName = packageName;
        dependencies = new ArrayList<>();
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<Node> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Node> dependencies) {
        this.dependencies = dependencies;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }

    public Node getChildByName(String name) {
        if (hasChildren()) {
            for (Node child : children) {
                if (child.getFilename().equals(name)) {
                    return child;
                }
            }
        }
        return null;
    }

    public int countDependenciesOn(Node node) {
        int count = 0;
        for (Node dependency : dependencies) {
            if (dependency.packageName.contains(node.packageName)) {
                count++;
            }
        }
        return count;
    }

    public boolean hasDependencyOn(Node node) {
        for (Node dependency : dependencies) {
            if (dependency.packageName.contains(node.packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return packageName;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
}
