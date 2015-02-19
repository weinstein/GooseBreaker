package graphics;

public interface Animation {
  public <TImage> TImage getKeyframe(KeyframeFactory<TImage> factory);
}
