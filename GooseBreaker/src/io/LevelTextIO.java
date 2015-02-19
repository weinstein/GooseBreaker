package io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import proto.LevelProto;

import com.google.protobuf.TextFormat;

public class LevelTextIO {
  
  /**
   * Parse a LevelProto.Level protobuf in plaintext-format from the given
   * input stream and return it, or null on IO exception.
   * @param is
   * @return
   */
  public static LevelProto.Level parseTextFormatLevelProtoOrNull(InputStream is) {
    LevelProto.Level.Builder levelBuilder = LevelProto.Level.newBuilder();
    try {
      TextFormat.merge(new InputStreamReader(is), levelBuilder);
      return levelBuilder.build();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static LevelProto.Level parseBinaryFormatLevelProtoOrNull(InputStream is) {
    LevelProto.Level.Builder levelBuilder = LevelProto.Level.newBuilder();
    try {
      levelBuilder.mergeFrom(is);
      return levelBuilder.build();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static String printTextFormatLevelProto(LevelIO level) {
    LevelProto.Level levelProto = level.toProto();
    return TextFormat.printToString(levelProto);
  }
}
