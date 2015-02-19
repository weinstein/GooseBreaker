package graphics;

import org.jbox2d.dynamics.Body;

import physics.Goose;

/**
 * The goose has a different animation depending on which direction it's facing.
 * It also flaps.
 * @author Jack
 *
 */
public class GooseAnimation extends AnimatedBody {
  private enum Dir {
    UP("goose_up"),
    DOWN("goose_down"),
    LEFT("goose_left"),
    RIGHT("goose_right");
    
    private String resName;
    private Dir(String resName) {
      this.resName = resName;
    }
    
    public String getResourceName() {
      return this.resName;
    }
  }
  
  
  /**
   * Get the keyframe number -- 0 for not flapping, 1 for flapping.
   * @return
   */
  private int getAnimationFrameNum(Body b) {
    Goose goose = (Goose) b.getUserData();
    if (goose.isFlapping()) {
      return 1;
    } else {
      return 0;
    }
  }
  
  /**
   * Get the base name of the keyframe resources, depending on the goose's
   * direction.
   * @return
   */
  private String getAnimationResource(Body body) {
    double cos = Math.cos(body.getAngle());
    double sin = Math.sin(body.getAngle());
    double root2over2 = Math.sqrt(2) / 2.0;
    
    if (cos > root2over2) {
      return Dir.RIGHT.getResourceName();
    } else if (cos < -root2over2) {
      return Dir.LEFT.getResourceName();
    } else if (sin > 0) {
      return Dir.DOWN.getResourceName();
    } else {
      return Dir.UP.getResourceName();
    }
  }


  @Override
  public <TImage> TImage getKeyframe(KeyframeFactory<TImage> factory) {
    Body b = this.getBody();
    return factory.getKeyframe(this.getAnimationResource(b), this.getAnimationFrameNum(b));
  }
  
}
