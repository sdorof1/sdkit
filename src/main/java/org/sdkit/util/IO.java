package org.sdkit.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IO {

  public static double[] readArray(Path path, String delimiter) throws IOException {
    ArrayList<Double> arr = new ArrayList<>();
    try (Stream<String> lines = Files.lines(path)) {

      lines.filter(line -> !line.isEmpty() && !line.startsWith("#")).forEach(line -> {
        arr.addAll(Arrays.stream(line.split(delimiter)).map(val -> Double.parseDouble(val))
            .collect(Collectors.toList()));
      });
    }
    return arr.stream().mapToDouble(d -> d).toArray();
  }

  public static double[] readArray(Path path) throws IOException {
    return readArray(path, " ");
  }

  public static void saveArray(Path fname, double[][] arr, String delimiter) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fname.toFile()))) {
      int nbRows = arr.length;
      int nbCols = arr[0].length;
      for (int row = 0; row < nbRows; ++row) {
        for (int col = 0; col < nbCols; ++col) {
          writer.write(String.valueOf(arr[row][col]) + (col < nbCols - 1 ? delimiter : ""));
        }
        writer.newLine();
      }
    }
  }

  public static void saveArray(Path fname, double[][] arr) throws IOException {
    saveArray(fname, arr, " ");
  }

  public static void saveArray(Path fname, String header, double[] arr, int nbCols, String delimiter)
      throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fname.toFile()))) {
      if (header != null) {
        writer.write(header);
        writer.newLine();
      }
      for (int i = 0; i < arr.length; ++i) {
        writer.write(String.valueOf(arr[i]));
        if ((i + 1) % nbCols != 0) {
          writer.write(delimiter);
        } else {
          writer.newLine();
        }
      }
    }
  }

  public static void saveArray(Path fname, String header, double[] arr) throws IOException {
    saveArray(fname, header, arr, 2, " ");
  }

  public static void saveArray(Path fname, double[] arr) throws IOException {
    saveArray(fname, null, arr);
  }

  public static Path createTempDirectory(String prefix, boolean deleteOnExit) throws IOException {
    Path path = Files.createTempDirectory(prefix);
    if (deleteOnExit) {
      recursiveDeleteOnShutdownHook(path);
    }
    return path;
  }

  public static void recursiveDeleteOnShutdownHook(final Path path) {
    Runtime.getRuntime().addShutdownHook(new Thread(
        new Runnable() {
          @Override
          public void run() {
            try {
              Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                  Files.delete(file);
                  return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException {
                  if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                  }
                  // directory iteration failed
                  throw e;
                }
              });
            } catch (IOException e) {
              throw new RuntimeException("Failed to delete " + path, e);
            }
          }
        }));
  }
}
