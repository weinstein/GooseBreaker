package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jbox2d.common.Vec2;
import org.junit.Test;

import tessellation.Triangle;
import tessellation.TriangleEdge;

public class TriangleTest {

  @Test
  public void test() {
    Triangle t = new Triangle(new Vec2(0, 0), new Vec2(1, 0), new Vec2(0, 1));
    assertEquals(t.getOrderedVertex(0), new Vec2(0, 0));
    assertEquals(t.getOrderedVertex(1), new Vec2(1, 0));
    assertEquals(t.getOrderedVertex(2), new Vec2(0, 1));

    TriangleEdge e1 = t.getOrderedEdge(0);
    assertEquals(e1.getFirst(), new Vec2(0, 0));
    assertEquals(e1.getSecond(), new Vec2(1, 0));
    assertEquals(e1.getOpposite(), new Vec2(0, 1));
    TriangleEdge e2 = t.getOrderedEdge(2);
    assertEquals(e2.getFirst(), new Vec2(0, 1));
    assertEquals(e2.getSecond(), new Vec2(0, 0));
    assertEquals(e2.getOpposite(), new Vec2(1, 0));

    assertEquals(t.getArea(), 0.5f, 1e-4);
  }


  @Test
  public void testClockwise() {
    Triangle t = new Triangle(new Vec2(0, 0), new Vec2(1, 0), new Vec2(0, 1));
    assertFalse(t.isClockwise());
    assertTrue(t.getClockwise().isClockwise());
    Triangle t2 = new Triangle(new Vec2(0, 0), new Vec2(0, 1), new Vec2(1, 0));
    assertTrue(t2.isClockwise());
    assertEquals(t2, t2.getClockwise());
  }

}
