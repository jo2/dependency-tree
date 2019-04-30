package org.wickedsource.coderadar.dependencytree;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {
    public int compare(Node o1, Node o2) {
//        System.out.println(o1.getPackageName() + ": " + o1.getDependencies().size() + "; " + o2.getPackageName() + ":" + o2.getDependencies().size());
        if (o1.getDependencies().size() > o2.getDependencies().size()) {
            return 1;
        } else if (o1.getDependencies().size() < o2.getDependencies().size()) {
            return -1;
        } else {
            return Integer.compare(o1.countDependenciesOn(o2), o2.countDependenciesOn(o1));
        }
    }
}
