/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.util.graph.impl;

import java.util.Iterator;

import com.ibm.wala.util.graph.AbstractNumberedGraph;
import com.ibm.wala.util.graph.EdgeManager;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.NodeManager;
import com.ibm.wala.util.intset.BasicNaturalRelation;

/**
 * 
 * A graph of numbered nodes, expected to have a fairly sparse edge structure.
 * 
 * @author sfink
 */
public class SlowSparseNumberedGraph<T> extends AbstractNumberedGraph<T> {

  private final SlowNumberedNodeManager<T> nodeManager = new SlowNumberedNodeManager<T>();

  private final SparseNumberedEdgeManager<T> edgeManager;

  public SlowSparseNumberedGraph() {
    this(0);
  }

  /**
   * 
   * If normalOutCount == n, this edge manager will eagerly allocated n words to
   * hold out edges for each node. (performance optimization for time)
   * 
   * @param normalOutCount
   *          what is the "normal" number of out edges for a node?
   */
  public SlowSparseNumberedGraph(int normalOutCount) {
    edgeManager = new SparseNumberedEdgeManager<T>(nodeManager, normalOutCount, BasicNaturalRelation.TWO_LEVEL);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.AbstractGraph#getNodeManager()
   */
  public NodeManager<T> getNodeManager() {
    return nodeManager;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.AbstractGraph#getEdgeManager()
   */
  public EdgeManager<T> getEdgeManager() {
    return edgeManager;
  }

  /**
   * @param g
   * @return a graph with the same nodes and edges as g
   */
  public static <T> SlowSparseNumberedGraph<T> duplicate(Graph<T> g) {
    SlowSparseNumberedGraph<T> result = new SlowSparseNumberedGraph<T>();
    for (Iterator<? extends T> it = g.iterateNodes(); it.hasNext();) {
      result.addNode(it.next());
    }
    for (Iterator<? extends T> it = g.iterateNodes(); it.hasNext();) {
      T n = it.next();
      for (Iterator<? extends T> it2 = g.getSuccNodes(n); it2.hasNext();) {
        result.addEdge(n, it2.next());
      }
    }
    return result;
  }
}