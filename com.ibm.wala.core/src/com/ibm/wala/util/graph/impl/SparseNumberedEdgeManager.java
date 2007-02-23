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

import java.util.Arrays;
import java.util.Iterator;

import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.graph.NumberedEdgeManager;
import com.ibm.wala.util.graph.NumberedNodeManager;
import com.ibm.wala.util.intset.BasicNaturalRelation;
import com.ibm.wala.util.intset.BitVector;
import com.ibm.wala.util.intset.IBinaryNaturalRelation;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.IntSetAction;

/**
 * An object which tracks edges for nodes that have numbers.
 * 
 * @author sfink
 */
public final class SparseNumberedEdgeManager<T> implements NumberedEdgeManager<T> {

  private final NumberedNodeManager<T> nodeManager;

  /**
   * cache this state here for efficiency
   */
  private final BitVector hasSuccessor = new BitVector();

  /**
   * @param nodeManager
   *          an object to track nodes
   */
  public SparseNumberedEdgeManager(NumberedNodeManager<T> nodeManager) {
    this(nodeManager, 0, BasicNaturalRelation.TWO_LEVEL);
  }

  /**
   * If normalOutCount == n, this edge manager will eagerly allocated n words to
   * hold out edges for each node. (performance optimization for time)
   * 
   * @param nodeManager
   *          an object to track nodes
   * @param normalCase
   *          what is the "normal" number of out edges for a node?
   */
  public SparseNumberedEdgeManager(NumberedNodeManager<T> nodeManager, int normalCase, byte delegateImpl) {
    this.nodeManager = nodeManager;
    if (normalCase == 0) {
      successors = new BasicNaturalRelation(defaultImpl, delegateImpl);
      predecessors = new BasicNaturalRelation(defaultImpl, delegateImpl);
    } else {
      byte[] impl = new byte[normalCase];
      Arrays.fill(impl, BasicNaturalRelation.SIMPLE);
      successors = new BasicNaturalRelation(impl, delegateImpl);
      predecessors = new BasicNaturalRelation(impl, delegateImpl);
    }
  }

  /**
   * The default implementation policy conservatively uses 2-level vectors, in
   * an attempt to somewhat optimize for space.
   */
  private final static byte[] defaultImpl = new byte[] { BasicNaturalRelation.TWO_LEVEL };

  private final IBinaryNaturalRelation successors;

