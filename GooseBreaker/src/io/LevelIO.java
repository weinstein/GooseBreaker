package io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;

import controller.Unlockable;
import proto.LevelProto;
import tessellation.IceChunk;
import tessellation.IceSheet;
import tessellation.Triangle;

/**
 * 
 * The LevelIO class aids in the serialization/deserialization of "levels" using
 * Google protobufs.
 * 
 * @author Jack
 */
public class LevelIO {
  private Collection<IceSheet> iceSheets;
  private AABB bounds;
  private Collection<LevelProto.GoosePlacement> goosePlacements;
  private float breadSupply;
  private long limitMillis;
  private int numIceSheetsGoal;
  
  private String prereqLevel;
  private String prereqMode;
  private int prereqScore;
  
  public LevelIO() {
    iceSheets = new ArrayList<IceSheet>();
    bounds = new AABB();
    goosePlacements = new ArrayList<LevelProto.GoosePlacement>();
  }
  
  public Collection<IceSheet> getIceSheets() {
    return iceSheets;
  }
  
  public Collection<LevelProto.GoosePlacement> getGoosePlacements() {
    return goosePlacements;
  }
  
  public void setBounds(AABB bounds) {
    this.bounds = bounds;
  }
  
  public AABB getBounds() {
    return bounds;
  }
  
  public float getBreadSupply() {
    return breadSupply;
  }
  
  /**
   * Reconstruct LevelIO data from a Level protobuf.
   * @param level
   * @return
   */
  public static LevelIO fromProto(LevelProto.Level level) {
    LevelIO levelIO = new LevelIO();
    
    for (LevelProto.IceSheet sheet : level.getSheetList()) {
      IceSheet parsed = LevelIO.protoToSheet(sheet);
      if (parsed != null) {
        levelIO.iceSheets.add(parsed);
      }
    }
    if (level.hasBounds()) {
      levelIO.bounds = LevelIO.protoToBounds(level.getBounds());
    }
    levelIO.goosePlacements.clear();
    levelIO.goosePlacements.addAll(level.getGooseList());
    if (level.hasBreadSupply()) {
      levelIO.breadSupply = level.getBreadSupply();
    }
    if (level.hasTimeLimitMs()) {
      levelIO.limitMillis = level.getTimeLimitMs();
    }
    if (level.hasNumIceSheetsGoal()) {
      levelIO.numIceSheetsGoal = level.getNumIceSheetsGoal();
    }
    if (level.hasUnlocksAfter()) {
      levelIO.prereqLevel = level.getUnlocksAfter().getLevel();
      levelIO.prereqMode = level.getUnlocksAfter().getMode();
      levelIO.prereqScore = level.getUnlocksAfter().getScore();
    }
    return levelIO;
  }

  /**
   * Construct a LevelProto.Level protobuf with the current LevelIO data.
   * @return
   */
  public LevelProto.Level toProto() {
    LevelProto.Level.Builder levelBuilder = LevelProto.Level.newBuilder();
    for (IceSheet sheet : iceSheets) {
      levelBuilder.addSheet(sheetToProto(sheet));
    }
    if (bounds != null) {
      levelBuilder.setBounds(boundsToProto(bounds));
    }
    levelBuilder.addAllGoose(this.goosePlacements);
    levelBuilder.setBreadSupply(this.breadSupply);
    levelBuilder.setTimeLimitMs(this.limitMillis);
    levelBuilder.setNumIceSheetsGoal(this.numIceSheetsGoal);
    
    LevelProto.UnlockConditions.Builder unlockCondBuilder = LevelProto.UnlockConditions.newBuilder();
    unlockCondBuilder.setLevel(this.prereqLevel);
    unlockCondBuilder.setMode(this.prereqMode);
    unlockCondBuilder.setScore(this.prereqScore);
    levelBuilder.setUnlocksAfter(unlockCondBuilder);
    
    return levelBuilder.build();
  }

  /**
   * Construct a LevelProto.Rect protobuf from the given AABB.
   * @param bounds
   * @return
   */
  public static LevelProto.Rect boundsToProto(AABB bounds) {
    LevelProto.Rect.Builder rectBuilder = LevelProto.Rect.newBuilder();
    rectBuilder.setLowerBound(vec2ToProto(bounds.lowerBound));
    rectBuilder.setUpperBound(vec2ToProto(bounds.upperBound));
    return rectBuilder.build();
  }
  
  /**
   * Reconstruct an AABB from the given LevelProto.Rect protobuf.
   * @param bounds
   * @return
   */
  public static AABB protoToBounds(LevelProto.Rect bounds) {
    AABB boundBox = new AABB();
    boundBox.lowerBound.set(protoToVec2(bounds.getLowerBound()));
    boundBox.upperBound.set(protoToVec2(bounds.getUpperBound()));
    return boundBox;
  }

