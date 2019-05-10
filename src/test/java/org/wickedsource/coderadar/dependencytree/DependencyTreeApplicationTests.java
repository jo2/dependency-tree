package org.wickedsource.coderadar.dependencytree;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;

import static org.junit.Assert.*;


public class DependencyTreeApplicationTests {

    private static final String BASEPACKAGE = "org/wickedsource/dependencytree/test";
    private static final String BASEPACKAGE_DOT = "org.wickedsource.dependencytree.test";
    private static Node BASEROOT;


    DependencyTree dependencyTree;

    @Before
    public void init(){
        String projectdir = "C:/Users/vossgaetter/Documents/repos/dependency-tree/src/test/resources/testSrc/";
        File rootFile = new File(projectdir + BASEPACKAGE);
        assertTrue("specified test directory doesn't exist",rootFile.exists());
        if (rootFile.isDirectory()) {

            BASEROOT = new Node(new LinkedList<>(), rootFile.getPath(), rootFile.getName(), BASEPACKAGE_DOT);
            dependencyTree = new DependencyTree(BASEPACKAGE, BASEPACKAGE_DOT, BASEROOT);
        }
    }

    private void createTree(){
        BASEROOT = dependencyTree.createTree(BASEROOT);
    }

    private void setDependencies(){
        BASEROOT = dependencyTree.setDependencies(BASEROOT);
    }


    Node emptypackage;
    Node somepackage;
    Node wildcardpackage;
    Node CoreTest;
    Node NotASourceFile;

    Node CircularDependencyTest;
    Node CoreDependency;
    Node NotADependencyTest;
    Node InvalidDependencyTest;
    Node DuplicateDependenciesTest;
    Node DuplicateDependencies2Test;

    Node WildcardImport1Test;
    Node WildcardImport2Test;
    Node WildcardImportCircularDependencyTest;

    private void createTreeTest() {
        createTree();
        assertNotNull(BASEROOT);
        assertEquals("Incorrect package name on basepackage",BASEROOT.getPackageName(),BASEPACKAGE_DOT);
        assertEquals("Incorrect file name on basepackage",BASEROOT.getFilename(),"test");
        assertTrue("Incorrect path on basepackage",BASEROOT.getPath().endsWith(BASEPACKAGE.replace("/","\\")));
        assertTrue("Incorrect number of children in basepackage",BASEROOT.getChildren().size()==new File(BASEROOT.getPath()).listFiles().length);

        //Testing for node existence

        assertNotNull(emptypackage      = BASEROOT.getChildByName("emptypackage"));
        assertNotNull(somepackage       = BASEROOT.getChildByName("somepackage"));
        assertNotNull(wildcardpackage   = BASEROOT.getChildByName("wildcardpackage"));
        assertNotNull(CoreTest          = BASEROOT.getChildByName("CoreTest.java"));
        assertNull("Not a valid java source file therfore",
                NotASourceFile          = BASEROOT.getChildByName("NotASourceFile.txt"));

        assertNotNull(CircularDependencyTest        = somepackage.getChildByName("CircularDependencyTest.java"));
        assertNotNull(CoreDependency                = somepackage.getChildByName("CoreDependencyTest.java"));
        assertNotNull(NotADependencyTest            = somepackage.getChildByName("NotADependencyTest.java"));
        assertNotNull(DuplicateDependenciesTest     = somepackage.getChildByName("DuplicateDependenciesTest.java"));
        assertNotNull(DuplicateDependencies2Test    = somepackage.getChildByName("DuplicateDependencies2Test.java"));
        assertNotNull(InvalidDependencyTest         = somepackage.getChildByName("InvalidDependencyTest.java"));

        assertNotNull(WildcardImport1Test                   = wildcardpackage.getChildByName("WildcardImport1Test.java"));
        assertNotNull(WildcardImport2Test                   = wildcardpackage.getChildByName("WildcardImport2Test.java"));
        assertNotNull(WildcardImportCircularDependencyTest  = wildcardpackage.getChildByName("WildcardImportCircularDependencyTest.java"));
    }

    private boolean compare(Node o1, Node o2) {
        if (o1.hasDependencyOn(o2)) {
            return false;
        } else if (o1.getDependencies().size() > o2.getDependencies().size()) {
            return false;
        }
        return true;
    }

    private boolean checkOrderRecursive(Node currentNode){
        if(currentNode.hasChildren()) {
            Node childBefore = null;
            for (Node child : currentNode.getChildren()){
                if(childBefore!=null){
                    if(!compare(childBefore,child)){
                        return false;
                    }
                }
                if(!checkOrderRecursive(child))return false;
                childBefore=child;
            }
        }
        return true;
    }

    private void sortTreeTest(){
        dependencyTree.sortTree(BASEROOT);
        assertTrue("Tree is not in order after sort",checkOrderRecursive(BASEROOT));
    }

    private void setDependenciesTest() {
        //work in progress
        setDependencies();

        //explicit and wildcard imports
        assertTrue(CoreTest.hasDependencyOn(CoreDependency));
        assertTrue(CoreTest.hasDependencyOn(WildcardImport1Test));
        assertTrue(CoreTest.hasDependencyOn(WildcardImport2Test));
        assertTrue(CoreTest.hasDependencyOn(WildcardImportCircularDependencyTest));
        assertEquals(4,CoreTest.getDependencies().size());

        //circular dependencies
        assertTrue(CircularDependencyTest.hasDependencyOn(CoreTest));

        //duplicate dependencies
        assertEquals(4,DuplicateDependenciesTest.getDependencies().size());
        assertEquals(3,DuplicateDependencies2Test.getDependencies().size());

        //invalid dependencies
        assertTrue("Some import statements are wrongfully processed",InvalidDependencyTest.getDependencies().isEmpty());

        //circular wildcard dependencies
        assertTrue(WildcardImportCircularDependencyTest.hasDependencyOn(CoreTest));
    }

    @Test
    public void testDependencyTree(){
        createTreeTest();
        sortTreeTest();
        setDependenciesTest();
        sortTreeTest();
    }

}
