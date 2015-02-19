package tessellation;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;

import util.RandomUtil;

/**
 *        A special PolygonShape with exactly three vertices.
 * 
 * @author Jack
 *
 * 
 */
public class Triangle extends PolygonShape {

  private TriangleEdge[] orderedEdges;
  // We have to keep these separate from the super.m_vertices, because
  // PolygonShape sometimes re-ordered the vertices, and it's very
  // convenient to be able to rely on a consistent order of the vertices
  // and edges.
  private Vec2[] orderedVertices;


  public Triangle(Vec2 a, Vec2 b, Vec2 c) {
    orderedVertices = new Vec2[3];
    for (int i = 0; i < 3; ++i) {
      orderedVertices[i] = new Vec2();
    }
    orderedVertices[0].set(a);
    orderedVertices[1].set(b);
    orderedVertices[2].set(c);
    initEdges();
    this.set(orderedVertices, 3);
  }


  public Triangle(Vec2[] vs) {
    orderedVertices = new Vec2[3];
    for (int i = 0; i < 3; ++i) {
      orderedVertices[i] = new Vec2(vs[i]);
    }
    initEdges();
    this.set(orderedVertices, 3);
  }


  /**
   * Helper method to initialize edges. Note that calling super.set(Vec2[]...)
   * to change the vertices of a triangle will invalid the traingle.
   */
  private void initEdges() {
    orderedEdges = new TriangleEdge[3];
    for (int i = 0; i < 3; ++i) {
      orderedEdges[i] = new TriangleEdge(this, i, (i + 1) % 3, (i + 2) % 3);
    }
  }


  /**
   * Get the edges of the triangle. The order of the returned edges will always
   * be the same.
   * 
   * @return The TriangleEdge's of the Triangle, each corresponding to a pair of
   *         consecutive vertices in their order. edge 0 is vert 0 -> vert 1,
   *         edge 1 is vert 1 -> vert 2, edge 2 is vert 2 -> vert 3
   */
  public TriangleEdge[] getOrderedEdges() {
    return orderedEdges;
  }


  /**
   * Get a single edge of index i. i should be between 0 and 2, or else an array
   * bounds exception will be thrown.
   * 
   * @param i
   *          Index of the edge, either 0, 1, or 2.
   * @return The corresponding TriangleEdge.
   */
  public TriangleEdge getOrderedEdge(int i) {
    return orderedEdges[i];
  }


  /**
   * Get a vertex by index. If i is not between 0 and 2, an array bounds
   * exception will be thrown.
   * 
   * @param i
   *          Index of the vertex, either 0, 1, or 2.
   * @return The corresponding Vec2 for the vertex
   */
  public Vec2 getOrderedVertex(int i) {
    return orderedVertices[i];
  }


  /**
   * Compute the area of the triangle.
   * 
   * @return the area of the triangle.
   */
  public float getArea() {
    float v1x = this.getOrderedVertex(1).x - this.getOrderedVertex(0).x;
    float v1y = this.getOrderedVertex(1).y - this.getOrderedVertex(0).y;
    float v2x = this.getOrderedVertex(2).x - this.getOrderedVertex(0).x;
    float v2y = this.getOrderedVertex(2).y - this.getOrderedVertex(0).y;
    return 0.5f * Math.abs(v1x * v2y - v2x * v1y);
  }


  /**
   * Pick a random edge non-uniformly, giving higher weight to edges with larger
   * opposite angles. Splitting on edges chosen this way helps prevent
   * excessively creating very skinny triangles.
   * 
   * @return A TriangleEdge chosen randomly as described.
   */
  public TriangleEdge randomEdgeWeightedByAngle() {
    double[] edgeOppAngle = new double[3];
    for (int i = 0; i < 3; ++i) {
      TriangleEdge e = orderedEdges[i];
      Vec2 v1 = e.getFirst().sub(e.getOpposite());
      v1.normalize();
      Vec2 v2 = e.getSecond().sub(e.getOpposite());
      v2.normalize();
      edgeOppAngle[i] = 1 - Vec2.dot(v1, v2);
    }
    return RandomUtil.WeightedFrom(orderedEdges, edgeOppAngle);
  }


  /**
   * True iff clockwise oriented by cross product.
   * 
   * @return
   */
  public boolean isClockwise() {
    float v1x = this.getOrderedVertex(1).x - this.getOrderedVertex(0).x;
    float v1y = this.getOrderedVertex(1).y - this.getOrderedVertex(0).y;
    float v2x = this.getOrderedVertex(2).x - this.getOrderedVertex(1).x;
    float v2y = this.getOrderedVertex(2).y - this.getOrderedVertex(1).y;
    return v1x * v2y - v2x * v1y < 0;
  }


  /**
   * If this is not clockwise oriented, return a new triangle with the same
   * vertices as this which is clockwise oriented. If this is already clockwise
   * oriented, just return this.
   * 
   * @return
   */
  public Triangle getClockwise() {
    if (isClockwise()) {
      return this;
    } else {
      return new Triangle(orderedVertices[2], orderedVertices[1],
          orderedVertices[0]);
    }
  }
}
