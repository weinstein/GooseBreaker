package util;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

public class PhysicsUtil {
  /**
   * Helper for getting the total AABB of the shapes of a body.
   * @param b
   * @return
   */
  public static AABB getBodyShapesAABB(Body b) {
    Vec2 lowerBound = new Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
    Vec2 upperBound = new Vec2(Float.MIN_VALUE, Float.MIN_VALUE);
    
    for (Fixture f = b.getFixtureList(); f != null; f = f.getNext()) {
      Shape s = f.getShape();
      if (s.getType() == ShapeType.POLYGON) {
        PolygonShape polyShape = (PolygonShape) s;
        for (int i = 0; i < polyShape.getVertexCount(); ++i) {
          Vec2 v = polyShape.getVertex(i);
          lowerBound.x = Math.min(lowerBound.x, v.x);
          lowerBound.y = Math.min(lowerBound.y, v.y);
          upperBound.x = Math.max(upperBound.x, v.x);
          upperBound.y = Math.max(upperBound.y, v.y);
        }
      } else if (s.getType() == ShapeType.CIRCLE) {
        CircleShape cirShape = (CircleShape) s;
        lowerBound.x = Math.min(lowerBound.x, cirShape.m_p.x - cirShape.m_radius);
        lowerBound.y = Math.min(lowerBound.y, cirShape.m_p.y - cirShape.m_radius);
        upperBound.x = Math.max(upperBound.x, cirShape.m_p.x + cirShape.m_radius);
        upperBound.y = Math.max(upperBound.y, cirShape.m_p.y + cirShape.m_radius);
      }
    }
    
    return new AABB(lowerBound, upperBound);
  }
  
  /**
   * Helper for getting the total AABB of a World.
   * @param world
   * @return
   */
  public static AABB getWorldShapesAABB(World world) {
    Vec2 lowerBound = new Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
    Vec2 upperBound = new Vec2(Float.MIN_VALUE, Float.MIN_VALUE);
    for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
      AABB bodyAABB = PhysicsUtil.getBodyShapesAABB(b);
      bodyAABB.lowerBound.addLocal(b.getPosition());
      bodyAABB.upperBound.addLocal(b.getPosition());
      lowerBound.x = Math.min(bodyAABB.lowerBound.x, lowerBound.x);
      lowerBound.y = Math.min(bodyAABB.lowerBound.y, lowerBound.y);
      upperBound.x = Math.max(bodyAABB.upperBound.x, upperBound.x);
      upperBound.y = Math.max(bodyAABB.upperBound.y, upperBound.y);
    }
    return new AABB(lowerBound, upperBound);
  }
}
