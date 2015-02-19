package tessellation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

import physics.FreeSpace;

/**
 *         An IceSheet represents a rigid collection of connected IceChunks.
 *         IceSheet's are divisible by removing IceChunk's and querying the
 *         graph of IceChunk's for connected components.
 * 
 * @author Jack
 *
 */
public class IceSheet {

  public static final float DENSITY = 2.0f;
  private IceChunk rootChunk;
  private float maxArea;
  private Set<IceChunk> iceChunksCached;
  private Set<IceChunk> iceChunksAndDeletedCached;
  private Set<IceChunk> perimeterChunksCached;
  private List<Vec2> perimeterCached;
  private boolean isCachedDirty;
  private Transform eye;
  
  public IceSheet(IceChunk chunk) {
    rootChunk = chunk;
    maxArea = Float.MAX_VALUE;
    isCachedDirty = true;
    iceChunksCached = new HashSet<IceChunk>();
    iceChunksAndDeletedCached = new HashSet<IceChunk>();
    perimeterChunksCached = new HashSet<IceChunk>();
    perimeterCached = new ArrayList<Vec2>();
    eye = new Transform();
  }
  
  
  public float computeArea() {
    float area = 0;
    for (IceChunk chunk : getIceChunks()) {
      area += chunk.getTriangle().getArea();
    }
    return area;
  }
  
  
  /**
   * Get the rootChunk of the IceSheet. The rootChunk is guaranteed to be
   * non-deleted, and to be connected to the ice sheet.
   * @return
   */
  public IceChunk getRootChunk() {
    return rootChunk;
  }
  
  
  public float getMaxArea() {
    return maxArea;
  }
  
  
  public void setMaxArea(float val) {
    maxArea = val;
  }
  
  
  /**
   * Get an IceChunk in the sheet (after transformation) at the given world
   * position. Automatically resolve non-particulate ice chunks on the way down.
   * @param xf
   * @param pos
   * @return
   */
  public IceChunk getIceChunkAt(Vec2 pos) {
    for (IceChunk chunk : getIceChunks()) {
      if (chunk.getTriangle().testPoint(eye, pos)) {
        return resolveIceChunk(chunk, pos);
      }
    }
    return null;
  }
  
  
  /**
   * Handle splitting a single IceChunk once by calling superChunk.randomBinaryDivision(),
   * if it is big enough, and update the root chunk (since it may be set to an
   * invalid chunk after division).
   * @param superChunk
   */
  public ArrayList<IceChunk> randomBinaryDivision(IceChunk superChunk) {
    ArrayList<IceChunk> splitChunks = superChunk.randomBinaryDivision();
    this.isCachedDirty = true;
    this.rootChunk = splitChunks.iterator().next();
    return splitChunks;
  }
  
  
  /**
   * Resolve an ice chunk recursively and update the root chunk to remain valid.
   * @param superChunk
   * @param xf
   * @param pos
   * @return
   */
  public IceChunk resolveIceChunk(IceChunk superChunk, Vec2 pos) {
    IceChunk result = superChunk.resolveRecursively(pos, maxArea);
    isCachedDirty = true;
    rootChunk = result;
    return result;
  }
  
  
  public Set<IceChunk> getIceChunks() {
    if (this.isCachedDirty) {
      this.computeCachedPerimeter();
      this.isCachedDirty = false;
    }
    return this.iceChunksCached;
  }
  
  
  private Set<IceChunk> getIceChunksIncludingDeleted() {
    if (this.isCachedDirty) {
      this.computeCachedPerimeter();
      this.isCachedDirty = false;
    }
    return this.iceChunksAndDeletedCached;
  }
  
  
  public Set<IceChunk> getBareIceChunks() {
    if (this.isCachedDirty) {
      this.computeCachedPerimeter();
      this.isCachedDirty = false;
    }
    return this.perimeterChunksCached;
  }
  
  
  private List<Vec2> getBareEdgePerimeter() {
    if (this.isCachedDirty) {
      this.computeCachedPerimeter();
      this.isCachedDirty = false;
    }
    return this.perimeterCached;
  }


  /**
   * Get all of the IceChunks in this IceSheet by finding all reachable
   * IceChunks from the root chunk.
   * 
   * @return A Queue of IceChunks part of this rigid ice sheet.
   */
  public ArrayList<IceChunk> getIceChunksCopy() {
    return new ArrayList<IceChunk>(this.getIceChunks());
  }
  
  
  /**
   * Same as above, but include the deleted chunks immediately adjacent to non-
   * deleted chunks.
   * @return
   */
  public ArrayList<IceChunk> getIceChunksIncludingDeletedCopy() {
    return new ArrayList<IceChunk>(this.getIceChunksIncludingDeleted());
  }


