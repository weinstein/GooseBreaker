package graphics;

/**
 * Animation for BreadBalls. Just a static bread image.
 * @author Jack
 *
 */
public class BreadAnimation extends StaticAnimation {
  private static final String BREAD_RES_NAME = "bread";
  
  public BreadAnimation() {
    super(BreadAnimation.BREAD_RES_NAME, 0);
  }

}
