package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;
import org.junit.Test;

import tessellation.IceChunk;
import tessellation.Triangle;

public class IceChunkTest {
  Triangle t1 = new Triangle(new Vec2(0, 0), new Vec2(1, 0), new Vec2(0, 1));
  Triangle t2 = new Triangle(new Vec2(0, 0), new Vec2(1, 0), new Vec2(0, -1));
  Triangle t3 = new Triangle(new Vec2(0, 0), new Vec2(-1, 0), new Vec2(0, 1));


  @Test
  public void testAddConnectedChunks() {
    IceChunk ice1 = new IceChunk(t1);
    IceChunk ice2 = new IceChunk(t2);
    IceChunk ice3 = new IceChunk(t3);
    ice1.addAdjacentOnEdge(2, ice3, 2);
    ice1.addAdjacentOnEdge(0, ice2, 0);
    ice2.addAdjacentOnEdge(2, ice3, 0);

    assertTrue(ice1.getAdjacentOnEdge(0).contains(ice2));
    assertTrue(ice1.getAdjacentOnEdge(2).contains(ice3));
    assertTrue(ice3.getAdjacentOnEdge(2).contains(ice1));
    assertEquals(0, ice2.getIndexOfEdgeAdjacent(ice1));
  }


  @Test
  public void testAddAndRemoveConnectedChunks() {
    IceChunk ice1 = new IceChunk(t1);
    IceChunk ice2 = new IceChunk(t2);
    IceChunk ice3 = new IceChunk(t3);
    ice1.addAdjacentOnEdge(2, ice3, 2);
    ice1.addAdjacentOnEdge(0, ice2, 0);
    ice2.addAdjacentOnEdge(2, ice3, 0);

    for (IceChunk c : ice1.getAdjacentIceChunks()) {
      ice1.removeAdjacentOnEdge(c);
    }
    assertTrue(ice1.getAdjacentIceChunks().isEmpty());
    assertFalse(ice2.getAdjacentOnEdge(0).contains(ice1));

    assertTrue(ice3.getAdjacentOnEdge(0).contains(ice2));
  }


  @Test
  public void testClockwiseOrderedStuff() {
    IceChunk ice1 = new IceChunk(new Triangle(new Vec2(0, 0), new Vec2(0, 3),
        new Vec2(3, 0)));

    List<IceChunk> adjIce = new ArrayList<IceChunk>();
    for (int i = 2; i >= 0; --i) {
      IceChunk adj = new IceChunk(new Triangle(new Vec2(i + 1, 3 - i), new Vec2(
          i + 1, 2 - i), new Vec2(i, 3 - i)));
      ice1.addAdjacentOnEdge(1, adj, 1);
      adjIce.add(adj);
    }
    List<IceChunk> expected = new ArrayList<IceChunk>();
    for (int i = 2; i >= 0; --i) {
      expected.add(adjIce.get(i));
    }

    List<IceChunk> sorted = ice1.getAdjacentAndDeletedSortedClockwise(1);
    assertEquals(sorted, expected);
  }
  
  
  @Test
  public void testResolveRecursively() {
    IceChunk ice = new IceChunk(new Triangle(new Vec2(0, 0), new Vec2(0, 3),
        new Vec2(3, 0)));
    IceChunk resolved = ice.resolveRecursively(new Vec2(0, 0), Float.MAX_VALUE);
    assertEquals(ice, resolved);
    
    resolved = ice.resolveRecursively(new Vec2(0, 0), 0.1f);
    assertTrue(resolved.getTriangle().getArea() < 0.1f);
    
    assertTrue(ice.getAdjacentIceChunks().isEmpty());
    assertTrue(ice.getAdjacentDeletedIceChunks().isEmpty());
  }

  // TODO: test splitting IceChunk's without randomness.

}
