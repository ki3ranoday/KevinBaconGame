import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

public class KevinBaconGame {
	private Graph<String, Set<String>> universe;
	private String center;
	private Graph<String, Set<String>> BFS;
	private Graph<String, Set<String>> KevinsUniverse;
	
	public KevinBaconGame(File actors, File movies, File movies_actors) throws Exception {
		makeGraph(actors, movies, movies_actors);
		System.out.println("Commands:\n" +
						   "c <#>:          list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation\n" + 
						   "d <low> <high>: list actors sorted by degree, with degree between low and high\n" + 
						   "i:              list actors with infinite separation from the current center\n" + 
						   "p <name>:       find path from <name> to current center of the universe\n" + 
						   "s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high\n" + 
						   "u <name>:       make <name> the center of the universe\n" + 
						   "q:              quit game\n" +
						   "h:              help");
		KevinsUniverse = GraphLib.bfs(universe, "Kevin Bacon");
		makeCenter("Kevin Bacon");
	}

	
	/**
	 * list top (pos num) or bottom (neg num) centers of universe sorted by avrg sep
	 * @param num
	 */
	public void listCenters(String num){
		try {
			int n = Integer.parseInt(num);
			Map<String, Double> centersMap = new HashMap<String, Double>();
			PriorityQueue<String> centersQueue;
			if(n < 0) { //if n is less than 0, sort them in descending order
				centersQueue = new PriorityQueue<String>((String s1, String s2)->
				(int)(10000 * (centersMap.get(s2).doubleValue() - centersMap.get(s1).doubleValue())));
			} else { //otherwise sort them in ascending order
				centersQueue = new PriorityQueue<String>((String s1, String s2)->
				(int)(10000 * (centersMap.get(s1).doubleValue() - centersMap.get(s2).doubleValue())));
			}
			for (String actor: universe.vertices()) {
				if(KevinsUniverse.hasVertex(actor)) { //limit it to people who are connected to kevin bacon
					Graph <String, Set<String>> bfs = GraphLib.bfs(universe, actor);
					centersMap.put(actor, GraphLib.averageSeparation(bfs,actor));
					centersQueue.add(actor);
				}
			}
			for(int i = 0; i < Math.abs(n); i ++) {
				String actor;
				if(!centersQueue.isEmpty()) {
					actor = centersQueue.poll();
					System.out.println(actor + "'s average seperation is " + centersMap.get(actor));
				}
			}
		}catch (Exception e) {
			System.err.println(num + "not an integer");
		}
		
	}
	
	/**
	 * list actors sorted by degree with degree between low and high
	 * @param l low
	 * @param h high
	 */
	public void degreesBetween(String l, String h) {
		PriorityQueue<String> actors = new PriorityQueue<String>((String s1, String s2) -> universe.outDegree(s2) - universe.outDegree(s1));
		int low = Integer.parseInt(l);
		int high = Integer.parseInt(h);
		for(String actor: universe.vertices())
			if(universe.outDegree(actor) >= low && universe.outDegree(actor) <= high)
				actors.add(actor);
		
		while(!actors.isEmpty()) {
			String actor  = actors.remove();
			System.out.println(actor + " has degree " + universe.outDegree(actor));
		}
	}
	/**
	 * list actors with infinite separation from current center (no path between)
	 */
	public void cantReach() {
		System.out.println("No path from " + center + " to: ");
		for(String actor: GraphLib.missingVertices(universe, BFS)) {
			System.out.println("  " + actor);
		}
		
	}
	/**
	 * find the path from name to the center of the universe
	 * @param name
	 */
	public void path(String name) {
		List<String> path =  GraphLib.getPath(BFS, name);
		if(path != null) {
			System.out.println(name + "'s number is " + (path.size() - 1));
			for (int i = 0; i < path.size()-1; i ++) {
				System.out.println(path.get(i) + " was in " + universe.getLabel(path.get(i),  path.get(i+1)) + " with " + path.get(i+1));
			}
		}
	}
	
