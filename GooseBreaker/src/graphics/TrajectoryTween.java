package graphics;

import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

import physics.Trajectory;

/**
 * An abstraction on top of a simple Trajectory object with extra book keeping
 * for the current time step, and for stepping through time.
 * 
 * The tween position follows the trajectory from tMin to tMax.
 * 
 * @author Jack
 *
 */
public class TrajectoryTween {
  private Trajectory trajectory;
  private float tMin;
  private float tMax;
  private float tCur;
  
  public TrajectoryTween(Trajectory trajectory, float tMin, float tMax) {
    this.trajectory = trajectory;
    this.tMin = tMin;
    this.tMax = tMax;
    this.tCur = tMin;
  }
  
  public float getCurrentTime() {
    return this.tCur;
  }
  
  public float getMinTime() {
    return this.tMin;
  }
  
  public float getMaxTime() {
    return this.tMax;
  }
  
  public Trajectory getTrajectory() {
    return this.trajectory;
  }
  
  public void step(float dt) {
    this.tCur = Math.min(this.tCur + dt, this.tMax);
  }
  
  public float getX() {
    return this.trajectory.getXAtTime(this.tCur);
  }
  
  public float getY() {
    return this.trajectory.getYAtTime(this.tCur);
  }
  
  public float getZ() {
    return this.trajectory.getZAtTime(this.tCur);
  }
  
  public boolean isFinished() {
    return this.tCur >= this.tMax;
  }
  
  public void getPos(Vec2 pos) {
    this.trajectory.getPosAtTime(this.tCur, pos);
  }
  
  public Vec2 getPos() {
    return this.trajectory.getPosAtTime(this.tCur);
  }
  
  public void getPos(Vec3 pos) {
    this.trajectory.getPosAtTime(this.tCur, pos);
  }
  
  public Vec3 getPos3() {
    return this.trajectory.getPos3AtTime(this.tCur);
  }
}
