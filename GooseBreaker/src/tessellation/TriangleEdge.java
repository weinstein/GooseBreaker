package tessellation;

import org.jbox2d.common.Vec2;

/**
 *         A TraingleEdge represents one edge from a Triangle shape. The edge
 *         maintains a reference to the parent Triangle, the indices of the
 *         endpoints of the edge, and the index of the vertex opposite the edge.
 * 
 * @author Jack
 *
 */
public class TriangleEdge {
  private int firstPointInd;
  private int secondPointInd;
  private Triangle parentTriangle;
  private int oppositeVertInd;
  
  private float len;
  private Vec2 dir;


  /**
   * Create a new TriangleEdge representing an edge in a triangle from vertex p1
   * to vertex p2 (and with opposite-angle vertex index oppInd).
   * 
   * @param triangle
   * @param p1
   * @param p2
   * @param oppInd
   */
  public TriangleEdge(Triangle triangle, int p1, int p2, int oppInd) {
    firstPointInd = p1;
    secondPointInd = p2;
    parentTriangle = triangle;
    oppositeVertInd = oppInd;
    
    dir = getSecond().sub(getFirst());
    this.len = dir.length();
  }

  public Triangle getParent() {
    return this.parentTriangle;
  }

  public float getLength() {
    return len;
  }


  /**
   * Get the first endpoint of the edge.
   * 
   * @return
   */
  public Vec2 getFirst() {
    return this.parentTriangle.getOrderedVertex(this.firstPointInd);
  }


  /**
   * Get the second endpoint of the edge.
   * 
   * @return
   */
  public Vec2 getSecond() {
    return this.parentTriangle.getOrderedVertex(this.secondPointInd);
  }
  
  
  public Vec2 getDirection() {
    return dir;
  }


  /**
   * Get the vertex of the opposite-angle from the edge.
   * 
   * @return
   */
  public Vec2 getOpposite() {
    return this.parentTriangle.getOrderedVertex(this.oppositeVertInd);
  }


  /**
   * Get the index of the edge, ie: this ==
   * this.parentTriangle.getOrderedEdge(this.getEdgeIndex())
   * 
   * @return
   */
  public int getEdgeIndex() {
    return firstPointInd;
  }


  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TriangleEdge)) {
      return false;
    }
    TriangleEdge e = (TriangleEdge) o;
    if (e.parentTriangle != parentTriangle) {
      return false;
    }
    return (e.firstPointInd == firstPointInd && e.secondPointInd == secondPointInd)
        || (e.firstPointInd == secondPointInd && e.secondPointInd == firstPointInd);
  }


  @Override
  public int hashCode() {
    return getFirst().hashCode() | getSecond().hashCode();
  }
}
