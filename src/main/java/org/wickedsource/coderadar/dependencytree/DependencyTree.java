package org.wickedsource.coderadar.dependencytree;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyTree {

    private String basepackage;
    private String basepackage_dot;
    private Node baseroot;

    public DependencyTree(String basepackage, String basepackage_dot, Node baseroot) {
        this.basepackage = basepackage;
        this.basepackage_dot = basepackage_dot;
        this.baseroot = baseroot;
    }

    public Node setDependencies(Node root) {
        for (Node child : root.getChildren()) {
            if (child.hasChildren()) {
                setDependencies(child);
            } else {
                for (String dependency : getDependenciesFromFile(child)) {
                    String dependencyString = dependency.replace(".", "/") + ".java";
                    dependencyString = dependencyString.substring(dependencyString.lastIndexOf(basepackage) + basepackage.length() + 1);
                    String[] pathParts = dependencyString.split("/");
                    Node currentNode = baseroot;
                    for (String pathPart : pathParts) {
                        if (currentNode != null) {
                            if (currentNode.hasChildren()) {
                                currentNode = currentNode.getChildByName(pathPart);
                            }
                        }
                    }
                    //TODO for testing
                    if (currentNode != null) {
                        child.getDependencies().add(currentNode);
                    }
                }
            }

        }
        return root;
    }

    public void exportJSON(Node root, String path) throws IOException {
        Files.write(Paths.get(path), new Gson().toJson(root).getBytes());
    }

    public List<String> getDependenciesFromFile(Node node) {
        try {
            if (!node.hasChildren()) {
                String content = new String(Files.readAllBytes(Paths.get(node.getPath())));
                Matcher importMatcher = Pattern.compile("import [a-zA-Z.]*[a-zA-Z];").matcher(content);
                List<String> imports = new ArrayList<>();
                while (importMatcher.find()) {
                    String dependency = importMatcher.group();
                    if (dependency.contains(basepackage_dot)) {
                        Matcher dependencyMatcher = Pattern.compile(" ([a-zA-Z]+.)*[a-zA-Z]").matcher(dependency);
                        dependencyMatcher.find();
                        imports.add(dependencyMatcher.group().substring(1));
                    }
                }
                return imports;
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public Node createTree(Node root) {
        File rootFile = new File(root.getPath());
        File[] files = Objects.requireNonNull(rootFile.listFiles());
        Arrays.sort(files, (f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            } else if (!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            } else {
                return f1.compareTo(f2);
            }
        });
        for (File file : files) {
            if (file.isDirectory()) {
                Node node = new Node(new ArrayList<>(), file.getPath(), file.getName(), root.getPackageName() + "." + file.getName());
                root.getChildren().add(node);
                createTree(node);
            } else {
                root.getChildren().add(new Node(new ArrayList<>(), file.getPath(), file.getName(), root.getPackageName() + "." + file.getName()));
            }
        }
        return root;
    }

    public String printDirectoryTree(Node root) {
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printDirectoryTree(root, indent, sb);
        return sb.toString();
    }

    private void printDirectoryTree(Node root, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(root.getFilename());
        sb.append("/    ");
        sb.append(root.getDependencies());
        sb.append("\n");
        for (Node child : root.getChildren()) {
            if (child.hasChildren()) {
                printDirectoryTree(child, indent + 1, sb);
            } else {
                printFile(child, indent + 1, sb);
            }
        }

    }

    private void printFile(Node node, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(node.getFilename() + ";    " + node.getDependencies());
        sb.append("\n");
    }

    private String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }


//    _______________________________________________________________________________________________________







    public String printDependencyTree(Node root) {
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printDependencyTree(root, indent, sb);
        return sb.toString();
    }

    private void printDependencyTree(Node root, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        //TODO condition under which dependencies shall be displayed next to each other

        sb.append(root.getFilename());

        if (!root.hasDependencies() && root.hasChildren()) {
            sb.append("/");
            sb.append("\n");
            for (Node child : root.getChildren()) {
                if (child.hasChildren()) {
                    printDependencyTree(child, indent + 1, sb);
                } else {
                    printDependencyTree(child, indent + 1, sb);
                }
            }
        } else {
            sb.append(" -> ");
            sb.append(root.getDependencies());
            sb.append("\n");
            for (Node dependency : root.getDependencies()) {
                if (dependency.hasDependencies()) {
                    printDependencyTree(dependency, indent + 1, sb);
                } else {
                    printDependency(dependency, indent + 1, sb);
                }
            }
        }
    }

    private void printDependency(Node node, int indent, StringBuilder sb) {
        sb.append(getDependencyIndentString(indent));
        sb.append("+--");
        sb.append(node.getPackageName());
        sb.append("\n");
    }

    private String getDependencyIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }
}
