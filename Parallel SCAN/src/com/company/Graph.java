package com.company;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

/**
 * Created by striest on 11/20/18.
 */

/**
 * Simple graph class
 */
public class Graph {
    public HashMap<String, Vertex> vertices;

    public Graph(){
        this.vertices = new HashMap<>();
    }

    /**
     * Implementation of parallel SCAN:
     *  -process each vertex and compute its eps neighbors
     *  -then for each core, do the disjoint set data structure
     *  -Observation: we have every vertex's directly reachable neighbors.
     *
     * For each edge, set the parent of the
     *
     * @param eps
     * @param mu
     * @return
     */
    public HashMap<Vertex, Integer> PSCANCluster(double eps, int mu){
        int cluster_curr = 0;
        HashMap<Vertex, Integer> cluster_map = new HashMap<>();
        HashMap<Vertex, Vertex> parents = new HashMap<>();
        HashSet<Edge> core_edges = new HashSet<>();

        //Find all edges connecting to a core vertex, set up parent pointers to all vertices (pointer graph)
        for(Vertex v: vertices.values()){
            parents.put(v, v);
            if (isCore(v, eps, mu)){
                Collection<Vertex> n = directlyReachable(v, eps, mu);

                for(Vertex u: n){
                    core_edges.add(new Edge(u, v));
                }
            }
        }

        //add all core edges to worklist
        HashSet<Edge> worklist = new HashSet<>();
        worklist.addAll(core_edges);
        HashSet<Edge> worklist_next = new HashSet<>();
        int iters = 0;

        //run CC algorithm
        while(!worklist.isEmpty()){
            for(Edge e: worklist){
                //check if edge is done (vertices already in same component)
                if(!(parents.get(e.v1) == parents.get(e.v2))){
                    worklist_next.add(e);
                    boolean b = parents.get(e.v1).compareTo(parents.get(e.v2)) > 0;

                    if(iters % 2 == 0 && b){
                        parents.put(parents.get(e.v1), parents.get(e.v2));
                    } else if(!b) {
                        parents.put(parents.get(e.v2), parents.get(e.v1));
                    }

                }
            }

            //set parent pointers to root of current CC
            for(Vertex v: parents.keySet()){
                Vertex v_curr = v;
                while(parents.get(v_curr) != v_curr){
                    v_curr = parents.get(v_curr);
                }
                parents.put(v, v_curr);
            }

            worklist = worklist_next;
            worklist_next = new HashSet<>();
            iters ++;
        }

//        for(Vertex v: parents.keySet()){
//            System.out.print(v.getId() + "->" + parents.get(v).getId() + ", ");
//        }
        System.out.println("Niters=" + iters);

        //build cluster map
        HashMap<Vertex, Integer> seen_parents = new HashMap<>();
        HashMap<Vertex, Integer> cluster_map_temp = new HashMap<>();
        ArrayList<Integer> cluster_sizes = new ArrayList<>();

        for(Vertex v: parents.keySet()){
            Vertex parent = parents.get(v);
            if(!seen_parents.keySet().contains(parent)){
                seen_parents.put(parent, cluster_curr);
                cluster_curr ++;
                cluster_sizes.add(0);
            }
            int cluster = seen_parents.get(parent);
            cluster_map_temp.put(v, cluster);
            cluster_sizes.set(cluster, cluster_sizes.get(cluster) + 1);
        }

        //filter out singletons
        for(Vertex v: cluster_map_temp.keySet()){
            if(cluster_sizes.get(cluster_map_temp.get(v)) > 1){
                cluster_map.put(v, cluster_map_temp.get(v));
            }
        }

        return cluster_map;
    }

    /**
     * Implementation of SCAN according to paper
     * @param eps
     * @param mu
     * @return
     */
    public HashMap<Vertex, Integer> SCANCluster(double eps, int mu){
        int cluster_curr = 0;
        int iters = 1;
        HashMap<Vertex, Integer> cluster_map = new HashMap<>();
        for(Vertex v: vertices.values()){
            if(!cluster_map.containsKey(v)){ //for each unmarked vertex V
                if(isCore(v, eps, mu)){ //if V is a core
                    //create new cluster, assign V to it
                    cluster_curr ++;
                    cluster_map.put(v, cluster_curr);
                    LinkedList<Vertex> q = new LinkedList<>();
                    q.addAll(v.getNeighbors());

                    //add all reachable vertices to the current cluster
                    LinkedList<Vertex> q_next = new LinkedList<>();
                    while(!q.isEmpty()){
                        iters ++;
                        while(!q.isEmpty()) {
                            Vertex y = q.pop();
                            for (Vertex x : directlyReachable(y, eps, mu)) {
                                cluster_map.put(x, cluster_curr);
                                if (!cluster_map.containsKey(x)) {
                                    q_next.addLast(x);
                                }
                            }
                        }
                        q = q_next;
                        q_next = new LinkedList<>();
                    }
                }
            }
        }
        System.out.println("NIters=" + iters);
        return cluster_map;
    }

