package physics;

import graphics.BreadAnimation;
import graphics.KeyframeFactory;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;

import tessellation.IceSheet;

/**
 * A food interface implementation for balls of bread. Bread balls physically
 * are circles with a radius and may be sitting on a piece of ice, or not. 
 * @author Jack
 *
 */
public class BreadBall extends FoodPiece {
  
  private static final float DEFAULT_DENSITY = IceSheet.DENSITY / 4;
  private Shape shape;
  private BreadAnimation animation;
  
  public BreadBall(float size) {
    CircleShape ball = new CircleShape();
    ball.setRadius(size);
    this.shape = ball;
    this.animation = new BreadAnimation();
  }
  
  @Override
  public Shape getShape() {
     return shape;
  }

  @Override
  public float getDensity() {
    return BreadBall.DEFAULT_DENSITY;
  }

  @Override
  public <TImage> TImage getKeyframe(KeyframeFactory<TImage> factory) {
    return this.animation.getKeyframe(factory);
  }

}
