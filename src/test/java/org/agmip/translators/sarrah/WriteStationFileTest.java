package org.agmip.translators.sarrah;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.agmip.ace.AceDataset;
import org.agmip.ace.io.AceParser;
//import static org.agmip.ace.translator.output.AceDatasetToSarraH.writeDailyWeatherFile;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WriteStationFileTest {
  static Path baseDir;
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void setUp() {
    URL resource = WriteStationFileTest.class.getResource("./output");
    System.out.println(resource.getPath().substring(1));
    baseDir = Paths.get(resource.getPath().substring(1));
  }

  @Test
  public void failUTF8FileOnDirectory() throws IOException {
    Path testDir = baseDir.resolve("Test");
    thrown.expect(FileNotFoundException.class);

    TransUtil.openUTF8FileForWrite(testDir);
  }

  @Test
  public void writeSampleFiles() {
    Path testFile = baseDir.resolve("Machakos-MAZ-0XFX.aceb");
    try {
      AceDataset ds = AceParser.parseACEB(testFile.toFile());
      ds.linkDataset();
      AceDatasetToSarraH.write(ds, baseDir);
    } catch(IOException ex) {
      fail("An unknown IOException occured: "+ex.getMessage());
    }
  }

  @AfterClass
  public static void cleanUp() {
    //Path testFile1 = baseDir.resolve("Station_2.txt");
    //Path testFile2 = baseDir.resolve("Meteorologie_2.txt");
    //Path testFile3 = baseDir.resolve("Pluviometrie_2.txt");
    //Files.deleteIfExists(testFile1);
  }
}
