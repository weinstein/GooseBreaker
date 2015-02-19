package graphics;

import util.ObjectLoader;

/**
 * The KeyframeFactory abstracts an image-like object loader, although no
 * real restrictions are placed on the kind of object produced by the factory.
 * @author Jack
 *
 * @param <TImage> The class of objects produced by the factory.
 */
public abstract class KeyframeFactory<TImage> implements ObjectLoader<TImage> {
  
  public char getSeperator() {
    return '_';
  }
  
  public TImage getKeyframe(String resource, int frame) {
    return this.get(resource + getSeperator() + frame);
  }
}
