package org.autopipes.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.UndirectedSubgraph;

public class EdgeTrimmer<V, E> {
	private final UndirectedGraph<V, E> graph;
	private UndirectedGraph<V, E> core;
	private Map<V, UndirectedGraph<V, E>> trees;
	private UndirectedGraph<V, E> rootTree;
	private V root;
	private V rootBase;

	public EdgeTrimmer(final UndirectedGraph<V, E> graph){
		this(graph, null);
	}
	public EdgeTrimmer(final UndirectedGraph<V, E> graph, final V root){
		this.graph = graph;
		this.root = root;
	}
	public UndirectedGraph<V, E> getCoreSubgraph(){
		if(core == null){
			buildCore();
		}
		return core;
	}
	public UndirectedGraph<V, E> getRootTree(){
		if(rootTree == null && root != null){
			buildTrees();
		}
		return rootTree;
	}
	public Map<V, UndirectedGraph<V, E>> getTreeMap(){
		if(trees == null){
			buildTrees();
		}
		return trees;
	}
	public V getRootBase() {
		if(root != null && rootBase == null && !getCoreSubgraph().vertexSet().isEmpty()){
			buildTrees();
		}
		return rootBase;
	}

	protected void buildTrees(){
		trees = new HashMap<V, UndirectedGraph<V, E>>();
        Set<V> forestVertices = new HashSet<V>();
        forestVertices.addAll(graph.vertexSet());
        forestVertices.removeAll(getCoreSubgraph().vertexSet());
        if(forestVertices.isEmpty()){
        	return; // no trees in graph
        }
        if(core.vertexSet().isEmpty()){
        	if(root != null){
        	    rootTree = graph;
        	}
        	return; // the graph is a tree
        }
    	UndirectedGraph<V, E> forest = new UndirectedSubgraph<V,E>(graph, forestVertices, null);
    	ConnectivityInspector<V, E> ci =
    		new ConnectivityInspector<V, E>(forest);
    	for(V v : core.vertexSet()){
    		if(graph.degreeOf(v) > core.degreeOf(v)){
    			Set<V> treeVertices = new HashSet<V>();
    			for(E edge : graph.edgesOf(v)){
    				V opposite = Graphs.getOppositeVertex(graph, edge, v);
    				if(core.vertexSet().contains(opposite)){
    					continue;
    				}
    				if(root != null && rootTree == null && ci.connectedSetOf(opposite).contains(root)){
    					Set<V> rootTreeVertices = ci.connectedSetOf(opposite);
    					rootTreeVertices.add(v);
    					rootTree = new UndirectedSubgraph<V,E>(graph, rootTreeVertices, null);
    					rootBase = v;
    				}else{
    				    treeVertices.addAll(ci.connectedSetOf(opposite));
    				}
    			}
    			if(!treeVertices.isEmpty()){
    				treeVertices.add(v);
    			    UndirectedGraph<V, E> tree = new UndirectedSubgraph<V,E>(graph, treeVertices, null);
    			    trees.put(v, tree);
    			}
    		}
    	}
	}

    protected void buildCore(){
    	core = new UndirectedSubgraph<V,E>(graph, graph.vertexSet(), null);
    	Set<V> ends = new HashSet<V>();
    	do{
    		ends.clear();
    		for(V v : core.vertexSet()){
    			if(core.degreeOf(v) == 1){
    				V nextV = v;
    				E nextE = core.edgesOf(nextV).iterator().next();
    				do{
    					ends.add(nextV);
    					nextV = Graphs.getOppositeVertex(core, nextE, nextV);
    					if(ends.contains(nextV)){
    						break; // chains from opposite sides meet
    					}
    					Set<E> nextEdges = core.edgesOf(nextV);
    					if(nextEdges.size() == 2){
    						for(E e : nextEdges){
    							if(e != nextE){
    								nextE = e;
    								break;
    							}
    						}
    					}else{
    						nextE = null;
    					}
    				}while(nextE != null);
    			}
    		}
    		core.removeAllVertices(ends);
    	}while(!ends.isEmpty());
    }
	public V getRoot() {
		return root;
	}
	public void setRoot(final V root) {
		this.root = root;
	}
}
