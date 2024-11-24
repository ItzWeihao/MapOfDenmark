package bfst22.vector;

import java.util.ArrayList;
import java.util.Comparator;

// A HashMap exclusively designed for nodes.
public class NodeMap extends ArrayList<PolyPoint> {
    boolean sorted;

    // adds a new node to the nodemap and corrects the sorted bool to false.
    public boolean add(PolyPoint node) {
        sorted = false;
        return super.add(node);
    }

    // gets the node from the map based on the given reference and sorts the map.
    // Uncertain as to why it sorts the map? Perhaps performance improvement?
    public PolyPoint get(final long ref) {
        if (!sorted) {
            sort(Comparator.comparing(node -> node.id));
            sorted = true;
        }
        int lo = 0;
        int hi = size();
        // I: get(lo).id <= ref < get(hi).id
        while (hi - lo > 1) {
            int mi = (lo + hi) / 2;
            if (get(mi).id <= ref) {
                lo = mi;
            } else {
                hi = mi;
            }
        }
        var node = get(lo);
        return node.id == ref ? node : null;
    }
}