  /**
   * Get the ice chunks along the perimeter of the sheet (those with bare
   * edges).
   * 
   * @return
   */
  public ArrayList<IceChunk> getBareIceChunksCopy() {
    return new ArrayList<IceChunk>(this.getBareIceChunks());
  }
  

  /**
   * Get the list of points, in order, which define the polygon outline of the
   * ice sheet (assuming there are no holes) using a clockwise-order DFS through
   * the chunks. The result may or may not be concave depending on the shape of
   * the sheet.
   * 
   * @param perimeterPts
   *          Add list of points on the perimeter to this list in order.
   */
  public ArrayList<Vec2> getBareEdgePerimeterCopy() {
    return new ArrayList<Vec2>(this.getBareEdgePerimeter());
  }


  /**
   * Tail-recursive helper for getBareEdgePerimeter. This function works by DFS
   * on connected non-deleted IceChunks, visiting adjacent chunks in
   * clockwise-order. The result, after all the recursive calls have finished,
   * is that the perimeter of the ice sheet (the bare edges) will have been
   * discovered in clockwise-order, and similarly the endpoints of the edges.
   * 
   * @param curChunk
   *          The current chunk we're going to look at.
   * @param prevChunk
   *          The chunk we just came from. We'll visit neighbors in
   *          clockwise-order, starting from the prevChunk.
   * @param visited
   *          The set of visited chunks. Don't visit a chunk more than once.
   * @param output
   *          Add endpoints of bare (perimeter) edges to this list in the
   *          (clockwise) order that they are encountered.
   * @param perimeterChunks
   *          Add chunks with bare (perimeter) edges to this set as they are
   *          encountered.
   */
  private void getBareEdgePerimeterRecursive(IceChunk curChunk, IceChunk prevChunk) {
    // TODO: make this not be recursive to avoid stack overflow on android
    if (!iceChunksCached.contains(curChunk)) {
      iceChunksCached.add(curChunk);
      iceChunksAndDeletedCached.add(curChunk);
      // prevChunk is only null if this is the very first chunk we're visiting.
      // In that case, the choice of starting edge is arbitrary.
      int firstEdge = prevChunk != null ? curChunk
          .getIndexOfEdgeAdjacent(prevChunk) : 0;
      // There are only 3 edges to check, but since we're starting from
      // prevChunk on the first edge, we'll need to come back to the first edge
      // again and check all the chunks up to prevChunk.
      for (int i = 0; i < 4; ++i) {
        int edgeInd = (firstEdge + i) % 3;
        TriangleEdge edge = curChunk.getTriangle().getOrderedEdge(edgeInd);
        // If there are no non-deleted chunks on the edge, it's the easiest kind
        // of bare edge: the entire edge is on the perimeter.
        if (!curChunk.hasAdjacentOnEdge(edgeInd)) {
          perimeterCached.add(edge.getFirst());
          perimeterChunksCached.add(curChunk);
          continue;
        }

        ArrayList<IceChunk> adjChunks = curChunk
            .getAdjacentAndDeletedSortedClockwise(edgeInd);

        // The first (i = 0) and last (i = 3) iterations are special.
        // The first iteration starts just after prevChunk, and the last one
        // goes up until prevChunk.
        int startInd = 0;
        int endInd = adjChunks.size();
        if (i == 0) {
          startInd = prevChunk != null ? adjChunks.indexOf(prevChunk) + 1 : 0;
        } else if (i == 3) {
          endInd = prevChunk != null ? adjChunks.indexOf(prevChunk) : 0;
        }
        for (int j = startInd; j < endInd; ++j) {
          IceChunk neighbor = adjChunks.get(j);
          int neighborInd = neighbor.getIndexOfEdgeAdjacent(curChunk);
          TriangleEdge neighborEdge = neighbor.getTriangle().getOrderedEdge(
              neighborInd);

          // If the neighbor is deleted, then (at least) the segment shared
          // with the neighbor is on the perimeter.
          if (neighbor.isDeleted()) {
            iceChunksAndDeletedCached.add(neighbor);
            perimeterChunksCached.add(curChunk);
            // Take either the endpoint of the neighbor edge, or the endpoint
            // of the chunk edge, whichever is the min in the clockwise-sense.
            Vec2 ab = edge.getDirection();
            float testX = neighborEdge.getSecond().x - edge.getFirst().x;
            float testY = neighborEdge.getSecond().y - edge.getFirst().y;
            if (testX * ab.x + testY * ab.y < 0) { // take the chunk edge endpoint
              perimeterCached.add(edge.getFirst());
            } else { // take the deleted chunk edge endpoint.
              perimeterCached.add(neighborEdge.getSecond());
            }
          } else {
            // Recurse on non-deleted neighbors.
            getBareEdgePerimeterRecursive(neighbor, curChunk);
          }
        }
      }
    }
  }
  
  
  /**
   * Helper method.
   * Compute cached data which the getBareEdgePerimeterRecursive DFS yields.
   * This should be called when isCachedDirty is true and needs to be set to
   * false.
   * ~O(n) for sheets of size n.
   */
  private void computeCachedPerimeter() {
    this.iceChunksCached.clear();
    this.iceChunksAndDeletedCached.clear();
    this.perimeterChunksCached.clear();
    this.perimeterCached.clear();
    getBareEdgePerimeterRecursive(rootChunk, null);
    this.isCachedDirty = false;
  }


