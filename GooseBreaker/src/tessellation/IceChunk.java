package tessellation;

import java.util.ArrayList;

import org.jbox2d.common.Vec2;

import util.RandomUtil;

/**
 *         An IceChunk represents a single, indivisible chunk of ice which may
 *         be part of a larger, connected, rigid sheet of ice.
 * 
 *         Each chunk is triangle shaped, and may be attached to other adjacent
 *         ice chunks along each edge. Together, the connected ice chunks form a
 *         connected graph; each ice chunk may be regarded as a graph node.
 * 
 *         The rigid bodies of ice can be determined from a collection of ice
 *         chunks by querying the graph containing all the ice chunks.
 * 
 *         Each chunk triangle is to be clockwise-oriented.
 * @author Jack
 */
public class IceChunk {
  private Triangle shape;
  private boolean isDeleted;
  // One Collection of adjacent IceChunk's for each triangle edge; three total
  private ArrayList<ArrayList<IceChunk>> adjacentByEdgeSorted;
  private ArrayList<ArrayList<IceChunk>> adjDeletedByEdge;
  private ArrayList<ArrayList<IceChunk>> adjNonDeletedByEdge;


  public IceChunk(Triangle tri) {
    shape = tri.getClockwise();
    adjacentByEdgeSorted = new ArrayList<ArrayList<IceChunk>>(3);
    adjDeletedByEdge = new ArrayList<ArrayList<IceChunk>>(3);
    adjNonDeletedByEdge = new ArrayList<ArrayList<IceChunk>>(3);
    for (int i = 0; i < 3; ++i) {
      adjacentByEdgeSorted.add(new ArrayList<IceChunk>());
      adjDeletedByEdge.add(new ArrayList<IceChunk>());
      adjNonDeletedByEdge.add(new ArrayList<IceChunk>());
    }
    isDeleted = false;
  }


  public Triangle getTriangle() {
    return shape;
  }


  public boolean isDeleted() {
    return isDeleted;
  }


  public void setDeleted() {
    if (!isDeleted) {
      for (int i = 0; i < 3; ++i) {
        for (IceChunk adjChunk : this.adjacentByEdgeSorted.get(i)) {
          int adjEdgeInd = adjChunk.getIndexOfEdgeAdjacent(this);
          adjChunk.adjNonDeletedByEdge.get(adjEdgeInd).remove(this);
          adjChunk.adjDeletedByEdge.get(adjEdgeInd).add(this);
        }
      }
    }
    isDeleted = true;
    
  }

  public boolean isEdgeBare(int edgeInd) {
    return this.adjNonDeletedByEdge.get(edgeInd).isEmpty()
        || !this.adjDeletedByEdge.get(edgeInd).isEmpty();
  }
  
  public boolean hasBareEdge() {
    for (int i = 0; i < 3; ++i) {
      if (isEdgeBare(i)) return true;
    }
    return false;
  }
  
  
  public boolean hasAdjacentOnEdge(int edgeInd) {
    return !adjNonDeletedByEdge.get(edgeInd).isEmpty();
  }


  /**
   * Get the IceChunks adjacent on a certain edge. The edge index corresponds to
   * the edge index for the underlying Triangle.
   * 
   * @param edgeInd
   *          Either 0, 1, or 2, corresponding to an edge in the chunk's
   *          triangle.
   * @return All IceChunks adjacent along the edge.
   */
  public ArrayList<IceChunk> getAdjacentOnEdge(int edgeInd) {
    return new ArrayList<IceChunk>(adjNonDeletedByEdge.get(edgeInd));
  }


  /**
   * Get IceChunks adjacent on a certain edge that have been marked deleted.
   * 
   * @param edgeInd
   * @return
   */
  public ArrayList<IceChunk> getDeletedOnEdge(int edgeInd) {
    return new ArrayList<IceChunk>(adjDeletedByEdge.get(edgeInd));
  }


  /**
   * Get all the adjacent IceChunks, deleted and non-deleted.
   * 
   * @param edgeInd
   * @return
   */
  public ArrayList<IceChunk> getAdjacentAndDeletedOnEdge(int edgeInd) {
    return new ArrayList<IceChunk>(adjacentByEdgeSorted.get(edgeInd));
  }


