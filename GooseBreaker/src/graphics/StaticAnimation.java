package graphics;

public class StaticAnimation implements Animation {
  private String res;
  private int frame;
  
  public StaticAnimation(String res, int frame) {
    this.res = res;
    this.frame = frame;
  }

  @Override
  public <TImage> TImage getKeyframe(KeyframeFactory<TImage> factory) {
    return factory.getKeyframe(this.res, this.frame);
  }

}
