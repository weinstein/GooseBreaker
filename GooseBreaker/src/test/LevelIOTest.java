package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import io.LevelIO;

import org.junit.Test;

import proto.LevelProto;
import tessellation.IceChunk;
import tessellation.IceSheet;

import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

public class LevelIOTest {

  @Test
  public void testParseSerializedProto() throws ParseException {
    // TODO: move this into a text file
    String data = 
        "sheet {\n"
        + "max_area: 0.1\n"
        + "piece_of_ice {\n"
        + "id: 0\n"
        + "is_deleted: false\n"
          + "triangle {\n"
            + "pt1 {\n"
              + "x: 0\n"
              + "y: 0\n"
            + "}\n"
            + "pt2 {\n"
              + "x: 1\n"
              + "y: 0\n"
            + "}\n"
            + "pt3 {\n"
              + "x: 0\n"
              + "y: 1\n"
            + "}\n"
          + "}\n"
        + "}\n"
        + "piece_of_ice {\n"
        + "id: 1\n"
        + "is_deleted: false\n"
          + "triangle {\n"
            + "pt1 {\n"
              + "x: 1\n"
              + "y: 0\n"
            + "}\n"
            + "pt2 {\n"
              + "x: 0\n"
              + "y: 1\n"
            + "}\n"
            + "pt3 {\n"
              + "x: 1\n"
              + "y: 1\n"
            + "}\n"
          + "}\n"
        + "}\n"
        + "adjacency {\n"
        + "ice_triangle_a: 0\n"
        + "edge_index_a: 1\n"
        + "ice_triangle_b: 1\n"
        + "edge_index_b: 0\n"
        + "}\n"
        + "}\n";
    LevelProto.Level.Builder builder = LevelProto.Level.newBuilder();
    TextFormat.merge(data, builder);
    LevelIO levelIO = LevelIO.fromProto(builder.build());
    assertEquals(levelIO.getIceSheets().size(), 1);
    IceSheet sheet = levelIO.getIceSheets().iterator().next();
    assertEquals(sheet.getMaxArea(), 0.1f, 1e-8f);
    assertEquals(sheet.getIceChunksCopy().size(), 2);
    for (IceChunk chunk : sheet.getIceChunksCopy()) {
      assertFalse(chunk.isDeleted());
    }
  }

}