  /**
   * Given an IceChunk, get the index of the edge along which it is adjacent. If
   * the chunk is not adjacent, return -1.
   * 
   * @param other
   *          Another IceChunk which may or may not be adjacent.
   * @return Either 0, 1, or 2 depending on the adjacent edge, or -1 if not
   *         adjacent.
   */
  public int getIndexOfEdgeAdjacent(IceChunk other) {
    for (int i = 0; i < 3; ++i) {
      if (adjacentByEdgeSorted.get(i).contains(other)) {
        return i;
      }
    }
    return -1;
  }
  
  
  private float getEdgeAxisProjection(TriangleEdge edge, TriangleEdge onto) {
    float vx = edge.getSecond().x - onto.getFirst().x;
    float vy = edge.getSecond().y - onto.getFirst().y;
    return vx * onto.getDirection().x + vy * onto.getDirection().y;
  }
  
  
  private void addEdgeClockwiseSorted(int edgeInd, IceChunk otherChunk, int otherEdgeInd) {
    TriangleEdge chunkEdge = this.getTriangle().getOrderedEdge(edgeInd);
    TriangleEdge otherEdge = otherChunk.getTriangle().getOrderedEdge(otherEdgeInd);
    float insProj = this.getEdgeAxisProjection(otherEdge, chunkEdge);
    
    ArrayList<IceChunk> adj = this.adjacentByEdgeSorted.get(edgeInd);
    for (int i = 0; i < adj.size(); ++i) {
      IceChunk adjChunk = adj.get(i);
      TriangleEdge adjChunkEdge = adjChunk.getTriangle().getOrderedEdge(adjChunk.getIndexOfEdgeAdjacent(this));
      float adjProj = this.getEdgeAxisProjection(adjChunkEdge, chunkEdge);
      if (insProj < adjProj) {
        adj.add(i, otherChunk);
        return;
      }
    }
    adj.add(adj.size(), otherChunk);
  }


  /**
   * Add an adjacent IceChunk along an edge in this chunk's Triangle; since the
   * chunk graph is implicitly undirected, also add this chunk to the other
   * chunk's list of adjacent chunks.
   * 
   * @param edgeInd
   *          The edge index of this.getTriangle() along which the chunk is
   *          adjacent
   * @param other
   *          The other adjacent triangle
   * @param otherEdgeInd
   *          The edge index of other.getTriangle() along which this chunk is
   *          adjacent.
   */
  public void addAdjacentOnEdge(int edgeInd, IceChunk other, int otherEdgeInd) {
    this.addEdgeClockwiseSorted(edgeInd, other, otherEdgeInd);
    if (other.isDeleted) {
      this.adjDeletedByEdge.get(edgeInd).add(other);
    } else {
      this.adjNonDeletedByEdge.get(edgeInd).add(other);
    }
    
    other.addEdgeClockwiseSorted(otherEdgeInd, this, edgeInd);
    if (this.isDeleted) {
      other.adjDeletedByEdge.get(otherEdgeInd).add(this);
    } else {
      other.adjNonDeletedByEdge.get(otherEdgeInd).add(this);
    }
  }


  /**
   * Remove a chunk from this chunk's list of adjacent chunks, and vice-versa.
   * If the chunk is not actually adjacent, do nothing.
   * 
   * @param other
   *          The other IceChunk which will no longer be adjacent.
   */
  public void removeAdjacentOnEdge(IceChunk other) {
    for (ArrayList<IceChunk> cs : adjacentByEdgeSorted) {
      cs.remove(other);
    }
    if (other.isDeleted) {
      for (ArrayList<IceChunk> cs : adjDeletedByEdge) {
        cs.remove(other);
      }
    } else {
      for (ArrayList<IceChunk> cs : adjNonDeletedByEdge) {
        cs.remove(other);
      }
    }
    
    for (ArrayList<IceChunk> cs : other.adjacentByEdgeSorted) {
      cs.remove(this);
    }
    if (this.isDeleted) {
      for (ArrayList<IceChunk> cs : other.adjDeletedByEdge) {
        cs.remove(this);
      }
    } else {
      for (ArrayList<IceChunk> cs : other.adjNonDeletedByEdge) {
        cs.remove(this);
      }
    }
  }


  /**
   * Disconnect this chunk from the chunk adjacency graph. Clear the adjacency
   * list for this chunk, and remove adjacencies from the previously-adjacent
   * chunks.
   */
  public void removeAdjacentOnAllEdges() {
    ArrayList<IceChunk> allAdjChunks = new ArrayList<IceChunk>();
    for (ArrayList<IceChunk> adj : this.adjacentByEdgeSorted) {
      allAdjChunks.addAll(adj);
    }
    for (IceChunk chunk : allAdjChunks) {
      for (ArrayList<IceChunk> cs : chunk.adjacentByEdgeSorted) {
        cs.remove(this);
      }
      if (this.isDeleted) {
        for (ArrayList<IceChunk> cs : chunk.adjDeletedByEdge) {
          cs.remove(this);
        }
      } else {
        for (ArrayList<IceChunk> cs : chunk.adjNonDeletedByEdge) {
          cs.remove(this);
        }
      }
    }
    
    for (ArrayList<IceChunk> adj : this.adjacentByEdgeSorted) {
      adj.clear();
    }
    for (ArrayList<IceChunk> adj : this.adjDeletedByEdge) {
      adj.clear();
    }
    for (ArrayList<IceChunk> adj : this.adjNonDeletedByEdge) {
      adj.clear();
    }
  }


