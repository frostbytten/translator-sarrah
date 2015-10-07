package org.agmip.ace.translator.output;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import org.agmip.ace.AceDataset;
import org.agmip.ace.AceWeather;
import org.agmip.ace.AceRecordCollection;
import org.agmip.ace.io.AceParser;
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
    URL resource = WriteStationFileTest.class.getResource("./");
    System.out.println(resource);
    baseDir = Paths.get(resource.getPath());
  }

  @Test
  public void failUTF8FileOnDirectory() throws IOException {
    Path testDir = baseDir.resolve("Test");
    thrown.expect(FileNotFoundException.class);

    AceDatasetToSarraH.openUTF8FileForWrite(testDir);
  }

  @Test
  public void writeSampleFiles() {
    Path testFile = baseDir.resolve("sample.aceb");
    try {
      AceDataset ds = AceParser.parseACEB(testFile.toFile());
      AceWeather testW = ds.getWeathers().get(0);
      AceDatasetToSarraH.writeWeatherStationFile(testW, baseDir);
      AceDatasetToSarraH.writeDailyWeatherFile(testW, baseDir);
      AceDatasetToSarraH.writeDailyRainfallFile(testW, baseDir);
    } catch(IOException ex) {
      fail("An unknown IOException occured: "+ex.getMessage());
    }
  }

  @AfterClass
  public static void cleanUp() {
    Path testFile1 = baseDir.resolve("Station_2.txt");
    Path testFile2 = baseDir.resolve("Meteorologie_2.txt");
    Path testFile3 = baseDir.resolve("Pluviometrie_2.txt");
    //Files.deleteIfExists(testFile1);
  }
}
