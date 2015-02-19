package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.google.protobuf.TextFormat;

import proto.LevelProto;

public class LevelProtoTextToBin {
  public static void main(String[] args) throws IOException {
    String inputDirStr = args[0];
    File inputDir = new File(inputDirStr);
    File[] inputFiles = inputDir.listFiles();
    String outputPath = args[1];
    
    for (File inFile : inputFiles) {
      if (!inFile.isFile()) {
        continue;
      }
      String fName = inFile.getName();
      
      InputStream is = new FileInputStream(inFile);
      LevelProto.Level.Builder levelBuilder = LevelProto.Level.newBuilder();
      TextFormat.merge(new InputStreamReader(is), levelBuilder);
      
      String outFilePath = outputPath + "/" + fName + ".bin";
      OutputStream os = new FileOutputStream(outFilePath);
      levelBuilder.build().writeTo(os);
      System.out.println(fName + " -> " + outFilePath);
    }
  }
}
