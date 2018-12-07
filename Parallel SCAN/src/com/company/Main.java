package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Graph g = buildGraph(args[0], Boolean.parseBoolean(args[1]));
        double eps = Double.parseDouble(args[2]);
        int mu = Integer.parseInt(args[3]);

        long t_curr = System.nanoTime();
        HashMap<Vertex, Integer> clustering = g.SCANCluster(eps, mu);
        double elapsedtime = (System.nanoTime() - t_curr )/1000000000.0;


        for(Vertex v: clustering.keySet()){
            System.out.println(v.getId() + ":" + clustering.get(v));
        }

        System.out.println("Total Time:" + elapsedtime + "s");

        long t_curr_2 = System.nanoTime();
        HashMap<Vertex, Integer> clustering2 = g.PSCANCluster(eps, mu);
        double elapsedtime_2 = (System.nanoTime() - t_curr )/1000000000.0;

        for(Vertex v: clustering2.keySet()){
            if(clustering2.get(v) != null){
                System.out.println(v.getId() + ":" + clustering2.get(v));
            }
        }

        System.out.println("Total Time:" + elapsedtime_2 + "s");
    }

    public static Graph buildGraph(String fp, boolean digraph){
        Graph out = new Graph();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fp));
            String s;
            while((s = reader.readLine()) != null){
                String[] tokens = s.split(" ");
                if(tokens.length < 2){
                    continue;
                }
                out.addId(tokens[0]);
                out.addId(tokens[1]);
                if(digraph){
                    out.addDirectedEdge(tokens[0], tokens[1]);
                } else {
                    out.addUndirectedEdge(tokens[0], tokens[1]);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return out;
    }

    public static String readInput(String fp){
        String out = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fp));
            String s;
            while((s = reader.readLine()) != null){
                out += s + '\n';
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return out;
    }

}
