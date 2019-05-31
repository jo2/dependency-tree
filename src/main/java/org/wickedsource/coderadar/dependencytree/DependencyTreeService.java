package org.wickedsource.coderadar.dependencytree;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "*")
public class DependencyTreeService {

    private static final String BASEPACKAGE = "org/wickedsource/coderadar";
    private static final String BASEPACKAGE_DOT = "org.wickedsource.coderadar";
    private static Node BASEROOT;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/getTree")
    public Node getDependencyTree() {
        String projectdir = "C:/Users/teklote/Documents/git/coderadar";
//        String projectdir = "C:/Users/teklote/Documents/git/coderadar/coderadar-server/coderadar-core/src/main/java/";

        File rootFile = new File(projectdir);
//        File rootFile = new File(projectdir + BASEPACKAGE);
        if (rootFile.isDirectory()) {

            BASEROOT = new Node(new LinkedList<>(), rootFile.getPath(), rootFile.getName(), "");
            DependencyTree dependencyTree = new DependencyTree(BASEPACKAGE, BASEPACKAGE_DOT, BASEROOT);
            dependencyTree.createTree(BASEROOT);
            dependencyTree.setDependencies(BASEROOT);
            BASEROOT.setDependencies(new LinkedList<>());
            dependencyTree.sortTree(BASEROOT);
            dependencyTree.setLayer(BASEROOT);
        }
        return BASEROOT;
    }

    /**
     * Get a list of all paths where 'src/main/java/BASEPACKAGE' follows.
     * @param base projectDir acting as base
     * @return List of paths to the modules
     */
    private List<String> getModulePaths(File base) {
        List<String> modulePaths = new LinkedList<>();
        if (base.exists() && base.isDirectory()) {
            // for all children
            for (File f : Objects.requireNonNull(base.listFiles())) {
                // check if filePath contains src/main/java
                if (f.getPath().contains("/src/main/java") || f.getPath().contains("\\src\\main\\java")) {
                    System.out.println(f.getPath());
                    // module found
                    // get name and path of module and add it to list
                    modulePaths.add(f.getParentFile().getParentFile().getParentFile().getPath());
                } else {
                    modulePaths.addAll(getModulePaths(f));
                }
            }
        }
        return modulePaths;
    }
}
