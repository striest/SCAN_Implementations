package com.company;
import java.util.HashSet;

/**
 * Created by striest on 11/20/18.
 */

/**
 * Class for vertices in undirected, unweighted graph
 */
public class Vertex implements Comparable<Vertex>{
    private HashSet<Vertex> neighbors;
    private String id;

    public Vertex(String id){
        this.id = id;
        this.neighbors = new HashSet<>();
        this.neighbors.add(this); //vertex is a neighbor with itself
    }

    public String getId(){
        return this.id;
    }

    public void addEdge(Vertex v){
        this.neighbors.add(v);
    }

    public void removeEdge(Vertex v){
        this.neighbors.remove(v);
    }

    public HashSet<Vertex> getNeighbors(){
        return this.neighbors;
    }

    public String toString(){
        String buf = id + ": {";
        for(Vertex v: neighbors){
            buf += v.getId() + ", ";
        }
        return buf.substring(0, buf.length() - 2) + "}\n";
    }

    @Override
    public int compareTo(Vertex u) {
        return this.getId().compareTo(u.getId());
    }
}
