package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.junit.Test;

import tessellation.IceChunk;
import tessellation.IceSheet;
import tessellation.Triangle;

public class IceSheetTest {

  @Test
  public void testCreateFromConnectedChunks() {
    // c1, c2, c3 are all connected. c4 is not.
    // The ice sheet initialized using c1 should contain c1, c2, and c3 but
    // not c4.
    IceChunk c1 = new IceChunk(new Triangle(new Vec2(0, 1), new Vec2(1, 1),
        new Vec2(1, 0)));
    IceChunk c2 = new IceChunk(new Triangle(new Vec2(1, 1), new Vec2(1, 0),
        new Vec2(2, 1)));
    IceChunk c3 = new IceChunk(new Triangle(new Vec2(-1, 0), new Vec2(-1, 1),
        new Vec2(-2, 1)));
    IceChunk c4 = new IceChunk(new Triangle(new Vec2(-1, 1), new Vec2(-1, 2),
        new Vec2(-2, 2)));
    // The adjacent index doesn't matter here; we just care about the overall
    // adjacency graph for IceSheets.
    c1.addAdjacentOnEdge(0, c2, 0);
    c1.addAdjacentOnEdge(0, c3, 0);
    IceSheet sheet = new IceSheet(c1);
    assertTrue(sheet.getIceChunksCopy().contains(c1));
    assertTrue(sheet.getIceChunksCopy().contains(c2));
    assertTrue(sheet.getIceChunksCopy().contains(c3));
    assertFalse(sheet.getIceChunksCopy().contains(c4));
  }


  @Test
  public void testSplitSheetIntoSheets() {
    // Start with c1, c2, and c3 connected as: c3 <-> c1 <-> c2
    // Removing c1 should split the sheet into two ice sheets: one for c3 and
    // one for c2.
    IceChunk c1 = new IceChunk(new Triangle(new Vec2(0, 1), new Vec2(1, 1),
        new Vec2(1, 0)));
    IceChunk c2 = new IceChunk(new Triangle(new Vec2(1, 1), new Vec2(1, 0),
        new Vec2(2, 1)));
    IceChunk c3 = new IceChunk(new Triangle(new Vec2(-1, 0), new Vec2(-1, 1),
        new Vec2(-2, 1)));
    c1.addAdjacentOnEdge(0, c2, 0);
    c1.addAdjacentOnEdge(0, c3, 0);
    IceSheet sheet = new IceSheet(c1);
    ArrayList<IceChunk> removeChunks = new ArrayList<IceChunk>();
    removeChunks.add(c1);
    Collection<IceSheet> splitSheets = sheet.removeIceChunks(removeChunks);

    assertEquals(splitSheets.size(), 2);
    for (IceSheet splitSheet : splitSheets) {
      assertEquals(splitSheet.getIceChunksCopy().size(), 1);
      assertTrue(splitSheet.getIceChunksCopy().contains(c2)
          || splitSheet.getIceChunksCopy().contains(c3));
      assertFalse(splitSheet.getIceChunksCopy().contains(c1));
    }
  }


  @Test
  public void testAddToWorldAsPhysicsBody() {
    IceChunk c1 = new IceChunk(new Triangle(new Vec2(0, 1), new Vec2(1, 1),
        new Vec2(1, 0)));
    IceChunk c2 = new IceChunk(new Triangle(new Vec2(1, 1), new Vec2(1, 0),
        new Vec2(2, 1)));
    IceChunk c3 = new IceChunk(new Triangle(new Vec2(-1, 0), new Vec2(-1, 1),
        new Vec2(-2, 1)));
    c1.addAdjacentOnEdge(0, c2, 0);
    c1.addAdjacentOnEdge(0, c3, 0);
    IceSheet sheet = new IceSheet(c1);

    World w = new World(new Vec2(0, 0));
    Body sheetBody = sheet.addToWorldAsBody(w);
    // There should be a fixture for each chunk on the perimeter
    assertEquals(sheetBody.m_fixtureCount, 3);

    // Each fixture should have an attached IceChunk in it's user data.
    Collection<IceChunk> chunkUserData = new HashSet<IceChunk>();
    for (Fixture f = sheetBody.getFixtureList(); f != null; f = f.getNext()) {
      assertTrue(f.getUserData() instanceof IceChunk);
      chunkUserData.add((IceChunk) f.getUserData());
    }
    // All three chunks should be accounted for in the body.
    assertTrue(chunkUserData.contains(c1));
    assertTrue(chunkUserData.contains(c2));
    assertTrue(chunkUserData.contains(c3));
  }

  @Test
  public void testPerimeterOfIceSheet() {
    IceChunk interior = new IceChunk(new Triangle(
        new Vec2(0, 0), new Vec2(0, 1), new Vec2(1, 0)));
    IceChunk exterior1 = new IceChunk(new Triangle(new Vec2(1, 0), new Vec2(0, 1), new Vec2(1, 1)));
    IceChunk deleted = new IceChunk(new Triangle(new Vec2(0, 1), new Vec2(1, 1), new Vec2(1, 2)));
    deleted.setDeleted();
    IceChunk exterior2 = new IceChunk(new Triangle(new Vec2(-1, 0), new Vec2(1, 0), new Vec2(0, 0)));
    IceChunk exterior3 = new IceChunk(new Triangle(new Vec2(1, 0), new Vec2(0, -1), new Vec2(0, 0)));
    deleted.addAdjacentOnEdge(0, exterior1, 1);
    interior.addAdjacentOnEdge(1, exterior1, 0);
    interior.addAdjacentOnEdge(0, exterior2, 1);
    interior.addAdjacentOnEdge(2, exterior3, 2);
    IceSheet sheet = new IceSheet(interior);
    
    Set<IceChunk> bareChunks = new HashSet<IceChunk>(sheet.getBareIceChunksCopy());
    Set<IceChunk> expectedBare = new HashSet<IceChunk>();
    expectedBare.add(exterior1);
    expectedBare.add(exterior2);
    expectedBare.add(exterior3);
    assertEquals(bareChunks, expectedBare);
    
    List<Vec2> perimeter = sheet.getBareEdgePerimeterCopy();
    assertEquals(6, perimeter.size());
  }
}
