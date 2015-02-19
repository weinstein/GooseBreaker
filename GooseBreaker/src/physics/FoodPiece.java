package physics;

import graphics.Animation;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

/**
 * Interface for pieces of food (maybe bread, maybe something tastier...!)
 * Food may be sitting on a piece of ice, or it may be floating around on it's
 * own in the water. 
 * @author Jack
 *
 */
public abstract class FoodPiece implements Animation {
  
  /**
   * The shape to use for the food physics body
   * @return
   */
  public abstract Shape getShape();
  
  /**
   * The density of the food physics body
   * @return
   */
  public abstract float getDensity();
  
  /**
   * Create a new Body for the piece of food in the given world and return it.
   * @param world
   */
  public Body attachToWorldAsBody(World world) {
    BodyDef bd = new BodyDef();
    bd.setType(BodyType.DYNAMIC);
    Body b = world.createBody(bd);
    b.setUserData(this);
    b.createFixture(getShape(), getDensity());
    b.setLinearDamping(FreeSpace.WATER_VISCOSITY * 10);
    return b;
  }
}
