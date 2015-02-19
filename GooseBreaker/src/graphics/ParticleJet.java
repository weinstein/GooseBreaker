package graphics;

import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

import physics.LinearTrajectory;
import physics.Trajectory;
import util.RandomUtil;

public class ParticleJet {

  private Animation anim;
  private Vec2 src;
  private float minRadius;
  private float maxRadius;
  private float minSize;
  private float maxSize;
  private float angle;
  private float angleSpread;
  private float minLifetime;
  private float maxLifetime;
  private float minMagnitude;
  private float maxMagnitude;
  private float minAngularVel;
  private float maxAngularVel;
  
  public ParticleJet(Animation anim, Vec2 source) {
    this.anim = anim;
    this.src = source;
  }
  
  public void setRadius(float r) {
    this.minRadius = r;
    this.maxRadius = r;
  }
  
  public void setRadius(float minR, float maxR) {
    this.minRadius = minR;
    this.maxRadius = maxR;
  }
  
  public void setSize(float size) {
    this.minSize = size;
    this.maxSize = size;
  }
  
  public void setSize(float minSize, float maxSize) {
    this.minSize = minSize;
    this.maxSize = maxSize;
  }
  
  public void setAngle(float angle) {
    this.angle = angle;
    this.angleSpread = 0;
  }
  
  public void setAngle(float angle, float spread) {
    this.angle = angle;
    this.angleSpread = spread;
  }
  
  public void setLifetime(float lifetime) {
    this.minLifetime = lifetime;
    this.maxLifetime = lifetime;
  }
  
  public void setLifetime(float minLife, float maxLife) {
    this.minLifetime = minLife;
    this.maxLifetime = maxLife;
  }
  
  public void setMagnitude(float magnitude) {
    this.minMagnitude = magnitude;
    this.maxMagnitude = magnitude;
  }
  
  public void setMagnitude(float minMagnitude, float maxMagnitude) {
    this.minMagnitude = minMagnitude;
    this.maxMagnitude = maxMagnitude;
  }
  
  public void setAngularVelocity(float angularVel) {
    this.minAngularVel = angularVel;
    this.maxAngularVel = angularVel;
  }
  
  public void setAngularVelocity(float min, float max) {
    this.minAngularVel = min;
    this.maxAngularVel = max;
  }
  
  public ParticleEffect generate() {
    float size = (float) RandomUtil.Uniform(this.minSize, this.maxSize);
    float angle = (float) RandomUtil.Uniform(this.angle - this.angleSpread/2, this.angle + this.angleSpread/2);
    float lifetime = (float) RandomUtil.Uniform(this.minLifetime, this.maxLifetime);
    float magnitude = (float) RandomUtil.Uniform(this.minMagnitude, this.maxMagnitude);
    float radius = (float) RandomUtil.Uniform(this.minRadius, this.maxRadius);
    float angularVel = (float) RandomUtil.Uniform(this.minAngularVel, this.maxAngularVel);
    
    Vec2 dir = new Vec2((float) Math.cos(angle), (float) Math.sin(angle));
    float speed = magnitude / lifetime;
    Vec2 vel = new Vec2(speed * dir.x, speed * dir.y);
    
    Trajectory traj = new LinearTrajectory(this.src, vel);
    
    float t0 = radius / speed;
    TrajectoryTween tween = new TrajectoryTween(traj, t0, t0 + lifetime);
    
    return new ParticleEffect(this.anim, tween, angle, angularVel, size, size);
  }
  
  
  
  /*
    private void addParticleJet(
      Animation anim, Vec2 pos, float wmin, float wmax, float hmin, float hmax,
      int num, float angle, float spread, float lifetime, float magnitude) {
    for (int i = 0; i < num; ++i) {
      float dirAngle = (float) RandomUtil.Uniform(angle - spread, angle + spread);
      Vec2 dir = new Vec2((float) Math.cos(dirAngle), (float) Math.sin(dirAngle));
      Trajectory traj = new LinearTrajectory(new Vec3(pos.x, pos.y, 0), new Vec3(magnitude / lifetime * dir.x, magnitude / lifetime * dir.y, 0));
      TrajectoryTween tween = new TrajectoryTween(traj, 0, lifetime);
      float w = (float) RandomUtil.Uniform(wmin, wmax);
      float h = (float) RandomUtil.Uniform(hmin, hmax);
      ParticleEffect part = new ParticleEffect(anim, tween, dirAngle, 0, w, h);
      this.particles.add(part);
    }
  }
  */
}
