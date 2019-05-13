package org.wickedsource.coderadar.dependencytree;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {
    public int compare(Node o1, Node o2) {
        // if o1 has a dependency on o2 and o2 does not have an dependency on o1
        //   return -1
        // else if o2 has a dependency on o1 and o1 does not have an dependency on o2
        //   return 1
        // else if o1 has more dependencies on o2 than o2 on o1
        //   return -1
        // else if o2 has more dependencies on o1 than o1 on o2
        //   return 1
        // else if o1 has more dependencies than o2
        //   return -1
        // else if o2 has more dependencies than o1
        //   return 1
        // else
        //   return 0
        if (o1.hasDependencyOn(o2) && !o2.hasDependencyOn(o1)) {
            return -1;
        } else if (o2.hasDependencyOn(o1) && !o1.hasDependencyOn(o2)) {
            return 1;
        } else if (o1.countDependenciesOn(o2).size() > o2.countDependenciesOn(o1).size()) {
            return -1;
        } else if (o1.countDependenciesOn(o2).size() < o2.countDependenciesOn(o1).size()) {
            return 1;
        } else if (o1.getDependencies().size() > o2.getDependencies().size()) {
            return -1;
        } else if (o1.getDependencies().size() < o2.getDependencies().size()) {
            return 1;
        } else {
            return 0;
        }
    }
}
