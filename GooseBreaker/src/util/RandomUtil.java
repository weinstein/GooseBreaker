package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class RandomUtil {

  /**
   * Return a uniform double in [a, b)
   * 
   * @param a
   *          Lower bound (inclusive)
   * @param b
   *          Upper bound (exclusive)
   * @return
   */
  public static double Uniform(double a, double b) {
    return Math.random() * (b - a) + a;
  }


  /**
   * Return a number chosen from an n-ary uniform sum distribution, rescaled to
   * be between [a, b). Ex: n=1 is uniform, n=2 is piecewise linear, ... n >> 2:
   * nearly normal, but for fixed [a, b), becomes arbitrarily sharp at (a + b) /
   * 2.
   * 
   * @param n
   *          Number of uniform distributions to sum
   * @param a
   *          Lower bound (inclusive)
   * @param b
   *          Upper bound (exclusive)
   * @return
   */
  public static double UniformSum(int n, double a, double b) {
    double rand = a;
    for (int i = 0; i < n; ++i) {
      rand += Math.random() * (b - a) / n;
    }
    return rand;
  }


  /**
   * Return a random integer in [a, b)
   * 
   * @param a
   *          Lower bound (inclusive)
   * @param b
   *          Upper bound (exclusive)
   * @return
   */
  public static int UniformInt(int a, int b) {
    return (int) Uniform(a, b);
  }


  /**
   * Return a random integer in [a, b]
   * 
   * @param a
   *          Lower bound (inclusive)
   * @param b
   *          Upper bound (inclusive)
   * @return
   */
  public static int UniformIntInclusive(int a, int b) {
    return (int) Uniform(a, b + 1);
  }


  /**
   * Return a random shuffling of the input list in O(n) time.
   * 
   * @param items
   * @return
   */
  public static <T> List<T> Shuffle(List<T> items) {
    ArrayList<T> shuffled = new ArrayList<T>(items);
    for (int i = 0; i < items.size(); ++i) {
      int j = UniformInt(i, items.size());
      T tmp = shuffled.get(j);
      shuffled.set(j, shuffled.get(i));
      shuffled.set(i, tmp);
    }
    return shuffled;
  }


  /**
   * Return a single item chosen uniformly from the input list. The input list
   * must not be empty.
   * 
   * @param items
   * @return
   */
  public static <T> T UniformFrom(List<T> items) {
    return items.get(UniformInt(0, items.size()));
  }


  /**
   * Select n items uniformly and without replacement from the input list.
   * 
   * @param n
   *          Number of items to select.
   * @param items
   *          Input list of items.
   * @return
   */
  public static <T> Collection<T> UniformFrom(int n, List<T> items) {
    Collection<T> result = new HashSet<T>();
    List<T> shuffled = Shuffle(items);
    for (int i = 0; i < n; ++i) {
      result.add(shuffled.get(i));
    }
    return result;
  }


  /**
   * Select a single item non-uniformly from the input list according to the
   * given relative weights.
   * 
   * @param items
   * @param weights
   * @return
   */
  public static <T> T WeightedFrom(T[] items, double[] weights) {
    int n = Math.min(items.length, weights.length);

    double totalWeight = 0;
    for (int i = 0; i < n; ++i) {
      totalWeight += weights[i];
    }

    double rand = Uniform(0, totalWeight);
    for (int i = 0; i < n; ++i) {
      if (rand < weights[i]) {
        return items[i];
      } else {
        rand -= weights[i];
      }
    }
    return null;
  }
}
