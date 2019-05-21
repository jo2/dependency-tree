//Don't import this
//import org.wickedsource.dependencytree.test.somepackage.NotADependencyTest;







public class InvalidDependencyTest{

    //Don't detect imports in strings
    String importBamboozleString = "import org.wickedsource.dependencytree.test.somepackage.NotADependencyTest;";

    public InvalidDependencyTest(){
        //Don't import this
        import org.wickedsource.dependencytree.test.somepackage.NotADependencyTest;
    }

}