package physics;

import graphics.GooseAnimation;
import graphics.KeyframeFactory;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.WeldJointDef;

/**
 * A Goose agent that swims towards the nearest BreadBall within a cone of
 * vision. 
 * @author Jack
 *
 */
public class Goose extends Agent {
  // TODO: these all depend on the goose size of about 5 to get proper control.
  // It'd be nice if they were independent of the goose size.
  private static final float DENSITY = 1.0f;
  private static final float SWIM_STROKE_IMPULSE = 15.0f;
  private static final float LINEAR_DAMPING_FACTOR = 1.2f;
  private static final float ANGULAR_DAMPING_FACTOR = 0.2f;
  private static final float VISION_CONE_COS_HALF_ANGLE = -1.0f / 2;
  private static final float SWIM_FREQUENCY = 0.4f;
  private static final float SWIM_STROKE_TIME = 0.15f;
  
  private float width = 1.0f;
  private float height = 1.0f;
  private float stepTimer;
  private float swimFrequency;
  private float strokeTime;
  private GooseAnimation animation;
  
  
  public Goose(float size) {
    this.width = size;
    this.height = size;
    this.stepTimer = 0;
    this.swimFrequency = Goose.SWIM_FREQUENCY;
    this.strokeTime = Goose.SWIM_STROKE_TIME;
    this.animation = new GooseAnimation();
  }
  
  
  public BodyDef getBodyDef() {
    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.linearDamping = Goose.LINEAR_DAMPING_FACTOR;
    return bd;
  }
  

  @Override
  public Body addToWorldAsBody(World world) {
    Vec2[] pts = new Vec2[5];
    pts[0] = new Vec2(0, -height/2);
    pts[1] = new Vec2(-width/2, -height/2);
    pts[2] = new Vec2(-width/2, height/2);
    pts[3] = new Vec2(0, height/2);
    pts[4] = new Vec2(width/3, 0);
    PolygonShape trapShape = new PolygonShape();
    trapShape.set(pts, 5);
    
    CircleShape bumper = new CircleShape();
    bumper.m_p.set(0, 0);
    bumper.setRadius(width/2);
    
    Body b = world.createBody(getBodyDef());
    b.createFixture(trapShape, Goose.DENSITY);
    Fixture f = b.createFixture(bumper, 0);
    f.setSensor(true);
    
    b.setUserData(this);
    b.setAngularDamping(b.getMass() * Goose.ANGULAR_DAMPING_FACTOR);
    
    return b;
  }
  
  
  /**
   * Apply impulse to the given body from either the left or right leg.
   * @param body Body to apply impulse to.
   * @param leftLeg If true, apply impulse as though from the left leg
   * (forward is neg. y-direction). If false, apply as though from the right leg.
   */
  private void applySwimImpulse(Body body, boolean leftLeg, float dt) {
    Vec2 localLegPt = new Vec2(0, width/2 * (leftLeg ? 1 : -1));
    Vec2 applicationPt = body.getWorldPoint(localLegPt);
    Vec2 impulseVector = new Vec2(1, 0);
    impulseVector.mulLocal(body.getMass() * Goose.SWIM_STROKE_IMPULSE * dt);
    Transform xf = new Transform();
    xf.set(new Vec2(), body.getAngle());
    impulseVector = Transform.mul(xf, impulseVector);
    
    body.applyLinearImpulse(impulseVector, applicationPt, true);
  }
  
  
  /**
   * Look at all the food pieces in the world, and get the vector direction
   * from the body to the nearest food piece. Ignore food not within the cone
   * of vision, as though the goose cannot "see" the food.
   * TODO: The goose has an infinitesimally short term memory; maybe increase it
   * @param body
   * @return
   */
  private Vec2 getDirOfNearestFoodPiece(Body body) {
    IceWorld world = (IceWorld) body.getWorld();
    Vec2 gooseDir = new Vec2((float) (Math.cos(body.getAngle())), (float) Math.sin(body.getAngle()));
    Vec2 toNearestFood = null;
    float nearestFoodDist = Float.MAX_VALUE;
    for (Body foodBody : world.getFoodPieceBodies()) {
      Vec2 towardsFood = foodBody.getPosition().sub(body.getPosition());
      float cosAngle = Vec2.dot(gooseDir, towardsFood) / gooseDir.length() / towardsFood.length();
      if (cosAngle < Goose.VISION_CONE_COS_HALF_ANGLE) {
        continue;
      }
      if (towardsFood.lengthSquared() < nearestFoodDist) {
        nearestFoodDist = towardsFood.lengthSquared();
        toNearestFood = towardsFood;
      }
    }
    return toNearestFood;
  }
  
  
  private void addToTimer(float dt) {
    this.stepTimer += dt;
    while (this.stepTimer > this.swimFrequency) {
      this.stepTimer -= this.swimFrequency;
    }
  }


  /**
   * The goose steps by finding the nearest food piece, and swimming towards it.
   */
  @Override
  public boolean step(float dt, Body myBody) {
    Vec2 travelDir = getDirOfNearestFoodPiece(myBody);
    if (travelDir == null) {
      stepTimer = this.swimFrequency;
      return false;
    }
    
    Vec2 gooseDir = new Vec2((float) (Math.cos(myBody.getAngle())), (float) Math.sin(myBody.getAngle()));
    // Is the food to the left or to the right from the goose's point of view?
    // If the food is clockwise from the goose, that means the goose should turn
    // left, which means swim with the right leg (and vice versa)
    boolean clockwise = Vec2.cross(gooseDir, travelDir) > 0;
      
    if (this.isFlapping()) {
      applySwimImpulse(myBody, !clockwise, dt);
    }
    this.addToTimer(dt);
    return true;
  }


  public boolean isFlapping() {
    return this.stepTimer < this.strokeTime;
  }


  @Override
  public <TImage> TImage getKeyframe(KeyframeFactory<TImage> factory) {
    this.animation.setBody(this.getBody());
    return this.animation.getKeyframe(factory);
  }
}
