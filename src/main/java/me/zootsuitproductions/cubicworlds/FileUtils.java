package me.zootsuitproductions.cubicworlds;

import static me.zootsuitproductions.cubicworlds.CubicWorlds.creatingWorldStateFileName;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
  public static void deleteFolder(File folder) {
    File[] files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteFolder(file);
          System.out.println("Deleted the folder");
        } else {
          file.delete();
        }
      }
    }
    folder.delete();
  }


  public static void deleteFile(String filePath) {
    File file = new File(filePath);

    if (file.exists()) {
      boolean deleted = file.delete();

      if (deleted) {
        System.out.println("File deleted successfully.");
      } else {
        System.out.println("Failed to delete the file.");
      }
    } else {
      System.out.println("File does not exist.");
    }
  }

  public static void createAndWriteFile(String filename, String content) {
    try {
      // Create the file if it doesn't exist
      Path filePath = Paths.get(filename);
      if (!Files.exists(filePath)) {
        Files.createFile(filePath);
        System.out.println("File created: " + filePath.toAbsolutePath());
      }

      // Write content to the file
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
        writer.write(content);
        System.out.println("Content written to the file successfully.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
