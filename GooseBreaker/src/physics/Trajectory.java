package physics;

import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

/**
 * Abstracts a pre-determined trajectory in 3-space. Each coordinate is known
 * ahead of time at all points in time.
 * @author Jack
 *
 */
public abstract class Trajectory {
  
  
  public abstract float getXAtTime(float t);
  
  
  public abstract float getYAtTime(float t);
  
  
  public abstract float getZAtTime(float t);
  
  
  public void getPosAtTime(float t, Vec2 pos) {
    pos.x = getXAtTime(t);
    pos.y = getYAtTime(t);
  }
  
  
  public void getPosAtTime(float t, Vec3 pos) {
    pos.x = getXAtTime(t);
    pos.y = getYAtTime(t);
    pos.z = getZAtTime(t);
  }
  
  
  public Vec2 getPosAtTime(float t) {
    Vec2 pos = new Vec2();
    this.getPosAtTime(t, pos);
    return pos;
  }
  
  
  public Vec3 getPos3AtTime(float t) {
    Vec3 pos = new Vec3();
    this.getPosAtTime(t, pos);
    return pos;
  }
}