  private final IBinaryNaturalRelation predecessors;

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#getPredNodes(java.lang.Object)
   */
  public Iterator<T> getPredNodes(T N) throws IllegalArgumentException {
    int number = nodeManager.getNumber(N);
    if (number < 0) {
      throw new IllegalArgumentException(N + " is not in graph");
    }
    IntSet s = predecessors.getRelated(number);
    Iterator<T> empty = EmptyIterator.instance();
    return (s == null) ? empty : nodeManager.iterateNodes(s);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#getPredNodeCount(java.lang.Object)
   */
  public int getPredNodeCount(T N) throws IllegalArgumentException {
    int number = nodeManager.getNumber(N);
    if (number < 0) {
      throw new IllegalArgumentException(N + "  is not in graph");
    }
    return predecessors.getRelatedCount(number);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#getSuccNodes(java.lang.Object)
   */
  public Iterator<T> getSuccNodes(T N) throws IllegalArgumentException {
    int number = nodeManager.getNumber(N);
    if (number == -1) {
      throw new IllegalArgumentException(N + "  is not in graph");
    }
    IntSet s = successors.getRelated(number);
    Iterator<T> empty = EmptyIterator.instance();
    return (s == null) ? empty : nodeManager.iterateNodes(s);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#getSuccNodes(java.lang.Object)
   */
  public Iterator<T> getSuccNodes(int number) {
    IntSet s = successors.getRelated(number);
    Iterator<T> empty = EmptyIterator.instance();
    return (s == null) ? empty : nodeManager.iterateNodes(s);
  }

  /*
   * (non-Javadoc)
   */
  public IntSet getSuccNodeNumbers(T node) throws IllegalArgumentException {
    if (nodeManager.getNumber(node) < 0) {
      throw new IllegalArgumentException("Node not in graph " + node);
    }
    return successors.getRelated(nodeManager.getNumber(node));
  }

  /*
   * (non-Javadoc)
   */
  public IntSet getPredNodeNumbers(T node) throws IllegalArgumentException {
    if (nodeManager.getNumber(node) < 0) {
      throw new IllegalArgumentException("Node not in graph " + node);
    }
    return predecessors.getRelated(nodeManager.getNumber(node));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#getSuccNodeCount(java.lang.Object)
   */
  public int getSuccNodeCount(T N) {
    return getSuccNodeCount(nodeManager.getNumber(N));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#getSuccNodeCount(java.lang.Object)
   */
  public int getSuccNodeCount(int number) {
    return successors.getRelatedCount(number);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#addEdge(java.lang.Object,
   *      java.lang.Object)
   */
  public void addEdge(T src, T dst) throws IllegalArgumentException {
    int x = nodeManager.getNumber(src);
    int y = nodeManager.getNumber(dst);
    if (x < 0) {
      throw new IllegalArgumentException("src " + src + " is not in graph");
    }
    if (y < 0) {
      throw new IllegalArgumentException("dst " + dst + " is not in graph");
    }
    predecessors.add(y, x);
    successors.add(x, y);
    hasSuccessor.set(x);
  }

  /*
   * (non-Javadoc)
   */
  public boolean hasEdge(T src, T dst) {
    int x = nodeManager.getNumber(src);
    int y = nodeManager.getNumber(dst);
    if (x < 0 || y < 0) {
      return false;
    }
    return successors.contains(x, y);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#removeEdges(java.lang.Object)
   */
  public void removeAllIncidentEdges(T node) throws IllegalArgumentException {
    final int number = nodeManager.getNumber(node);
    if (number < 0) {
      throw new IllegalArgumentException("node not in graph: " + node);
    }
    IntSet succ = successors.getRelated(number);
    if (succ != null) {
      succ.foreach(new IntSetAction() {
        public void act(int x) {
          predecessors.remove(x, number);
        }
      });
    }
    IntSet pred = predecessors.getRelated(number);
    if (pred != null) {
      pred.foreach(new IntSetAction() {
        public void act(int x) {
          successors.remove(x, number);
          if (successors.getRelatedCount(x) == 0) {
            hasSuccessor.clear(x);
          }
        }
      });
    }
    successors.removeAll(number);
    hasSuccessor.clear(number);
    predecessors.removeAll(number);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#removeEdges(java.lang.Object)
   */
  public void removeIncomingEdges(T node) throws IllegalArgumentException {
    final int number = nodeManager.getNumber(node);
    if (number < 0) {
      throw new IllegalArgumentException("node not in graph: " + node);
    }
    IntSet pred = predecessors.getRelated(number);
    if (pred != null) {
      pred.foreach(new IntSetAction() {
        public void act(int x) {
          successors.remove(x, number);
          if (successors.getRelatedCount(x) == 0) {
            hasSuccessor.clear(x);
          }
        }
      });
    }
    predecessors.removeAll(number);
  }

  public void removeEdge(T src, T dst) throws IllegalArgumentException {
    final int srcNumber = nodeManager.getNumber(src);
    final int dstNumber = nodeManager.getNumber(dst);
    if (srcNumber < 0) {
      throw new IllegalArgumentException("src not in graph: " + src);
    }
    if (dstNumber < 0) {
      throw new IllegalArgumentException("dst not in graph: " + dst);
    }
    successors.remove(srcNumber, dstNumber);
    if (successors.getRelatedCount(srcNumber) == 0) {
      hasSuccessor.clear(srcNumber);
    }
    predecessors.remove(dstNumber, srcNumber);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.util.graph.EdgeManager#removeEdges(java.lang.Object)
   */
  public void removeOutgoingEdges(T node) throws IllegalArgumentException {
    final int number = nodeManager.getNumber(node);
    if (number < 0) {
      throw new IllegalArgumentException("node not in graph: " + node);
    }
    IntSet succ = successors.getRelated(number);
    if (succ != null) {
      succ.foreach(new IntSetAction() {
        public void act(int x) {
          predecessors.remove(x, number);
        }
      });
    }
    successors.removeAll(number);
    hasSuccessor.clear(number);
  }

  /**
   * This is implemented as a shortcut for efficiency
   * 
   * @param node
   * @return true iff that node has any successors
   */
  public boolean hasAnySuccessor(int node) {
    return hasSuccessor.get(node);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "Successors relation:\n" + successors;
  }

}
