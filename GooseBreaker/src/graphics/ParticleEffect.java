package graphics;

import org.jbox2d.common.Vec2;

public class ParticleEffect {
  
  private TrajectoryTween posTween;
  private float initAngle;
  private float angleVel;
  private Animation animation;
  private float width, height;
  
  public ParticleEffect(Animation anim, TrajectoryTween posTween, float initAngle, float angleVel, float width, float height) {
    this.posTween = posTween;
    this.animation = anim;
    this.initAngle = initAngle;
    this.angleVel = angleVel;
    this.width = width;
    this.height = height;
  }
  
  public Vec2 getPos() {
    return this.posTween.getPos();
  }
  
  
  public Vec2 getFinalPos() {
    return this.posTween.getTrajectory().getPosAtTime(this.posTween.getMaxTime());
  }
  
  
  public boolean isFinished() {
    return this.posTween.isFinished();
  }
  
  
  public void step(float dt) {
    this.posTween.step(dt);
  }
  
  
  public float getAngle() {
    return this.initAngle + this.posTween.getCurrentTime() * this.angleVel;
  }
  
  public float getScale() {
    float height = this.posTween.getZ();
    return 1 + (float) Math.sqrt(height);
  }
  
  
  public float getWidth() {
    return this.width * this.getScale();
  }
  
  public float getHeight() {
    return this.height * this.getScale();
  }
  
  
  public Animation getAnimation() {
    return this.animation;
  }
}
