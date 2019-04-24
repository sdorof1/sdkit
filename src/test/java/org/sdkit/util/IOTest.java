package org.sdkit.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;
import org.sdkit.util.IO;

public class IOTest extends Assert {

  @Test
  public void array() throws IOException {

    double[] arr = new double[] {1, 2, 4, 5, 7, 8};

    Path fname = Files.createTempFile("io-", ""); //$NON-NLS-1$ //$NON-NLS-2$

    IO.saveArray(fname, arr);
    assertTrue(Files.exists(fname));

    double[] actuals = IO.readArray(fname);
    assertArrayEquals(arr, actuals, 0);
  }

  // FIXME: test number of columns

  @Test
  public void array2D() throws IOException {

    double[] arr = new double[] {1, 2, 4, 5, 7, 8};

    double[][] arr2D = new double[][] {
        {1, 2},
        {4, 5},
        {7, 8}
    };

    Path fname = Files.createTempFile("io-", ""); //$NON-NLS-1$ //$NON-NLS-2$

    IO.saveArray(fname, arr2D);
    assertTrue(Files.exists(fname));

    double[] actuals = IO.readArray(fname);
    assertArrayEquals(arr, actuals, 0);
  }
}
