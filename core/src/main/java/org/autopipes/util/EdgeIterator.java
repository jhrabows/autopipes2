package org.autopipes.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.UndirectedSubgraph;

/**
 * <h2>Problem</h2>
 * Given a connected graph <i>g</i> and a vertex <i>R</i> of index 1,
 * construct an iterator EI(g, R) on the edge set.
 * <h2>Algorithm</h2>
 * <h3>Step1: Tree decomposition</h3>
 * We are going to find vertex sets C, RT, T(0), ..., T(n) such that
 * <ul>
 * <li>g is a union of C, RT and the T(i)'s.
 * <li>The graphs induced by RT and T(i) are trees</li>
 * <li>The graph induced by C is connected and has no vertices of index 1</li>
 * <li>The sets T(i) are disjoint and each has one vertex in common with C (T(i)'s base).</li>
 * <li>RT contains R. If C is not empty then RT has one vertex RB in common with C (RT's base)</li>
 * <li>If RB exists it is of degree 1 in RT and RT\{RB} is disjoint from any T(i)</li>
 * </ul>
 * We start with a standard tree iteration over RT starting at R and ending in RB (if present).
 * If C is empty we are done. Otherwise we continue with next step.
 * <h3>Step2: Recursion</h3>
 * Let C(0), ..., C(m) be the connected components of
 * the graph obtained by removing RB from C (together with its adjacent vertices).
 * For each component C(i) we are going to create a new graph g(i) described further down.
 * The resulting iterator will be an iteration over the iterators EI(g(i), RB).
 * If at any time during the iteration of g(i) we encounter a base vertex of a tree T(i)
 * from Step 1 then T(i) iteration will be performed and when it finishes, the main iteration will resume.
 * T(i) is traversed using standard tree iteration starting at the base.
 * <h4>Definition of the graph g(i)</h4>
 * Let E(i, 0), ..., E(i, k(i)) be the edges which join RB with the C(i) components.
 * The vertex set of g(i) will consist of the vertices of C(i) together with RB and
 * (in case k(i) > 1) a new helper vertices H(i, 1), ..., H(i, k(i)). The edges will be the edges of C(i)
 * except that for j > 0, E(i,j) is re-wired too join to H(i, j) instead of RB.
 * Note that RB and each H(i,j) are of index 1 in g(i) - in particular the iterators
 * EI(g(i), RB) are defined.
 *
 * @author Tata
 *
 */
public class EdgeIterator<V, E> {
	private static Logger logger = Logger.getLogger(EdgeIterator.class);

	private final UndirectedGraph<V, E> graph;
	private final Class<? extends V> vertexClass;
	private final V raiser;
	private List<E> edges;
	private Set<E> reversed;
	private Map<V, List<UndirectedGraph<V, E>>> treeMap;

	public EdgeIterator(final UndirectedGraph<V, E> graph, final V raiser, final Class<? extends V> vertexClass){
		this.graph = graph;
		this.vertexClass = vertexClass;
		this.raiser = raiser;
	}

	/**
	 * When called first time, builds an ordered list of edges
	 * and creates a subset of edges which are incompatible with this ordering.
	 * @return the ordered list of edges
	 */
	public List<E> getEdges(){
		if(edges == null){
			edges = new ArrayList<E>();
			reversed = new HashSet<E>();
			treeMap = new HashMap<V, List<UndirectedGraph<V, E>>>();
			try{
				addEdges(graph, raiser);
				if(edges.size() != graph.edgeSet().size()){
					logger.error("Iteration count failure. Iterated "
							+ edges.size() + " edges out of " + graph.edgeSet().size());
				}
			}catch(Exception t){
				logger.error("Iteration exception failure", t);
			}
		}
		return edges;
	}
	
	/**
	 * Returns a list of edges which are incompatible with the iteration.
	 * (Assuming that the iteration was performed by a getEdges() call).
	 * @return the incompatible list or <code>null</code> before the iteration
	 */
	public Set<E> getReversed() {
		return reversed;
	}
	
/**
 * (Not used?)
 * Change the way edges are ordered in the iterated graph so that
 * when a predecessor edge is adjacent to the next edge then the
 * common point is the predecessor's target vertex and the successor's
 * source vertex. This operation does not alter the edge objects
 * even when edge properties were used to define the original graph ordering.
 * Subsequent calls to this method are no-ops.
 */
	public void orderGraph(){
		getEdges(); // make sure things are initialized
		for(E r : reversed){
			V source = graph.getEdgeSource(r);
			V target = graph.getEdgeTarget(r);
			graph.removeEdge(r);
			graph.addEdge(target, source, r);
		}
		reversed.clear();
	}

