package physics;

import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

/**
 * A simple linear trajectory with initial position and velocity.
 * @author Jack
 *
 */
public class LinearTrajectory extends Trajectory {
  private Vec3 startPos;
  private Vec3 vel;
  
  public LinearTrajectory(Vec3 startPos, Vec3 velocity) {
    this.startPos = startPos;
    this.vel = velocity;
  }
  
  public LinearTrajectory(Vec2 startPos, Vec2 velocity) {
    this.startPos = new Vec3(startPos.x, startPos.y, 0);
    this.vel = new Vec3(velocity.x, velocity.y, 0);
  }

  @Override
  public float getXAtTime(float t) {
    return startPos.x + t * vel.x;
  }

  @Override
  public float getYAtTime(float t) {
    return startPos.y + t * vel.y;
  }

  @Override
  public float getZAtTime(float t) {
    return startPos.z + t * vel.z;
  }

}
