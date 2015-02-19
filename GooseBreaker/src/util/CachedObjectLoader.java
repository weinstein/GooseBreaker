package util;

import java.util.HashMap;
import java.util.Map;

public class CachedObjectLoader<TData> implements ObjectLoader<TData> {
  private ObjectLoader<TData> loader;
  private Map<String, TData> cache;
  
  public CachedObjectLoader(ObjectLoader<TData> loader) {
    this.loader = loader;
    this.cache = new HashMap<String, TData>();
  }

  private TData reload(String resource) {
    TData data = this.loader.get(resource);
    this.cache.put(resource, data);
    return data;
  }
  
  /**
   * If the frame is not cached, load it into the cache and return it.
   * Otherwise, just return the keyframe in the cache.
   */
  @Override
  public TData get(String resource) {
    TData data = this.cache.get(resource);
    if (data == null) {
      return reload(resource);
    }
    return data;
  }

}
