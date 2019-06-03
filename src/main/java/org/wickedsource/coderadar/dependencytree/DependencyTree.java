package org.wickedsource.coderadar.dependencytree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DependencyTree {

    private String basepackage;
    private String basepackage_dot;
    private Node baseroot;
    private RegexPatternCache cache;

    public DependencyTree(String basepackage, String basepackage_dot, Node baseroot) {
        this.basepackage = basepackage;
        this.basepackage_dot = basepackage_dot;
        this.baseroot = baseroot;
        cache = new RegexPatternCache();
    }

    /**
     * set all dependencies for a given Node object including fully qualified class name usages, imports and wildcard imports
     *
     * @param root Node object to set dependencies for
     * @return Node which has its dependencies set
     */
    public void setDependencies(Node root) {
        for (Node child : root.getChildren()) {
            if (child.hasChildren()) {
                setDependencies(child);
            } else {
                // get all dependencies from imports including wildcard imports
                for (String dependency : getDependenciesFromFile(child)) {
                    String dependencyString = dependency.replace(".", "/");
                    // if import is not a wildcard import add .java to it to find the file
                    if (!dependency.matches("[a-zA-Z.]*\\*")) {
                        dependencyString += ".java";
                    }
                    // remove the basepackage name from dependency to find file(s) in same package
                    dependencyString = dependencyString.substring(dependencyString.lastIndexOf(basepackage) + basepackage.length() + 1);
                    String[] pathParts = dependencyString.split("/");
                    // iterate through all children til the package and filename matches the dependencyString

//                    System.out.println("Find dependencies for: " + child.getPackageName() + "; dependencyString: " + dependencyString);
                    List<Node> foundDependencies = findPackageNameInModules(pathParts, baseroot);
                    foundDependencies.stream().filter(
                            wildcardDependency -> !child.getDependencies().contains(wildcardDependency)
                    )
                            .forEach(wildcardDependency -> child.getDependencies().add(wildcardDependency));
                }
                // get all dependencies for fully qualified class usage, ignoring:
                //   import declarations
                //   package declarations
                //   single line comments
                //   multi line comments
                //   strings
                for (String qualifiedDependency : getClassQualifierDependencies(child)) {
                    // add .java to find the file
                    String dependencyString = qualifiedDependency.replace(".", "/") + ".java";
                    // remove the basepackage name from dependency to find file(s) in same package
                    dependencyString = dependencyString.substring(dependencyString.lastIndexOf(basepackage) + basepackage.length() + 1);
                    String[] pathParts = dependencyString.split("/");
                    List<Node> foundDependencies = findPackageNameInModules(pathParts, baseroot);
                    foundDependencies.stream().filter(
                            wildcardDependency -> !child.getDependencies().contains(wildcardDependency)
                    )
                            .forEach(wildcardDependency -> child.getDependencies().add(wildcardDependency));
                }
            }
            // add all file dependencies to the current package; done for structuring purposes
            root.getDependencies().addAll(child.getDependencies());
        }
    }

    /**
     * analyze the file of a given Node object for fully qualified class name usage dependencies. helper method for setDependencies(Node node)
     *
     * @param node Node to analyze
     * @return List of package and file names the current node has dependencies on
     */
    private List<String> getClassQualifierDependencies(Node node) {
        try {
            if (!node.hasChildren() && node.getFilename().endsWith(".java")) {
                List<String> imports = new ArrayList<>();
                String[] lines = clearFileContent(new String(Files.readAllBytes(Paths.get(node.getPath())))).split("\n");
                for (String content : lines) {
                    // dependency found if string.string.... pattern is matched and
                    //   if pattern is not in a single or multi line comment
                    //   if pattern is not in a string
                    //   if pattern is not in an import or the package name
                    Matcher importMatcher = cache.getPattern("^(?!(.*\\simport|.*\\spackage|\\s*//|\\s*/\\*|\\s*\\*|.*\")).*(([A-Za-z_$][A-Za-z_$0-9]*)\\.)+([A-Za-z_$][A-Za-z_$0-9]*)(?!\\($)").matcher(content);
                    if (importMatcher.lookingAt()) {
                        String dependency = importMatcher.group();
                        // if it is an import from the current project
                        if (dependency.contains(basepackage_dot)) {
                            // extract packageName.fileName from matched region
                            Matcher dependencyMatcher = Pattern.compile("([a-zA-Z]+\\.)+[a-zA-Z]+").matcher(dependency);
                            if (dependencyMatcher.find()) {
                                String foundDependency = dependencyMatcher.group();
                                if (!imports.contains(foundDependency)) {
                                    imports.add(foundDependency);
                                }
                            }
                        }
                    }
                }
                return imports;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * analyze the file of a given Node object for import and wildcard import dependencies. helper method for setDependencies(Node node)
     *
     * @param node Node to analyze
     * @return List of package and file names the current node has dependencies on
     */
    private List<String> getDependenciesFromFile(Node node) {
        try {
            if (!node.hasChildren() && node.getFilename().endsWith(".java")) {
                List<String> imports = new ArrayList<>();
                String[] lines = clearFileContent(new String(Files.readAllBytes(Paths.get(node.getPath())))).split("\n");
                for (String content : lines) {
                    // found the end of the area where import statements are valid
                    //   if the line does not begin with a single or multi line comment
                    //   if the line does not begin with an import statement
                    //   if the line is not empty or not only contains whitespaces
                    //   if the line is not the package declaration
                    Matcher classMatcher = cache.getPattern("^(?!(\\s*import|\\s*$|\\s*//|\\s*/\\*|\\s*\\*|\\s*package))").matcher(content);
                    if (classMatcher.find()) {
                        break;
                    }
                    // find all regions with an import statement
                    Matcher importMatcher = cache.getPattern("^(?!(\\s*//|\\s*/\\*|\\s*\\*|.*\"))import (([A-Za-z_$][A-Za-z_$0-9]*)\\.)+(([A-Za-z_$][A-Za-z_$0-9]*)|\\*);").matcher(content);
                    while (importMatcher.find()) {
                        String dependency = importMatcher.group();
                        // if it is an import from the current project
                        if (dependency.contains(basepackage_dot)) {
                            // extract packageName.fileName from matched region
                            Matcher dependencyMatcher = cache.getPattern(" ([a-zA-Z]+.)*([a-zA-Z]|\\*)").matcher(dependency);
                            if (dependencyMatcher.find()) {
                                String foundDependency = dependencyMatcher.group().substring(1);
                                if (!imports.contains(foundDependency)) {
                                    imports.add(foundDependency);
                                }
                            }
                        }
                    }
                }
                return imports;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * remove comments from a given fileContent
     * @param fileContent fieContent to clear
     * @return cleared fileContent
     */
    private String clearFileContent(final String fileContent) {
        return fileContent.replaceAll("(\\/\\*(.|[\\r\\n])+?\\*\\/)|(\\/\\/.*[\\r\\n])", "");
    }

    private List<Node> findPackageNameInModules(String[] packageName, Node root) {
        // List nodes
        // if root is a module
        //   go through every child of root
        //     if the child is a package
        //       if first part of packageName fits child.filename
        //         add goThroughPackageTree(packageName, child) to nodes
        //       else skip
        //     else if the child is a module
        //       add recursive to nodes
        //   return nodes
        // else if root is package
        //   add goThroughPackageTree(packageName, root) to nodes
        // return nodes

        List<Node> nodes = new LinkedList<>();
        if (root.getPackageName().equals("")) {
            for (Node child : root.getChildren()) {
                if (!child.getPackageName().equals("")) {
                    if (child.getFilename().equals(packageName[0])) {
                        nodes.addAll(gotThroughPackageTree(packageName, child));
                    }
                } else {
                    nodes.addAll(findPackageNameInModules(packageName, child));
                }
            }
        } else {
            nodes.addAll(gotThroughPackageTree(packageName, root));
        }
        return nodes;
    }

    private List<Node> gotThroughPackageTree(String[] packageName, Node root) {
        Node currentNode = root;
        for (int i = 1; i < packageName.length; i++) {
            if (currentNode != null) {
                if (currentNode.hasChildren()) {
                    // if dependencyString contains a wildcard add all children as dependencies and stop here
                    if (packageName[i].equals("*")) {
                        return currentNode.getChildren();
                    } else {
                        if (currentNode.getChildByName(packageName[i]) != null) {
                            currentNode = currentNode.getChildByName(packageName[i]);
                        } else {
                            return Collections.EMPTY_LIST;
                        }
                    }
                }
            }
        }
        return Collections.singletonList(currentNode);
    }

    /**
     * create DependencyTree from file system recursively
     *
     * @param root current root Node which's children are created
     * @return current Node with its children
     */
    public void createTree(Node root) {
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
        List<String> fileNames = Arrays.stream(files).map(File::getName).collect(Collectors.toList());
        if (fileNames.contains("src")) {
            for (File file : files) {
                if (file.getName().equals("src")) {
                    // check if there is a child with 'src/main/java/{basepackage}'
                    String packagePath = "/main/java/" + basepackage;
                    File childToContinue = new File(file.getPath() + packagePath);
                    if (childToContinue.exists()) {
                        for (File f : Objects.requireNonNull(childToContinue.listFiles())) {
                            // if such a file exists use it instead of the current file
                            String packageName = (root.getPackageName().equals("") ? f.getName() : root.getPackageName() + "." + f.getName());

                            Node packageSkipNode = new Node(new LinkedList<>(), f.getPath(), f.getName(), packageName);
                            if (f.isDirectory()) {
                                createTree(packageSkipNode);
                            }
                            if (!packageSkipNode.getFilename().endsWith(".java") && packageSkipNode.getChildren().isEmpty()) {
                                continue;
                            }
                            root.getChildren().add(packageSkipNode);
                            continue;
                        }
                    } else {
                        Node node;
                        if (file.getPath().contains("src/main/java") || file.getPath().contains("src\\main\\java")) {
                            node = new Node(new LinkedList<>(), file.getPath(), file.getName(), root.getPackageName() + "." + file.getName());
                        } else {
                            node = new Node(new LinkedList<>(), file.getPath(), file.getName(), "");
                        }
                        if (file.isDirectory()) {
                            createTree(node);
                        }
                        if (!node.getFilename().endsWith(".java") && node.getChildren().isEmpty()) {
                            continue;
                        }
                        root.getChildren().add(node);
                    }
                }
            }
        } else {
            for (File file : files) {
                // skip file if file is
                //   a non java file
                //   a hidden package (beginning with a '.')
                //   an output package (named 'build', 'out', or 'classes')
                //   a node_modules directory
                Matcher forbiddenDirs = cache.getPattern("(^\\.|build|out|classes|node_modules)").matcher(file.getName());
                if (!file.isDirectory() && !file.getName().endsWith(".java") || forbiddenDirs.find()) {
                    continue;
                }

                // if filename equals src
                //   check if children with src/main/java exist

                Node node;
                if (file.getPath().contains("src/main/java") || file.getPath().contains("src\\main\\java")) {
                    node = new Node(new LinkedList<>(), file.getPath(), file.getName(), root.getPackageName() + "." + file.getName());
                } else {
                    node = new Node(new LinkedList<>(), file.getPath(), file.getName(), "");
                }
                if (file.isDirectory()) {
                    createTree(node);
                }
                if (!node.getFilename().endsWith(".java") && node.getChildren().isEmpty()) {
                    continue;
                }
                root.getChildren().add(node);
            }
        }
    }

    /**
     * sort the children of a given Node object and their children recursively:
     * if o1 has a dependency on o2 and o2 does not have an dependency on o1
     *   o1 is before o2
     * else if o2 has a dependency on o1 and o1 does not have an dependency on o2
     *   o2 is before o1
     * else if o1 has more dependencies on o2 than o2 on o1
     *   o1 is before o2
     * else if o2 has more dependencies on o1 than o1 on o2
     *   o2 is before o1
     * else if o1 has more dependencies than o2
     *   o1 is before o2
     * else if o2 has more dependencies than o1
     *   o2 is before o1
     * else if o1 is a directory and o2 is not
     *   o1 is before o2
     * else if o2 is a directory and o1 is not
     *   o2 is before o1
     * else compare o1 and o2 lexically
     *
     * @param node Node object which's children are to sort
     */
    public void sortTree(Node node) {
        if (node.hasChildren()) {
            NodeComparator nodeComparator = new NodeComparator();
            for (Node child : node.getChildren()) {
                sortTree(child);
            }
            node.getChildren().sort(nodeComparator);
        }
    }

    /**
     * Set the display layer of a given Node object's children and their children recursively
     *
     * @param node Node object which children's display layer is set
     */
    public void setLayer(Node node) {
        int layer = 0;
        for (int i = 0; i < node.getChildren().size(); i++) {
            // for every child in the current layer check
            for (int j = 0; j < i; j++) {
                if (node.getChildren().get(j).getLayer() == layer) {
                    // if a child in the current layer has a dependency on the child[i] and child[i] has no dependency on it
                    // or a child in the current row has more dependencies on the child[i] than the child[i] has on it
                    if (node.getChildren().get(j).hasDependencyOn(node.getChildren().get(i)) && !node.getChildren().get(i).hasDependencyOn(node.getChildren().get(j))) {
                        // raise row
                        layer++;
                    } else if (node.getChildren().get(j).countDependenciesOn(node.getChildren().get(i)).size() > node.getChildren().get(i).countDependenciesOn(node.getChildren().get(j)).size()) {
                        // raise layer
                        layer++;
                    }
                }
            }
            node.getChildren().get(i).setLayer(layer);
            setLayer(node.getChildren().get(i));
        }
    }

    /**
     * toString method for DependencyTree. Overridden for testing purposes.
     *
     * @return DependencyTree object.toString()
     */
    @Override
    public String toString() {
        Node node = baseroot;
        if (!node.hasChildren()) {
            throw new IllegalArgumentException("folder is not a Directory");
        }
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printDirectoryTree(node, indent, sb);
        return sb.toString();
    }

    /**
     * helper method for toString()
     */
    private void printDirectoryTree(Node node, int indent, StringBuilder sb) {
        if (!node.hasChildren()) {
            throw new IllegalArgumentException("folder is not a Directory");
        }
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(node.getFilename());
        sb.append(": ");
        sb.append(node.getLayer());
        sb.append("; ");
        sb.append(node.getDependencies());
        sb.append("/");
        sb.append("\n");
        for (Node child : node.getChildren()) {
            if (child.hasChildren()) {
                printDirectoryTree(child, indent + 1, sb);
            } else {
                printFile(child, indent + 1, sb);
            }
        }
    }

    /**
     * helper method for toString()
     */
    private void printFile(Node node, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(node.getFilename());
        sb.append(": ");
        sb.append(node.getLayer());
        sb.append("\n");
    }

    /**
     * helper method for toString()
     */
    private String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }
}