  /**
   * Get all adjacent chunks not marked deleted.
   * 
   * @return a Collection of adjacent IceChunk's.
   */
  public ArrayList<IceChunk> getAdjacentIceChunks() {
    ArrayList<IceChunk> result = new ArrayList<IceChunk>();
    for (ArrayList<IceChunk> c : adjNonDeletedByEdge) {
      result.addAll(c);
    }
    return result;
  }


  /**
   * Get all adjacent chunks marked deleted.
   * 
   * @return
   */
  public ArrayList<IceChunk> getAdjacentDeletedIceChunks() {
    ArrayList<IceChunk> result = new ArrayList<IceChunk>();
    for (ArrayList<IceChunk> c : adjDeletedByEdge) {
      result.addAll(c);
    }
    return result;
  }


  /**
   * Get the adjacent chunks along an edge, deleted and non-deleted, and sort
   * them in clockwise-order. Use selection sort in O(n^2).
   * 
   * @param edge
   * @return
   */
  public ArrayList<IceChunk> getAdjacentAndDeletedSortedClockwise(int edge) {
    return this.adjacentByEdgeSorted.get(edge);
  }
  
  
  /**
   * As an alternative to pre-splitting a chunk all the way, only split
   * triangles as necessary in order to resolve a triangle containing the given
   * point.
   * @param xf
   * @param pos
   * @param maxArea
   * @return The triangle containing the point after recursive resolution, or
   * null if this triangle doesn't contain the point. Resolution involves
   * disconnecting this chunk from the graph and adding new chunks as necessary.
   */
  public IceChunk resolveRecursively(Vec2 pos, float maxArea) {
    if (this.getTriangle().getArea() < maxArea) {
      return this;
    }
    
    ArrayList<IceChunk> splits = new ArrayList<IceChunk>(this.randomBinaryDivision());
    IceChunk chunk1 = splits.get(0);
    IceChunk chunk2 = splits.get(1);
    int splitEdgeInd = chunk1.getIndexOfEdgeAdjacent(chunk2);
    TriangleEdge splitEdge = chunk1.getTriangle().getOrderedEdge(splitEdgeInd);
    Vec2 edgeDir = splitEdge.getDirection();
    Vec2 edgeDirPerp = new Vec2(edgeDir.y, -edgeDir.x);
    float testX = pos.x - splitEdge.getFirst().x;
    float testY = pos.y - splitEdge.getFirst().y;
    if (testX * edgeDirPerp.x + testY * edgeDirPerp.y > 0) {
      return chunk1.resolveRecursively(pos, maxArea);
    } else {
      return chunk2.resolveRecursively(pos, maxArea);
    }
  }


  /**
   * Divide this chunk into two chunks according to the following process:
   *   - Pick an edge at random from the chunk Triangle
   *   - Pick a random number between 0 and 1; this number is used to determine where along the
   *     chosen edge a new vertex will be placed.
   *   - The two new triangles are formed by splitting this triangle through the
   *     segment formed by connecting the split point on the split edge and the 
   *     vertex from the original triangle opposite the split edge.
   *   - The two new triangles are adjacent along the newly created segment; the
   *     old triangle is disconnected from the chunk graph, and all triangles
   *     that were adjacent to the old triangle are now adjacent to at least one
   *     of the new triangles depending on the adjacent edge and the placement
   *     along the edge.
   *   - Both of the new triangles have one edge which is the same as an edge
   *     from the original triangles; adjacencies along this edge are simply
   *     transfered to the new triangles.
   *   - The adjacencies along the split edge are transferred to one of the new
   *     triangles, or both, depending on the adjacent triangle's edge placement
   *     in relation to the split point.
   * 
   * Randomly dividing a triangle will properly update adjacencies between the
   * newly created chunks and the deleted chunks. The original chunk (this) will
   * be disconnected from the chunk graph, but will not be marked deleted.
   * 
   * @return A Collection of exactly two new, non-overlapping IceChunk's which
   *         replace this IceChunk in the adjacency graph.
   */
  public ArrayList<IceChunk> randomBinaryDivision() {
    // Pick a random edge weighted by the opposite angle -- larger angles
    // are more likely. This helps prevent making lots of very thin
    // triangles.
    TriangleEdge splitEdge = this.getTriangle().randomEdgeWeightedByAngle();
    // The split point is chosen using a uniform sum distribution, so that
    // splits near the center are more likely. This also prevents skinny
    // triangles.
    float splitFrac = (float) RandomUtil.UniformSum(8, 0, 1);
    
    return binaryDivision(splitEdge, splitFrac);
  }
  
