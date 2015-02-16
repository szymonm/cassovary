/*
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.twitter.cassovary.graph.node

import java.{util => jutil}

import com.twitter.cassovary.graph.{Node, SeqBasedNode, SortedNeighborsNodeOps}
import com.twitter.cassovary.util.{Sharded2dArray, ArraySlice}

/**
 * Nodes in the graph that store both directions and
 * whose inEdges (and only inEdges) can be mutated after initialization
 */
trait BiDirectionalNode extends Node

class FillingInEdgesBiDirectionalNode(val id: Int, val outboundNodes: Seq[Int])
  extends BiDirectionalNode {

  var inEdges: Array[Int] = BiDirectionalNode.noEdges

  override def inboundNodes(): Seq[Int] = inEdges

  /**
   * Creates array of a given size to store incoming edges.
   */
  def createInEdges(size: Int): Unit = {
    inEdges = new Array[Int](size)
  }

  /**
   * Sorts incoming edges.
   */
  def sortInNeighbors() = {
    jutil.Arrays.sort(inEdges)
  }
}

object FillingInEdgesBiDirectionalNode {
  def apply(nodeId: Int, out: Seq[Int], sortedNeighbors: Boolean): FillingInEdgesBiDirectionalNode = {
    if (sortedNeighbors) {
      new FillingInEdgesBiDirectionalNode(nodeId, out) with SortedNeighborsNodeOps
    } else {
      new FillingInEdgesBiDirectionalNode(nodeId, out)
    }
  }
}

object BiDirectionalNode {
  val noEdges = Array[Int]()

  def apply(nodeId: Int, in: Seq[Int], out: Seq[Int], sortedNeighbors: Boolean = false): BiDirectionalNode = {
    if (sortedNeighbors) {
      new SeqBasedNode(nodeId, in, out) with BiDirectionalNode with SortedNeighborsNodeOps
    } else {
      new SeqBasedNode(nodeId, in, out) with BiDirectionalNode
    }
  }
}

object SharedArrayBasedBiDirectionalNode {
  def apply(nodeId: Int, sharedOutEdgesArray: Sharded2dArray[Int],
      reverseDirEdgeArray: Sharded2dArray[Int]) = {
    new Node {
      val id = nodeId
      def outboundNodes() = sharedOutEdgesArray(nodeId)
      def inboundNodes() = reverseDirEdgeArray(nodeId)
    }
  }
}