  /**
   * Construct a LevelProto.IceSheet protobuf given an IceSheet.
   * @param sheet
   * @return
   */
  public static LevelProto.IceSheet sheetToProto(IceSheet sheet) {
    LevelProto.IceSheet.Builder sheetBuilder = LevelProto.IceSheet.newBuilder();
    sheetBuilder.setMaxArea(sheet.getMaxArea());
    Map<IceChunk, Integer> chunkToId = new HashMap<IceChunk, Integer>();
    int numChunks = 0;
    for (IceChunk chunk : sheet.getIceChunksIncludingDeletedCopy()) {
      chunkToId.put(chunk, numChunks++);
    }
    for (IceChunk chunk : sheet.getIceChunksIncludingDeletedCopy()) {
      sheetBuilder.addPieceOfIce(chunkToProto(chunk, chunkToId.get(chunk)));
      for (int i = 0; i < 3; ++i) {
        Collection<IceChunk> adjChunks = chunk.getAdjacentAndDeletedOnEdge(i);
        for (IceChunk adjChunk : adjChunks) {
          LevelProto.Adjacency.Builder adjBuilder = LevelProto.Adjacency.newBuilder();
          adjBuilder.setIceTriangleA(chunkToId.get(chunk));
          adjBuilder.setIceTriangleB(chunkToId.get(adjChunk));
          adjBuilder.setEdgeIndexA(i);
          adjBuilder.setEdgeIndexB(adjChunk.getIndexOfEdgeAdjacent(chunk));
          sheetBuilder.addAdjacency(adjBuilder.build());
        }
      }
    }
    return sheetBuilder.build();
  }

  /**
   * Construct a LevelProto.IceTriangle protobuf from the given IceChunk and the
   * (sheet-unique) integer identifier.
   * @param chunk
   * @param id
   * @return
   */
  public static LevelProto.IceTriangle chunkToProto(IceChunk chunk, Integer id) {
    LevelProto.IceTriangle.Builder chunkBuilder = LevelProto.IceTriangle.newBuilder();
    chunkBuilder.setTriangle(triangleToProto(chunk.getTriangle()));
    chunkBuilder.setId(id);
    chunkBuilder.setIsDeleted(chunk.isDeleted());
    return chunkBuilder.build();
  }
  
  /**
   * Construct a LevelProto.Triangle protobuf from the given Triangle.
   * @param t
   * @return
   */
  public static LevelProto.Triangle triangleToProto(Triangle t) {
    LevelProto.Triangle.Builder triangleBuilder = LevelProto.Triangle.newBuilder();
    triangleBuilder.setPt1(vec2ToProto(t.getOrderedVertex(0)));
    triangleBuilder.setPt2(vec2ToProto(t.getOrderedVertex(1)));
    triangleBuilder.setPt3(vec2ToProto(t.getOrderedVertex(2)));
    return triangleBuilder.build();
  }
  
  /**
   * Construct a LevelProto.Point from the given Vec2.
   * @param v
   * @return
   */
  public static LevelProto.Point vec2ToProto(Vec2 v) {
    LevelProto.Point.Builder ptBuilder = LevelProto.Point.newBuilder();
    ptBuilder.setX(v.x);
    ptBuilder.setY(v.y);
    return ptBuilder.build();
  }
  
  /**
   * Reconstruct a Vec2 from the given LevelProto.Point protobuf.
   * @param pt
   * @return
   */
  public static Vec2 protoToVec2(LevelProto.Point pt) {
    return new Vec2(pt.getX(), pt.getY());
  }
  
  /**
   * Reconstruct a Triangle from the given LevelProto.Triangle protobuf.
   * @param t
   * @return
   */
  public static Triangle protoToTriangle(LevelProto.Triangle t) {
    return new Triangle(
        protoToVec2(t.getPt1()),
        protoToVec2(t.getPt2()),
        protoToVec2(t.getPt3()));
  }
  
  /**
   * Reconstruct an IceSheet from the given LevelProto.IceSheet protobuf.
   * @param protoSheet
   * @return
   */
  public static IceSheet protoToSheet(LevelProto.IceSheet protoSheet) {
    ArrayList<IceChunk> chunks = new ArrayList<IceChunk>();
    for (int i = 0; i < protoSheet.getPieceOfIceCount(); ++i) {
      chunks.add(null);
    }
    
    for (LevelProto.IceTriangle protoTriangle : protoSheet.getPieceOfIceList()) {
      int id = protoTriangle.getId();
      Triangle t = protoToTriangle(protoTriangle.getTriangle());
      IceChunk chunk = new IceChunk(t);
      if (protoTriangle.getIsDeleted()) {
        chunk.setDeleted();
      }
      chunks.set(id, chunk);
    }
    for (LevelProto.Adjacency adj : protoSheet.getAdjacencyList()) {
      int idA = adj.getIceTriangleA();
      int idB = adj.getIceTriangleB();
      IceChunk chunkA = chunks.get(idA);
      IceChunk chunkB = chunks.get(idB);
      if (chunkA.getIndexOfEdgeAdjacent(chunkB) < 0) {
        chunkA.addAdjacentOnEdge(adj.getEdgeIndexA(), chunkB, adj.getEdgeIndexB());
      }
    }
    
    for (IceChunk chunk : chunks) {
      if (!chunk.isDeleted()) {
        IceSheet sheet = new IceSheet(chunk);
        if (protoSheet.hasMaxArea()) {
          sheet.setMaxArea(protoSheet.getMaxArea());
        }
        return sheet;
      }
    }
    return null;
  }

  public long getTimeLimitMillis() {
    return this.limitMillis;
  }

  public int getNumIceSheetsGoal() {
    return this.numIceSheetsGoal;
  }
}
