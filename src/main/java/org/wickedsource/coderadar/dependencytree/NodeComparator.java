package org.wickedsource.coderadar.dependencytree;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {
    public int compare(Node o1, Node o2) {
        if (o1.hasDependencyOn(o2)) {
            return -1;
        } else if (o2.hasDependencyOn(o1)) {
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
