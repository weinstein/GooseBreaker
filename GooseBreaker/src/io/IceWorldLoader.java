package io;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;

import physics.Goose;
import physics.IceWorld;
import proto.LevelProto;
import tessellation.IceSheet;

public class IceWorldLoader {
  public IceWorldLoader() {
  }
  
  /**
   * Make static collision walls for the given bounding box.
   * @param world
   * @param bounds
   * @return
   */
  private static Body makeWalls(IceWorld world, AABB bounds) {
    BodyDef bd = new BodyDef();
    bd.type = BodyType.STATIC;
    Body b = world.createBody(bd);
    ChainShape walls = new ChainShape();
    Vec2[] corners = new Vec2[4];
    corners[0] = new Vec2(bounds.lowerBound.x, bounds.lowerBound.y);
    corners[1] = new Vec2(bounds.lowerBound.x, bounds.upperBound.y);
    corners[2] = new Vec2(bounds.upperBound.x, bounds.upperBound.y);
    corners[3] = new Vec2(bounds.upperBound.x, bounds.lowerBound.y);
    walls.createLoop(corners, 4);
    b.createFixture(walls, 100000.0f).setFriction(0);
    return b;
  }
  
  /**
   * Helper for adding elements to the IceWorld from a LevelIO object.
   * @param level
   * @param outputWorld
   */
  public void loadFromProto(LevelIO level, IceWorld outputWorld) {
    // Add ice sheets from the level data.
    for (IceSheet sheet : level.getIceSheets()) {
      outputWorld.addIceSheet(sheet);
    }
    
    // Add boundary walls from the level data.
    if (level.getBounds() != null) {
      IceWorldLoader.makeWalls(outputWorld, level.getBounds());
    }
    
    for (LevelProto.GoosePlacement goosePlacement : level.getGoosePlacements()) {
      Goose goose = new Goose(goosePlacement.getSize());
      Body gooseBody = outputWorld.addAgent(goose);
      gooseBody.setTransform(
          new Vec2(goosePlacement.getWorldX(), goosePlacement.getWorldY()),
          goosePlacement.getAngle());
    }
  }
}
