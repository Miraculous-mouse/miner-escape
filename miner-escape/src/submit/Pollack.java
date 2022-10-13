package submit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import graph.FindState;
import graph.Finder;
import graph.FleeState;
import graph.Node;
import graph.NodeStatus;

/** A solution with find-the-Orb optimized and flee getting out as fast as possible. */
public class Pollack extends Finder {

    /** LinkedList to record whether a node has been visited */
    private LinkedList<Long> visited= new LinkedList<>();

    /** Get to the orb in as few steps as possible. <br>
     * Once you get there, you must return from the function in order to pick it up. <br>
     * If you continue to move after finding the orb rather than returning, it will not count.<br>
     * If you return from this function while not standing on top of the orb, it will count as <br>
     * a failure.
     *
     * There is no limit to how many steps you can take, but you will receive<br>
     * a score bonus multiplier for finding the orb in fewer steps.
     *
     * At every step, you know only your current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the orb at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * currentLoc(), neighbors(), and distanceToOrb() in FindState.<br>
     * You know you are standing on the orb when distanceToOrb() is 0.
     *
     * Use function moveTo(long id) in FindState to move to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position.
     *
     * A suggested first implementation that will always find the orb, but <br>
     * likely won't receive a large bonus multiplier, is a depth-first search. <br>
     * Some modification is necessary to make the search better, in general. */
    @Override
    public void find(FindState state) {
        // TODO 1: Walk to the orb
        long bp= state.currentLoc();
        if (state.distanceToOrb() == 0) return;
        Map<Integer, Long> neighborInfo= new HashMap<>();
        for (NodeStatus w : state.neighbors()) {
            if (!visited.contains(w.getId()))
                neighborInfo.put(w.getDistanceToTarget(), w.getId());
        }
        if (!neighborInfo.isEmpty()) {
            state.moveTo(keySortFirst(neighborInfo));
            visited.add(bp);
        } else {
            state.moveTo(visited.getLast());
            visited.addFirst(bp);
            visited.remove(visited.getLast());
        }
        find(state);
        if (state.distanceToOrb() != 0) state.moveTo(bp);
    }

    private long keySortFirst(Map<Integer, Long> neighborInfo) {
        // TODO Auto-generated method stub
        TreeMap<Integer, Long> sorted= new TreeMap<>();
        sorted.putAll(neighborInfo);
        return sorted.firstEntry().getValue();
    }

    /** Get out the cavern before the ceiling collapses, trying to collect as <br>
     * much gold as possible along the way. Your solution must ALWAYS get out <br>
     * before steps runs out, and this should be prioritized above collecting gold.
     *
     * You now have access to the entire underlying graph, which can be accessed <br>
     * through FleeState state. <br>
     * currentNode() and exit() will return Node objects of interest, and <br>
     * allsNodes() will return a collection of all nodes on the graph.
     *
     * Note that the cavern will collapse in the number of steps given by <br>
     * stepsLeft(), and for each step this number is decremented by the <br>
     * weight of the edge taken. <br>
     * Use stepsLeft() to get the steps still remaining, and <br>
     * moveTo() to move to a destination node adjacent to your current node.
     *
     * You must return from this function while standing at the exit. <br>
     * Failing to do so before steps runs out or returning from the wrong <br>
     * location will be considered a failed run.
     *
     * You will always have enough steps to flee using the shortest path from the <br>
     * starting position to the exit, although this will not collect much gold. <br>
     * For this reason, using Dijkstra's to plot the shortest path to the exit <br>
     * is a good starting solution
     *
     * Here's another hint. Whatever you do you will need to traverse a given path. It makes sense
     * to write a method to do this, perhaps with this specification:
     *
     * // Traverse the nodes in moveOut sequentially, starting at the node<br>
     * // pertaining to state <br>
     * // public void moveAlong(FleeState state, List<Node> moveOut) */
    @Override
    public void flee(FleeState state) {
        // TODO 2. Get out of the cavern in time, picking up as much gold as possible.
        if (state.currentNode() == state.exit()) return;
        HashMap<Double, Node> gdp= new HashMap<>();

        // HashSet<Node> gold =new HashSet<>();
        ArrayList<Double> r= new ArrayList<>();

        for (Node nodes : state.allNodes()) {
            if (!(nodes.getTile().gold() == 0)) {
                Double ratio= (double) (nodes.getTile().gold() /
                    Path.pathSum(Path.shortestPath(state.currentNode(), nodes)));

                r.add(ratio);
                // gold.add(nodes); //add all nodes with gold to the Hashset
                gdp.put(ratio, nodes);

            }
            // sort gold/dis ratio, biggest first
            Collections.sort(r, Collections.reverseOrder());
        }
        // List<Node> findamount= Path.shortestPath(state.currentNode(), sortTreemap(gdamount));
        // List<Node> fleepath= currentShortestPath(state);
        int i= 0;
        while (i < r.size()) {
            List<Node> sr= Path.shortestPath(state.currentNode(), gdp.get(r.get(i)));
            Integer current_coin= Path
                .pathSum(Path.shortestPath(state.currentNode(), gdp.get(r.get(i))));
            Integer toexit= Path.pathSum(Path.shortestPath(gdp.get(r.get(i)), state.exit()));
            if (state.stepsLeft() > current_coin + toexit) {
                shortestRun(state, sr);
                // i= r.size();
                flee(state);
            } else if (state.currentNode() == state.exit()) return;
            else {
                i++ ;
            }
        }

        List<Node> sr= Path.shortestPath(state.currentNode(), gdp.get(r.get(0)));
        List<Node> sr2= Path.shortestPath(state.currentNode(), gdp.get(r.get(1)));
        Integer current_coin= Path
            .pathSum(Path.shortestPath(state.currentNode(), gdp.get(r.get(0))));
        Integer sec_coin= Path
            .pathSum(Path.shortestPath(state.currentNode(), gdp.get(r.get(1))));

        Integer toexit= Path.pathSum(Path.shortestPath(gdp.get(r.get(0)), state.exit()));
        if (state.stepsLeft() > current_coin + toexit) {
            shortestRun(state, sr);
        } else if (state.stepsLeft() > sec_coin + toexit) shortestRun(state, sr2);
        else {
            List<Node> exit= Path.shortestPath(state.currentNode(), state.exit());
            for (Node a : exit) {
                if (state.currentNode().getNeighbors().contains(a)) state.moveTo(a);
                if (state.currentNode() == state.exit()) return;
            }
        }
        // flee(state);
    }

    // current node to exit, shortest path, 不用
    private List<Node> currentShortestPath(FleeState state) {
        List<Node> sp= Path.shortestPath(state.currentNode(), state.exit());
        return sp;
    }

    private void shortestRun(FleeState state, List<Node> sr) {
        for (Node e : sr) {
            if (state.currentNode() != e) state.moveTo(e);
            if (state.currentNode() == state.exit()) return;
        }
    }

    // 暂时不用
    private Node sortTreemap(Map<Integer, Node> gdamount) {
        // TODO Auto-generated method stub
        // use a heap with insert!!! poll once, the secone largest is the next to poll

        TreeMap<Integer, Node> sorted= new TreeMap<>();
        sorted.putAll(gdamount);
        return sorted.lastEntry().getValue();
    }

}
