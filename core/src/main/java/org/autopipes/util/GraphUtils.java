package org.autopipes.util;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;


public class GraphUtils {

	public static <V, E> boolean tripple(final Graph<V, E> g, final E e1, final E e2, final V[] tripple){
        boolean ret = true;
		if(g.getEdgeSource(e1) == g.getEdgeSource(e2)){
			tripple[0]=(g.getEdgeTarget(e1));
			tripple[1]=(g.getEdgeSource(e1));
			tripple[2]=(g.getEdgeTarget(e2));
		}else if(g.getEdgeTarget(e1) == g.getEdgeTarget(e2)){
			tripple[0]=(g.getEdgeSource(e1));
			tripple[1]=(g.getEdgeTarget(e1));
			tripple[2]=(g.getEdgeSource(e2));
		}else if(g.getEdgeSource(e1) == g.getEdgeTarget(e2)){
			tripple[0]=(g.getEdgeTarget(e1));
			tripple[1]=(g.getEdgeSource(e1));
			tripple[2]=(g.getEdgeSource(e2));
		}else if(g.getEdgeTarget(e1) == g.getEdgeSource(e2)){
			tripple[0]=(g.getEdgeSource(e1));
			tripple[1]=(g.getEdgeTarget(e1));
			tripple[2]=(g.getEdgeTarget(e2));
		}else{
			ret = false;
		}
		return ret;
	}
	
	public static<V, E> void clearGraph(Graph<V, E> g){
		while(!g.vertexSet().isEmpty()){
			V v = g.vertexSet().iterator().next();
		    g.removeVertex(v);
		}
	}

	private static class GraphFactory<V,E> implements EdgeFactory<V,Graph<V, E>>{
        private final Class edgeClass;
		public GraphFactory(final Class edgeClass){
			this.edgeClass = edgeClass;
		}
//		@Override
		public Graph<V, E> createEdge(final V arg0, final V arg1) {
			return new SimpleGraph<V,E>(edgeClass);
		}
	}

	public static <V, E> Graph<V, Graph<V, E>> branchGraph(final Graph<V, E> g, final Class edgeClass){
		Graph<V, Graph<V, E>> ret = new SimpleGraph<V, Graph<V, E>>(new GraphFactory<V,E>(edgeClass));

		return ret;
	}
/**
 * Computes the line graph of a graph.
 * The line graph L(g) of an undirected graph g is a graph such that
 <ul>
  <li>each vertex of L(g) represents an edge of g</li>
  <li>any two vertices of L(g) are adjacent if and only if
      their corresponding edges share a common endpoint in g</li>
 </ul>
 * @param <V> vertex data type of g
 * @param <E> edge data type of g
 * @param g the source graph
 * @return the line graph L(g)
 */
	public static <V, E> UndirectedGraph<E, DefaultEdge> lineGraph(final Graph<V, E> g){
		UndirectedGraph<E, DefaultEdge> ret = new SimpleGraph<E, DefaultEdge>(DefaultEdge.class);
        for(E e : g.edgeSet()){
        	ret.addVertex(e);
        }
        List<E> edgeStar = new ArrayList<E>();
        for(V v : g.vertexSet()){
        	edgeStar.clear();
        	edgeStar.addAll(g.edgesOf(v));
            for(int i = 0; i < edgeStar.size(); i++){
            	for(int j = i + 1; j < edgeStar.size(); j++){
            		ret.addEdge(edgeStar.get(i), edgeStar.get(j));
            	}
            }
        }
		return ret;
	}
}
