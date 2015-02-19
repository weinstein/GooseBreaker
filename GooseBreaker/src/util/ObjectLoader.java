package util;

public interface ObjectLoader<TData> {
  public TData get(String resourceName);
}