  /**
   * Return the Collection of IceSheet's that result from removing a chunk from
   * this IceSheet. This IceSheet will still contain the same rootChunk, and may
   * represent an IceSheet in the output collection.
   * 
   * @param chunk
   *          IceChunk to remove.
   */
  /*
  public ArrayList<IceSheet> removeIceChunk(IceChunk chunk) {
    ArrayList<IceSheet> sheets = new ArrayList<IceSheet>();
    Collection<IceChunk> oldChunks = getIceChunks();
    chunk.setDeleted(true);
    oldChunks.remove(chunk);
    
    this.isCachedDirty = true;
    if (oldChunks.isEmpty()) {
      this.rootChunk = null;
    } else {
      this.rootChunk = oldChunks.iterator().next();
      oldChunks.removeAll(this.getIceChunks());
      sheets.add(this);
    }
    
    while (!oldChunks.isEmpty()) {
      IceChunk newRoot = oldChunks.iterator().next();
      IceSheet newSheet = new IceSheet(newRoot);
      newSheet.setMaxArea(maxArea);
      oldChunks.removeAll(newSheet.getIceChunks());
      sheets.add(newSheet);
    }
    return sheets;
  }
  */
  
  
  /**
   * Same as removeIceChunk, but remove all ice chunks in the given collectio
   * at once.
   * @param chunks
   * @return
   */
  public ArrayList<IceSheet> removeIceChunks(ArrayList<IceChunk> chunks) {
    ArrayList<IceSheet> sheets = new ArrayList<IceSheet>();
    HashSet<IceChunk> oldChunks = new HashSet<IceChunk>(this.getIceChunks());
    oldChunks.removeAll(chunks);
    for (IceChunk chunk : chunks) {
      chunk.setDeleted();
    }
    
    if (oldChunks.isEmpty()) {
      this.rootChunk = null;
      this.isCachedDirty = true;
      return sheets;
    } else {
      this.rootChunk = oldChunks.iterator().next();
      this.isCachedDirty = true;
      oldChunks.removeAll(this.getIceChunks());
      sheets.add(this);
    }
    
    while (!oldChunks.isEmpty()) {
      IceChunk newRoot = oldChunks.iterator().next();
      IceSheet newSheet = new IceSheet(newRoot);
      newSheet.setMaxArea(maxArea);
      oldChunks.removeAll(newSheet.getIceChunks());
      sheets.add(newSheet);
    }
    return sheets;
  }


  /**
   * Default JBox2D physics BodyDef for IceSheet bodies.
   * 
   * @return An IceSheet bodydef.
   */
  public static BodyDef getBodyDef() {
    BodyDef bd = new BodyDef();
    bd.linearDamping = FreeSpace.WATER_VISCOSITY;
    bd.angularDamping = FreeSpace.WATER_VISCOSITY;
    bd.type = BodyType.DYNAMIC;
    return bd;
  }


  /**
   * Add the IceSheet to the given World as a single physics body. 
   * 
   * Because Box2D doesn't like having butt tons of shapes on bodies (it makes
   * collision detection hard) we can't just add a shape and a fixture for each
   * particulate ice chunk. Instead, we only need to add the perimeter of the
   * sheet (assuming there are no holes) to get correct collision dynamics,
   * modulo a slightly inaccurate total mass and moment of inertia for the body.
   * 
   * @param world
   *          The world to use to create a new body
   * @return The newly created body
   */
  public Body addToWorldAsBody(World world) {
    BodyDef bd = getBodyDef();
    Body body = world.createBody(bd);
    body.setUserData(this);

    for (IceChunk chunk : this.getBareIceChunks()) {
      Fixture f = body.createFixture(chunk.getTriangle(),
          IceSheet.DENSITY);
      f.setUserData(chunk);
    }

    return body;
  }
}
