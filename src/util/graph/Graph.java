package util.graph;

public class Graph {

    int nodecount = 0;
    //Premier sommet du graphe
    NodeList mynodes;
    //Dernier sommet du graphe
    NodeList mylast;

    public NodeList nodes() {
        return mynodes;
    }

    //Ajouter un sommet au graphe
    public Node newNode() {
        return new Node(this);
    }

    void check(Node n) {
        if (n.mygraph != this)
            throw new Error("Graph.addEdge using nodes from the wrong graph");
    }

    public int nodeCount() {
        return nodecount;
    }

    static boolean inList(Node a, NodeList l) {
        for (NodeList p = l; p != null; p = p.tail)
            if (p.head == a) return true;
        return false;
    }

    public Node[] nodeArray() {
        Node[] array = new Node[nodecount];
        for (NodeList q = mynodes; q != null; q = q.tail) {
            array[q.head.mykey] = q.head;
        }
        return array;
    }

    //Ajouter un arc au graphe
    public void addEdge(Node from, Node to) {
        check(from);
        check(to);
        if (from.goesTo(to)) return;
        to.preds = new NodeList(from, to.preds);
        from.succs = new NodeList(to, from.succs);
    }

    public void addNOEdge(Node from, Node to) {
        check(from);
        check(to);
        if (!from.goesTo(to)) {
            to.preds = new NodeList(from, to.preds);
            from.succs = new NodeList(to, from.succs);
        }
        if (!to.goesTo(from)) {
            from.preds = new NodeList(to, from.preds);
            to.succs = new NodeList(from, to.succs);
        }
    }


    NodeList delete(Node a, NodeList l) {
        if (l == null) throw new Error("Graph.rmEdge: edge nonexistent");
        else if (a == l.head) return l.tail;
        else return new NodeList(l.head, delete(a, l.tail));
    }

    public void rmEdge(Node from, Node to) {
        to.preds = delete(from, to.preds);
        from.succs = delete(to, from.succs);
    }

    /**
     * Print a human-readable dump for debugging.
     */
    public void show(java.io.PrintStream out) {
        for (NodeList p = nodes(); p != null; p = p.tail) {
            Node n = p.head;
            out.print(n.toString());
            out.print(": ");
            for (NodeList q = n.succ(); q != null; q = q.tail) {
                out.print(q.head.toString());
                out.print(" ");
            }
            out.println();
        }
    }

}
