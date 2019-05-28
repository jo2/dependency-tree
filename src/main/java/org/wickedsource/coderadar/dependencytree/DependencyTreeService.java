package org.wickedsource.coderadar.dependencytree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.LinkedList;

@RestController
@CrossOrigin(origins = "*")
public class DependencyTreeService {

    private static final String BASEPACKAGE = "org/wickedsource/coderadar";
    private static final String BASEPACKAGE_DOT = "org.wickedsource.coderadar";
    private static Node BASEROOT;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/getTree")
    public String getDependencyTree() {
        String projectdir = "C:/Users/teklote/Documents/git/coderadar/coderadar-server/coderadar-core/src/main/java/";
        File rootFile = new File(projectdir + BASEPACKAGE);
        if (rootFile.isDirectory()) {

            BASEROOT = new Node(new LinkedList<>(), rootFile.getPath(), rootFile.getName(), BASEPACKAGE_DOT);
            DependencyTree dependencyTree = new DependencyTree(BASEPACKAGE, BASEPACKAGE_DOT, BASEROOT);
            dependencyTree.createTree(BASEROOT);
            dependencyTree.setDependencies(BASEROOT);
            BASEROOT.setDependencies(new LinkedList<>());
            dependencyTree.sortTree(BASEROOT);
            dependencyTree.setLayer(BASEROOT);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Node.class,new NodeSerializer());
        objectMapper.registerModule(simpleModule);
        try {
            String serialized = objectMapper.writeValueAsString(BASEROOT);
            return serialized;
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }

        return null;
    }
}
