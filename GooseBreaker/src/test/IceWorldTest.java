package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.junit.Test;

import physics.IceWorld;
import tessellation.IceChunk;
import tessellation.IceSheet;
import tessellation.Triangle;

public class IceWorldTest {

  @Test
  public void testGetFixtureAt() {
    IceWorld world = new IceWorld();
    IceChunk c1 = new IceChunk(new Triangle(new Vec2(0, 1), new Vec2(1, 1),
        new Vec2(1, 0)));
    IceChunk c2 = new IceChunk(new Triangle(new Vec2(1, 1), new Vec2(1, 0),
        new Vec2(2, 1)));
    IceChunk c3 = new IceChunk(new Triangle(new Vec2(-1, 0), new Vec2(-1, 1),
        new Vec2(-2, 1)));
    c1.addAdjacentOnEdge(0, c2, 0);
    c1.addAdjacentOnEdge(0, c3, 0);
    IceSheet sheet = new IceSheet(c1);
    sheet.addToWorldAsBody(world);

    Fixture f = world.getFixtureAt(new Vec2(0.9f, 0.9f)); // c1
    assertNotNull(f);
    assertEquals(f.getUserData(), c1);

    Fixture f2 = world.getFixtureAt(new Vec2(1.2f, 1.2f)); // nothing
    assertNull(f2);
  }

}
