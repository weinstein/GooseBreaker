package graphics;

import util.CachedObjectLoader;
import util.ObjectLoader;

/**
 * Take another keyframe factory to delegate the actual work of loading
 * keyframes, and cache the results in memory, loading uncached keyframes as
 * needed.
 * @author Jack
 *
 * @param <TImage> The class of the image objects produced by the factory.
 */
public class CachedKeyframeFactory<TImage> extends KeyframeFactory<TImage> {
  private CachedObjectLoader<TImage> cache;
  
  public CachedKeyframeFactory(ObjectLoader<TImage> loader) {
    this.cache = new CachedObjectLoader<TImage>(loader);
  }

  @Override
  public TImage get(String resourceName) {
    return cache.get(resourceName);
  }

}