	private void addEdges(final UndirectedGraph<V, E> g, final V raiser)
	        throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException{
		EdgeTrimmer<V, E> et = new EdgeTrimmer<V, E>(g, raiser);
		// merge tree map (it may be used in the last leg of raiser iteration
		for(V v : et.getTreeMap().keySet()){
			List<UndirectedGraph<V, E>> treeList = treeMap.get(v);
			if(treeList == null){
				treeList = new ArrayList<UndirectedGraph<V, E>>();
				treeMap.put(v, treeList);
			}
		    treeList.add(et.getTreeMap().get(v));
		}
		// process raiser tree
		V rootBase = et.getRootBase();
		UndirectedGraph<V, E> rootTree = et.getRootTree();
		addTreeEdges(rootTree, raiser, rootBase);
		if(rootBase == null){
			return;
		}
		// recursive step
		UndirectedGraph<V, E> core = et.getCoreSubgraph();
		UndirectedGraph<V, E> subcore = new UndirectedSubgraph<V, E>(core, core.vertexSet(), null);
		subcore.removeVertex(rootBase);
		ConnectivityInspector<V, E> ci = new ConnectivityInspector<V, E>(subcore);
		for(Set<V> compSet : ci.connectedSets()){
			UndirectedGraph<V, E> compGraph;
			if(ci.connectedSets().size() == 1){
				// special case when the general construction amounts to a no-op
				compGraph = core;
			}else{
				compGraph = new UndirectedSubgraph<V, E>(core, compSet, null);
				compGraph.addVertex(rootBase);
				for(E e : core.edgesOf(rootBase)){
					V opposite = Graphs.getOppositeVertex(core, e, rootBase);
					if(compSet.contains(opposite)){
						compGraph.addEdge(rootBase, opposite, e);
					}
				}
			}
			UndirectedGraph<V, E> altGraph = alter(compGraph, rootBase);
			addEdges(altGraph, rootBase);
		}

	}
	/**
	 * Creates a new graph with the same edges, containing the same vertices as the passed graph g,
	 * and where the degree of the root is 1. If g is connected then so is the new graph.
	 * @param g the source graph
	 * @param root the root vertex
	 * @return the new graph
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 */
	private  UndirectedGraph<V, E> alter(final UndirectedGraph<V, E> g, final V root)
	    throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException{
        if(g.degreeOf(root) == 1){
        	return g;
        }
		UndirectedGraph<V, E> ret = new SimpleGraph<V, E>(g.getEdgeFactory());
		Graphs.addGraph(ret, g);
		boolean first = true;
		for(E e : g.edgesOf(root)){
			if(first){
				first = false;
			}else{
				V opposite = Graphs.getOppositeVertex(g, e, root);
				ret.removeEdge(e);
				Constructor<? extends V> constructor = vertexClass.getConstructor();
				V v = constructor.newInstance();
				ret.addVertex(v);
				ret.addEdge(opposite, v, e);
			}
		}
		return ret;
	}
	private void injectFromMap(final V v){
		List<UndirectedGraph<V, E>> treeList = treeMap.get(v);
		if(treeList != null){
			for(UndirectedGraph<V, E> tree : treeList){
			addTreeEdges(tree, v, null);
			}
		}
	}
	/**
	 * Function which determines an order of tree traversal.
	 * @param tree one to be traversed
	 * @param start that of the iteration
	 * @param end A tree vertex. If not null then all other tree vertices
	 * (except its children - if any) must be visited first.
	 * @param source Edge element (not part of the tree) which connects the start vertex to its iteration parent.
	 * Null in the initial call. May be used in an angle-based ordering.
	 * @return
	 */
	protected Map<E, Set<V>> startEdges(final UndirectedGraph<V, E> tree, final V start, final V end){
		Map<E, Set<V>> ret = new LinkedHashMap<E, Set<V>>();
		SortedMap<Integer, List<E>> sizeMap = new TreeMap<Integer, List<E>>();
    	UndirectedGraph<V, E> locator = new UndirectedSubgraph<V,E>(tree, tree.vertexSet(), null);
        locator.removeVertex(start);
    	ConnectivityInspector<V, E> ci = new ConnectivityInspector<V, E>(locator);
    	for(E e : tree.edgesOf(start)){
    		V opposite = Graphs.getOppositeVertex(tree, e, start);
    		Set<V> subset = ci.connectedSetOf(opposite);
    		int size = (end != null && subset.contains(end)) ? Integer.MAX_VALUE : subset.size();
    		List<E> sizeList = sizeMap.get(size);
    		if(sizeList == null){
    			sizeList = new ArrayList<E>();
    			sizeMap.put(size, sizeList);
    		}
    		sizeList.add(e);
    	}
    	for(List<E> sizeList : sizeMap.values()){
    		for(E e : sizeList){
        		V opposite = Graphs.getOppositeVertex(tree, e, start);
    			ret.put(e, ci.connectedSetOf(opposite));
    		}
    	}
		return ret;
	}
	/**
	 * Recursive tree traversal
	 * @param tree traversed tree
	 * @param start of traversal
	 * @param end if not null and of degree 1 then the end of traversal
	 */
    private void addTreeEdges(final UndirectedGraph<V, E> tree, final V start, final V end){
    	if(tree.vertexSet().size() == 1){
    		return; // reached the end
    	}
    	Map<E, Set<V>> se = startEdges(tree, start, end);
        for(E e : se.keySet()){
            edges.add(e);
            if(graph.getEdgeSource(e) != start){
            	reversed.add(e);
            }
    		V opposite = Graphs.getOppositeVertex(tree, e, start);
			UndirectedGraph<V, E> subtree = new UndirectedSubgraph<V,E>(tree, se.get(e), null);
            injectFromMap(opposite);
            addTreeEdges(subtree, opposite,
            		(end != null && subtree.vertexSet().contains(end)) ? end : null);
        }
    }
    //private void checkForReversed(final E e, final V origin){
    //	V source = graph.getEdgeSource(e);
    //}

}
