import java.util.*;


/**
 * Library for graph analysis
 * 
 * @author Kieran O'Day for SA7 and PS4
 * 
 */
public class GraphLib {
	
	/*
	 * returns a tree of paths to all the places with ability to backtrack to make a path
	 */
	public static <V,E> Graph<V,E> bfs(Graph <V,E> g, V source){
		if(g == null)
			System.err.println("Graph is null");
		if(!g.hasVertex(source)) 
			System.err.println(source + " not in graph");
		Graph<V,E> bfs = new AdjacencyMapGraph<V,E>();
		Queue<V> toVisit = new LinkedList<V>(); //queue so its a breadth first search (apparently linked lists implement queue cool)
		toVisit.add(source); //start with the source
		bfs.insertVertex(source);
		while(!toVisit.isEmpty()) {
			V cur = toVisit.remove(); //take off the current vertex
			for(V neighbor: g.outNeighbors(cur)) { //add all the neighbors that haven't been added yet
				if(!bfs.hasVertex(neighbor)) { 
					toVisit.add(neighbor); //look at these neighbors later
					bfs.insertVertex(neighbor); //add the neighbor to the bfs graph
					bfs.insertDirected(neighbor, cur, bfs.getLabel(cur, neighbor)); //link back to the one before for backtracking
				}
			}
		}
		return bfs;
	}
	
	/**
	 * Makes a path from the node back to the source by backtracking along the bfs tree
	 * @param tree the bfs tree
	 * @param v the node you want to end at
	 * @return the path to that node starting at the source
	 * @throws Exception
	 */
	public static <V,E> List<V> getPath(Graph<V,E> tree, V v){
		if(tree == null) {
			System.err.println("Null tree");
			return null;
		}
		if(!tree.hasVertex(v)) {
			System.err.println("No path to " + v);
			return null;
		}
		List<V> path = new ArrayList<V>();
		V current = v;
		path.add(current); //start with the end point
		while(tree.outDegree(current) > 0) { //add the other points to the beginning of the path until the root is reached
			V next = tree.outNeighbors(current).iterator().next();
			path.add(next);
			current = next;
		}
		return path;
	}
	
	/**
	 * Returns which vertices are in graph but not in sub graph
	 * @param graph
	 * @param subgraph
	 * @return the missing vertices
	 */
	public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph){
		Set<V> missing = new HashSet<V>();
		for (V vertex: graph.vertices())
			if(!subgraph.hasVertex(vertex))
				missing.add(vertex);
		return missing;
	}
	
	/**
	 * Returns the average length of paths from the source to any other node
	 * @param tree
	 * @param root
	 * @return
	 * @throws Exception
	 */
	public static <V,E> double averageSeparation(Graph<V,E> tree, V root){
		if(!tree.hasVertex(root)) {
			System.err.println(root + " not in tree");
			return 0;
		}
		return totalSep(tree,root,0)/(tree.numVertices()-1);
	}
	public static <V,E> double totalSep(Graph<V,E> tree, V root, double depth) {
		Double retVal = depth;
		for(V neighbor: tree.inNeighbors(root)) {
			retVal += totalSep(tree,neighbor,depth + 1);
		}
		return retVal;
	}
	/**
	 * Takes a random walk from a vertex, up to a given number of steps
	 * So a 0-step path only includes start, while a 1-step path includes start and one of its out-neighbors,
	 * and a 2-step path includes start, an out-neighbor, and one of the out-neighbor's out-neighbors
	 * Stops earlier if no step can be taken (i.e., reach a vertex with no out-edge)
	 * @param g		graph to walk on
	 * @param start	initial vertex (assumed to be in graph)
	 * @param steps	max number of steps
	 * @return		a list of vertices starting with start, each with an edge to the sequentially next in the list;
	 * 			    null if start isn't in graph
	 */
	public static <V,E> List<V> randomWalk(Graph<V,E> g, V start, int steps){
		List<V> vertices = new ArrayList<V>();
		if(!g.hasVertex(start)) {
			System.err.println(new Exception("Node " + start + " doesn't exist"));
			return vertices;
		}
		vertices.add(start);
		V current = start;
		for(int i = 0; i < steps; i ++) {
			int rn = (int)(Math.random() * g.outDegree(current));
			for (V v: g.outNeighbors(current)) {
				if(rn == 0) {
					current = v;
					vertices.add(current);
					break;
				}
				rn--;
			}
		}
		return vertices;
	}
	
	/**
	 * Orders vertices in decreasing order by their in-degree
	 * 
	 * sort sorts in ascending order by default, comparator should return - if v1>v2 and + in v2>v1 so it should be v2-v1
	 * @param g		graph
	 * @return		list of vertices sorted by in-degree, decreasing (i.e., largest at index 0)
	 */
	public static <V,E> List<V> verticesByInDegree(Graph<V,E> g) {
		List<V> vertices = new ArrayList<V>();
		for(V v: g.vertices()) {
			vertices.add(v);
		}
		vertices.sort((V v1, V v2) -> g.inDegree(v2) - g.inDegree(v1));
		return vertices;
	}
	public static <V,E> List<V> verticesByOutDegree(Graph<V,E> g) {
		List<V> vertices = new ArrayList<V>();
		for(V v: g.vertices()) {
			vertices.add(v);
		}
		vertices.sort((V v1, V v2) -> g.outDegree(v2) - g.outDegree(v1));
		return vertices;
	}
	
	
	public static void main(String [] args) {
		Graph<String,String> test = new AdjacencyMapGraph<String,String>();
		test.insertVertex("Kevin Bacon");
		test.insertVertex("Bob");
		test.insertVertex("Alice");
		test.insertVertex("Charlie");
		test.insertVertex("Dartmouth");
		test.insertVertex("Nobody");
		test.insertVertex("Nobody's Friend");
		test.insertUndirected("Kevin Bacon", "Alice", "A Movie, E Movie");
		test.insertUndirected("Kevin Bacon", "Bob", "A Movie");
		test.insertUndirected("Alice", "Bob", "A Movie");
		test.insertUndirected("Alice", "Charlie", "D Movie");
		test.insertUndirected("Bob", "Charlie", "C Movie");
		test.insertUndirected("Charlie", "Dartmouth", "B Movie");
		test.insertUndirected("Nobody", "Nobody's Friend", "F Movie");

		try {
			Graph<String,String> pathGraph = bfs(test,"Kevin Bacon");
			System.out.println(getPath(pathGraph, "Dartmouth"));
			System.out.println(missingVertices(test, pathGraph));
			System.out.println(averageSeparation(pathGraph,"Kevin Bacon"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
}