  /**
   * Divide this chunk into two non-overlapping chunks by splitting through the
   * split point on the provided split edge, and update the chunk adjacency
   * graph.
   * See randomBinaryDivision().
   * @param splitEdge
   * @param splitFrac
   * @return
   */
  public ArrayList<IceChunk> binaryDivision(TriangleEdge splitEdge, float splitFrac) {
    Vec2 splitPt = splitEdge.getDirection().mul(splitFrac).add(splitEdge.getFirst());
    Vec2 pivot = splitEdge.getOpposite();

    IceChunk newChunk1 = new IceChunk(new Triangle(pivot, splitEdge.getFirst(),
        splitPt));
    IceChunk newChunk2 = new IceChunk(new Triangle(pivot, splitPt,
        splitEdge.getSecond()));
    newChunk1.addAdjacentOnEdge(2, newChunk2, 0);

    int splitEdgeInd = splitEdge.getEdgeIndex();
    int unsplitEdgeInd1 = (splitEdgeInd + 2) % 3;
    int unsplitEdgeInd2 = (splitEdgeInd + 1) % 3;
    // The first two edges are trivial: just transfer adjacency from
    // the edges of the old triangle to the same edge in the new triangle.
    for (IceChunk chunk : this.adjacentByEdgeSorted.get(unsplitEdgeInd1)) {
      int otherEdgeInd = chunk.getIndexOfEdgeAdjacent(this);
      newChunk1.addAdjacentOnEdge(0, chunk, otherEdgeInd);
    }
    for (IceChunk chunk : this.adjacentByEdgeSorted.get(unsplitEdgeInd2)) {
      int otherEdgeInd = chunk.getIndexOfEdgeAdjacent(this);
      newChunk2.addAdjacentOnEdge(2, chunk, otherEdgeInd);
    }

    // The third edge is nontrivial: depending on the location of the
    // endpoints of the neighbor triangle, it may be adjacent to either one
    // of the new triangles, or both.
    // There are three cases:
    // For endpoints 'o' and split point 'x', and split edge endpoints
    // (1) and (2):
    // Case 1: (1)--o--o--x--(2) the neighbor is adj to new triangle 1
    // Case 2: (1)--x--o--o--(2) the neighbor is adj to new triangle 2
    // Case 3: (1)--o--x--o--(2) the neighbor is adj to new triangle 1,2
    //
    // dir is the direction from from split edge endpoint (1) to (2)
    // a point p "to the right of" the split point x means
    // (p - x) dot dir > 0
    // a point p "to the left of" the split point x means
    // (p - x) dot dir < 0
    Vec2 dir = splitEdge.getSecond().sub(splitPt);
    for (IceChunk chunk : this.adjacentByEdgeSorted.get(splitEdgeInd)) {
      int otherEdgeInd = chunk.getIndexOfEdgeAdjacent(this);
      TriangleEdge otherEdge = chunk.getTriangle().getOrderedEdge(otherEdgeInd);
      Vec2 p1 = otherEdge.getFirst().sub(splitPt);
      Vec2 p2 = otherEdge.getSecond().sub(splitPt);
      if (Vec2.dot(p1, dir) > 0 && Vec2.dot(p2, dir) > 0) {
        newChunk2.addAdjacentOnEdge(1, chunk, otherEdgeInd);
      } else if (Vec2.dot(p1, dir) < 0 && Vec2.dot(p2, dir) < 0) {
        newChunk1.addAdjacentOnEdge(1, chunk, otherEdgeInd);
      } else {
        newChunk1.addAdjacentOnEdge(1, chunk, otherEdgeInd);
        newChunk2.addAdjacentOnEdge(1, chunk, otherEdgeInd);
      }
    }

    this.removeAdjacentOnAllEdges();

    ArrayList<IceChunk> newChunks = new ArrayList<IceChunk>();
    newChunks.add(newChunk1);
    newChunks.add(newChunk2);
    return newChunks;
  }

}