    /**
     * Gets the set of directly reachable vertices from V, given eps, mu. This will be:
     *  Nothing if V is not a core
     *  V's epsilon neighbors if V is a core
     * @param v
     * @param eps
     * @param mu
     * @return
     */
    public Collection<Vertex> directlyReachable(Vertex v, double eps, int mu){
        if(!isCore(v, eps, mu)){
            return new LinkedList<>();
        } else{
           return epsNeighbors(v, eps);
        }
    }

    /**
     * Gets epsilon neighbors of a vertex, i.e. vertices in the neighborhood of V with structural similarity over epsilon
     * @param v
     * @param eps
     * @return
     */
    public Collection<Vertex> epsNeighbors(Vertex v, double eps){
        Collection<Vertex> out = new HashSet<>();
        Collection<Vertex> neighbors = v.getNeighbors();
        for(Vertex u: neighbors){
            if(structSim(v, u) >= eps){
                out.add(u);
            }
        }
        return out;
    }

    public boolean isCore(Vertex v, double eps, int mu){
        return epsNeighborhoodSize(v, eps) >= mu;
    }

    public int epsNeighborhoodSize(String id, double eps){
        return epsNeighborhoodSize(vertices.get(id), eps);
    }

    public int epsNeighborhoodSize(Vertex v, double eps){
        int cnt = 0;
        Collection<Vertex> neighbors = v.getNeighbors();
        for(Vertex u: neighbors){
            if(structSim(v, u) >= eps){
                cnt++;
            }
        }
        return cnt;
    }


    /**
     * Computes the structural simlarity of two vertices according to the formulation in the SCAN paper.
     * @param v1
     * @param v2
     * @return
     */
    public double structSim(String v1, String v2){
        return structSim(vertices.get(v1), vertices.get(v2));
    }


        /**
         * Computes the structural simlarity of two vertices according to the formulation in the SCAN paper.
         * @param v1
         * @param v2
         * @return
         */
    public double structSim(Vertex v1, Vertex v2){
        HashSet<Vertex> n1 = v1.getNeighbors();
        HashSet<Vertex> n2 = v2.getNeighbors();

        double shared_neighbors = 0.0;
        for(Vertex v:n1){
            if(n2.contains(v)){
                shared_neighbors ++;
            }
        }
        return shared_neighbors / (Math.sqrt(n1.size() * n2.size()));
    }


    /**
     * Checks if a vertex with the given id exists in the graph
     * @param id
     * @return
     */
    public boolean containsId(String id){
        return this.vertices.containsKey(id);
    }

    /**
     * Adds a vertex to the graph, if the vertex id doesn't already exist in graph
     * @param v the vertex to add
     * @return whether the vertex was successfully added.
     */
    public boolean addVertex(Vertex v){
        if(this.vertices.containsKey(v.getId())) {
            return false;
        } else {
            this.vertices.put(v.getId(), v);
            return true;
        }
    }

    /**
     * Adds a vertex to the graph, if the vertex id doesn't already exist in graph
     * @param v the vertex to add
     * @return whether the vertex was successfully added.
     */
    public boolean addId(String v){
        if(this.vertices.containsKey(v)) {
            return false;
        } else {
            this.vertices.put(v, new Vertex(v));
            return true;
        }
    }

    /**
     * Adds a directed edge between two existing veritces in the graph
     * @param v1 the out-vertex
     * @param v2 the in-vertex
     * @return whether or not the edge was successfully added
     */
    public boolean addDirectedEdge(Vertex v1, Vertex v2){
        if(this.vertices.containsKey(v1.getId()) && this.vertices.containsKey(v2.getId())){
            v1.addEdge(v2);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a directed edge between two existing veritces in the graph
     * @param v1 the out-vertex
     * @param v2 the in-vertex
     * @return whether or not the edge was successfully added
     */
    public boolean addDirectedEdge(String v1, String v2){
        if(this.vertices.containsKey(v1) && this.vertices.containsKey(v2)){
            vertices.get(v1).addEdge(vertices.get(v2));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds an undirected edge between two existing veritces in the graph
     * @param v1 the out-vertex
     * @param v2 the in-vertex
     * @return whether or not the edge was successfully added
     */
    public boolean addUndirectedEdge(Vertex v1, Vertex v2){
        if(this.vertices.containsKey(v1.getId()) && this.vertices.containsKey(v2.getId())){
            v1.addEdge(v2);
            v2.addEdge(v1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds an undirected edge between two existing veritces in the graph
     * @param v1 the out-vertex
     * @param v2 the in-vertex
     * @return whether or not the edge was successfully added
     */
    public boolean addUndirectedEdge(String v1, String v2){
        if(this.vertices.containsKey(v1) && this.vertices.containsKey(v2)){
            vertices.get(v1).addEdge(vertices.get(v2));
            vertices.get(v2).addEdge(vertices.get(v1));
            return true;
        } else {
            return false;
        }
    }

    public String toString(){
        String buf = "";
        for(Vertex v: this.vertices.values()){
            buf += v.toString();
        }
        return buf + '\n';
    }
}
