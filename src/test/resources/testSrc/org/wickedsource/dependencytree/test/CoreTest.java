//These imports shouldn't matter to the DependencyTree
import java.util.Objects;
import java.io.BufferedReader;
import org.wickedsource.dependencytree.test.somepackage.CoreDependencyTest;
//Wildcard imports should add all classes contained in the package as dependencies
import org.wickedsource.dependencytree.test.wildcardpackage.*;


//Test code for dependencyTree
public class CoreTest{

    org.wickedsource.dependencytree.test.somepackage.FullyClassifiedDependencyTest fullyClassifiedDependencyTest;

    public CoreTest{

    }

}