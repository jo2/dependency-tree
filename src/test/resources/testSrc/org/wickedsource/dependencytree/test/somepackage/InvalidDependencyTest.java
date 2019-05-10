//Don't import this
//import org.wickedsource.dependencytree.test.somepackage.NotADependencyTest;

//None of these typo imports should appear in the tree dependencies
//Specified class does not exist         v
import org.wickedsource.depencytree.test.CoreTes;

//Don't improt this
improt org.wickedsource.depencytree.test.somepackage.NotADependencyTest;

//Specified package does not exist
//         v
import org.wicedsource.depencytree.test.CoreTest;

//import specification can't end on a dot
//                                         v
import org.wickedsource.dependencytree.test.;

//import specification can't start on a dot
//     v
import .org.wickedsource.dependencytree.test.somepackage.NotADependencyTest;

//import specification can't end on an asterisk without having a dot before it
//                                         v
import org.wickedsource.dependencytree.test*;

//import specification can't start on an asterisk
//     v
import *org.wickedsource.dependencytree.test;


public class InvalidDependencyTest{
    //Don't import this
    import org.wickedsource.dependencytree.test.somepackage.NotADependencyTest;

    //Don't detect imports in strings
    String importBamboozleString = "import org.wickedsource.dependencytree.test.somepackage.NotADependencyTest;";

    public InvalidDependencyTest(){
        //Don't import this
        import org.wickedsource.dependencytree.test.somepackage.NotADependencyTest;
    }

}
//Don't import this
import org.wickedsource.dependencytree.test.somepackage.NotADependencyTest;