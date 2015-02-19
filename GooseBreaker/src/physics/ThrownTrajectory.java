package physics;

import org.jbox2d.common.Vec3;

/**
 * A special kind of trajectory representing something thrown through the air
 * in the presence of gravity and possibly air resistance.
 * 
 * Represents the solution to the differential equations:
 * x'' = -c x'
 * y'' = -c y'
 * z'' = -g - c z'
 * 
 * Note that the special case c = 0 has to be handled separately, since the
 * general solution only works in the limit as c -> 0.
 * 
 * @author Jack
 *
 */
public class ThrownTrajectory extends Trajectory {
  private Vec3 initialPos;
  private Vec3 initialVel;
  private float gravity;
  private float damping;
  
  public ThrownTrajectory(Vec3 pos0, Vec3 vel0, float g, float c) {
    this.initialPos = new Vec3(pos0);
    this.initialVel = new Vec3(vel0);
    this.gravity = g;
    this.damping = c;
  }
  
  @Override
  public float getXAtTime(float t) {
    if (damping == 0.0f) {
      return initialPos.x + initialVel.x * t;
    }
    
    float dampingFactor = (float) (1 - Math.exp(-damping * t));
    return initialPos.x + initialVel.x / damping * dampingFactor;
  }
  
  @Override
  public float getYAtTime(float t) {
    if (damping == 0.0f) {
      return initialPos.y + initialVel.y* t;
    }
    
    float dampingFactor = (float) (1 - Math.exp(-damping * t));
    return initialPos.y + initialVel.y / damping * dampingFactor;
  }
  
  @Override
  public float getZAtTime(float t) {
    if (damping == 0.0f) {
      return initialPos.z + initialVel.z * t - 0.5f * gravity * t * t;
    }
    
    float dampingFactor = (float) (1 - Math.exp(-damping * t));
    float gOverC = gravity / damping;
    return initialPos.z - gOverC * t + (initialVel.z + gOverC) / damping * dampingFactor;
  }
  
  private float undampedTimeOfFlight() {
    float radical = (float) Math.sqrt(initialVel.z * initialVel.z + 2 * initialPos.z * gravity);
    return (initialVel.z + radical) / (gravity);
  }
  
  /**
   * Stable but possibly slow. Make use of the fact that z(t)=0 for exactly
   * one value of t between under and over.
   * @param under
   * @param over
   * @param maxError
   * @return
   */
  private float getTimeOfFlightBinSearch(float under, float over, float maxError) {
    float guess = under/2.0f + over/2.0f;
    float height = getZAtTime(guess);
    if (Math.abs(height) < maxError) {
      return guess;
    } else if (height < 0) {
      return getTimeOfFlightBinSearch(under, guess, maxError);
    } else {
      return getTimeOfFlightBinSearch(guess, over, maxError);
    }
  }
  
  /**
   * Get the smallest time > 0 for which z <= 0, with error no more than
   * maxError.
   * @param maxError
   * @return
   */
  public float getTimeOfFlightApprox(float maxError) {
    if (damping == 0) {
      return undampedTimeOfFlight();
    } else if (initialPos.z < 0) {
      return 0;
    }
    float underEstimate = 0;
    // using the fact that 0 <= 1 - e^(-ct) <= 1 for t >= 0 and c > 0,
    // z < z_approx = z_0 - g/c t + (z'_0 + g/c) / c so when z_approx = 0, z <= 0 too.
    float overEstimate = damping / gravity * (initialPos.z + (initialVel.z + gravity / damping) / damping);
    return getTimeOfFlightBinSearch(underEstimate, overEstimate, maxError);
  }
}