	/**
	 * list actors sort by seperation from center between low and high seperations
	 * @param low
	 * @param high
	 */
	public void seperationsBetween(String l, String h) {
		PriorityQueue<String> actors = new PriorityQueue<String>((String s1, String s2) -> (GraphLib.getPath(BFS, s2).size()-1)-(GraphLib.getPath(BFS, s1).size()-1));
		int low = Integer.parseInt(l);
		int high = Integer.parseInt(h);
		for(String actor: BFS.vertices())
			if(GraphLib.getPath(BFS, actor) != null)
				if((GraphLib.getPath(BFS, actor).size()-1) >= low && (GraphLib.getPath(BFS, actor).size()-1) <= high)
					actors.add(actor);
		
		while(!actors.isEmpty()) {
			String actor  = actors.remove();
			System.out.println(actor + " is " + (GraphLib.getPath(BFS, actor).size()-1) + " away from " + center);
		}
	}
	/**
	 * make name the center of the universe
	 * @param name of the center of the universe
	 */
	public void makeCenter(String name) {
		center = name;
		BFS = GraphLib.bfs(universe, name);
		System.out.println(name + " is now the center of the universe, average separation " + GraphLib.averageSeparation(BFS, name) + ", connected to " + (universe.numVertices() - GraphLib.missingVertices(universe, BFS).size()) + "/" + universe.numVertices() + " actors");
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws Exception 
	 */
	public void makeGraph(File actors, File movies, File movies_actors) throws Exception {
		universe = new AdjacencyMapGraph<String, Set<String>>(); //initialize the graph
		Map <Integer, String> a = new HashMap <Integer, String>(); //id-actor map
		Map <Integer, String> m = new HashMap <Integer, String>(); //id-movie map
		Map <String, ArrayList<String>> ma = new HashMap <String, ArrayList<String>>(); //movie-actors map
		
		BufferedReader in = new BufferedReader(new FileReader(actors));
		while(in.ready()) { //read the actors into a id-name map
			String line = in.readLine();
			Integer actorId = Integer.parseInt(line.split("\\|")[0]);
			String actorName = line.split("\\|")[1];
			a.put(actorId, actorName);
			universe.insertVertex(actorName); //add the actors as vertices of the graph
		}
		in = new BufferedReader(new FileReader(movies));
		while(in.ready()) { //read the movies into an id-name map and set up a movie-list of actors map
			String line = in.readLine();
			Integer movieId = Integer.parseInt(line.split("\\|")[0]);
			String movieName = line.split("\\|")[1];
			m.put(movieId, movieName);
			ma.put(movieName, new ArrayList<String>()); //initialize arraylists in the movie-actor map
		}
		in = new BufferedReader(new FileReader(movies_actors));
		while(in.ready()) { //put the actors into the movies they play in
			String line = in.readLine();
			String movieName = m.get(Integer.parseInt(line.split("\\|")[0])); //get the movie name from the key
			String actorName = a.get(Integer.parseInt(line.split("\\|")[1])); //get the actor name from the key
			ma.get(movieName).add(actorName); //add the actor name to the movie's list
		}
		for(String movie: ma.keySet()) { // go through all the movies and make connections between actors in the movies
			for(int i = 0; i < ma.get(movie).size(); i ++) { //go through every possible pair in the movie's actors
				for(int j = i + 1; j < ma.get(movie).size(); j ++) {
					if(!universe.hasEdge(ma.get(movie).get(i), ma.get(movie).get(j))) { //if there isn't already a connection make a new connection
						universe.insertUndirected(ma.get(movie).get(i), ma.get(movie).get(j), new HashSet<String>());
					}
					universe.getLabel(ma.get(movie).get(i), ma.get(movie).get(j)).add(movie); //add the movie to the edge set of movies
				}
			}
		}
	}
	
	public void playGame() {
		Scanner in = new Scanner(System.in);
		
		while(true) {
			String line = in.nextLine();
			try {
				if(line.split(" ")[0].equals("c")) {
					listCenters(line.substring(2));
				}else if(line.split(" ")[0].equals("d")) {
					degreesBetween(line.split(" ")[1],line.split(" ")[2]);
				}else if(line.equals("i")) {
					cantReach();
				}else if(line.split(" ")[0].equals("p")) {
					path(line.substring(2));
				}else if(line.split(" ")[0].equals("s")) {
					seperationsBetween(line.split(" ")[1],line.split(" ")[2]);
				}else if(line.split(" ")[0].equals("u")) {
					makeCenter(line.substring(2));
				}else if(line.equals("q")) {
					System.out.println("Bye Bye see you again soon");
					break;
				}else if(line.equals("h")) {
					System.out.println("Commands:\n" +
							   "c <#>:          list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation\n" + 
							   "d <low> <high>: list actors sorted by degree, with degree between low and high\n" + 
							   "i:              list actors with infinite separation from the current center\n" + 
							   "p <name>:       find path from <name> to current center of the universe\n" + 
							   "s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high\n" + 
							   "u <name>:       make <name> the center of the universe\n" + 
							   "q:              quit game\n"+
							   "h:              help");
				}else {
					System.err.println("Command not found");
				}
			}catch (Exception e) {
				System.err.println("Command not found");
			}
		}
	}
	
	public static void main(String [] args) {
		try {
			KevinBaconGame game = new KevinBaconGame(new File("PS4/actors.txt"),new File("PS4/movies.txt"), new File("PS4/movie-actors.txt"));
			//System.out.println("Graph:\n" + game.universe);
			game.playGame();
		}catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
