package physics;

import util.RandomUtil;
import graphics.ParticleEffect;
import graphics.TrajectoryTween;

/**
 * Represents a piece of food flying on a pre-determined trajectory.
 * @author Jack
 *
 */
public class FlyingPieceOfFood extends ParticleEffect {
  private FoodPiece pieceOfFood;
  
  private static float randomAngle() {
    return (float) RandomUtil.Uniform(0, 2 * Math.PI);
  }
  
  private static float randomAngularVelocity(float magnitude) {
    return (float) RandomUtil.UniformSum(3, -magnitude, magnitude);
  }
  
  public FlyingPieceOfFood(FoodPiece food, TrajectoryTween tween) {
    super(food, tween, randomAngle(), randomAngularVelocity(9),
          food.getShape().getRadius(), food.getShape().getRadius());
    this.pieceOfFood = food;
  }
  
  public FoodPiece getPieceOfFood() {
    return pieceOfFood;
  }
}
