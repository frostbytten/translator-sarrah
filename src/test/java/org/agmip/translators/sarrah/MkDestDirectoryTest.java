package org.agmip.translators.sarrah;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Optional;
import static org.agmip.translators.sarrah.WriteStationFileTest.baseDir;

//import org.agmip.ace.translator.output.AceDatasetToSarraH;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class MkDestDirectoryTest {
  static Path baseDir;
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void setUp() {
    URL resource = MkDestDirectoryTest.class.getResource("./");
//    System.out.println(resource);
//    baseDir = Paths.get(resource.getPath());
    System.out.println(resource.getPath().substring(1));
    baseDir = Paths.get(resource.getPath().substring(1));
  }

//  @Test
//  public void createNewUnnestedDirectory() {
//    assertNotNull("baseDir should not be null", baseDir);
//    Optional<Path> newDir = AceDatasetToSarraH.mkDestDirectory(baseDir, "SarraH");
//    Path testDir = baseDir.resolve("SarraH");
//    assertNotNull("newDir should not be null", newDir.get());
//    assertEquals("testDir and newDir should be the same", newDir.get(), testDir);
//  }
//
//  @Test
//  public void createNew1Directory() {
//    assertNotNull("baseDir should not be null", baseDir);
//    Optional<Path> newDir = AceDatasetToSarraH.mkDestDirectory(baseDir, "SarraH");
//    Path testDir = baseDir.resolve("SarraH-1");
//    assertNotNull("newDir should not be null", newDir.get());
//    assertTrue("newDir should exist.", Files.exists(newDir.get()));
//    assertEquals("testDir and newDir should be the same", newDir.get(), testDir);
//  }
//
//  @Test
//  public void createExistingDirectory() {
//    assertNotNull("baseDir should not be null", baseDir);
//    Path testDir = baseDir.resolve("Test-2");
//    assertFalse("newDir should not exist yet.", Files.exists(testDir));
//    Optional<Path> newDir = AceDatasetToSarraH.mkDestDirectory(baseDir, "Test");
//    assertNotNull("newDir should not be null", newDir.get());
//    assertTrue("newDir should exist", Files.exists(newDir.get()));
//    assertEquals("testDir and newDir should be the same", newDir.get(), testDir);
//  }

  @AfterClass
  public static void cleanUp() throws Exception {
    Path newDir1 = baseDir.resolve("SarraH");
    Path newDir2 = baseDir.resolve("SarraH-1");
    Path newDir3 = baseDir.resolve("Test-2");
    Files.deleteIfExists(newDir1);
    Files.deleteIfExists(newDir2);
    Files.deleteIfExists(newDir3);
  }
}
