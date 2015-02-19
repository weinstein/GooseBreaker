package physics;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import tessellation.IceChunk;

/**
 * RayCastCallback that retrieves the non-deleted IceChunk fixture nearest to
 * the ray's starting point. 
 * @author Jack
 *
 */
public class GetFirstIceChunkFixtureCallback implements RayCastCallback {

  private Fixture result;
  private Vec2 pt;
  private float fraction;


  public GetFirstIceChunkFixtureCallback() {
    result = null;
    fraction = 0;
  }


  public float getFraction() {
    return fraction;
  }


  public Fixture getResult() {
    return result;
  }
  
  
  public Vec2 getPoint() {
    return pt;
  }


  @Override
  public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal,
      float fraction) {
    if (!(fixture.getUserData() instanceof IceChunk)) {
      return -1;
    }

    IceChunk chunk = (IceChunk) fixture.getUserData();
    if (chunk.isDeleted()) {
      return -1;
    }

    result = fixture;
    this.fraction = fraction;
    this.pt = new Vec2(point);
    return fraction;
  }


  public void clear() {
    this.fraction = 0;
    this.pt = null;
    this.result = null;
  }

}
