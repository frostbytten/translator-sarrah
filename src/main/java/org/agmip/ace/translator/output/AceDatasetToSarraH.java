package org.agmip.ace.translator.output;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.opencsv.CSVWriter;
import org.agmip.ace.*; //Eventually we will be touching all the components
import org.agmip.ace.translator.output.IFromAceDataset;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AceDatasetToSarraH implements IFromAceDataset {
  private static Logger LOG    = LoggerFactory.getLogger(AceDatasetToSarraH.class);
  //private static String HEADER = "#separateur|\t;date_format|dd/MM/yyyy;heure_format|hh:mm:ss;float_separateur|.;\n";
  //private static final String[] STATION_HEADER = {"CodePays", "Code", "Nom", "Latitude", "Longitude", "Altitude", "CodeTypeStation"};
  private static final String[] METEOROLOGIE_HEADER = {"CodeStation", "Jour", "TMax", "TMin", "TMoy", "HMax", "HMin", "HMoy", "Vt", "Ins", "Rg", "ETO"};
  private static final String[] PLUVIOMETRIE_HEADER = {"CodeStation", "Jour", "Pluie"};
  private static final DateTimeFormatter dtfIn = ISODateTimeFormat.basicDate();
  private static final DateTimeFormatter dtfOut = DateTimeFormat.forPattern("dd/MM/yyyy");


  private AceDatasetToSarraH() {} // Cannot initialize this class

  public static void write(AceDataset ds, Path baseDir) {
    Optional<Path> destDir = mkDestDirectory(baseDir, "SarraH");
    if (destDir.isPresent()) {
      List<AceWeather> stations = ds.getWeathers();
      if (stations.size() > 0) {
        writeWeatherFiles(stations, destDir.get());
      }
    }
  }

  /* This method should be promoted to agmip-commons */
  public static Optional<Path> mkDestDirectory(Path baseDir, String newDirName) {
    try {
      Path newDir = baseDir.toAbsolutePath().resolve(newDirName);
      int i = 1;
      while(Files.exists(newDir)) {
        newDir = baseDir.toAbsolutePath().resolve(new StringBuilder(newDirName)
            .append("-").append(i).toString());
        i++;
      }
      Files.createDirectories(newDir);
      return Optional.of(newDir);
    } catch (Exception ex) {
      LOG.error("Unable to create destination directory: {}", ex.getMessage());
      return Optional.empty();
    }
  }

  private static void writeWeatherFiles(List<AceWeather> stations, Path destDir) {
    for(AceWeather station : stations) {
      writeWeatherStationFile(station, destDir);
      writeDailyWeatherFile(station, destDir);
      writeDailyRainfallFile(station, destDir);
    }
  }

  protected static Optional<Path> generateFileName(String prefix, AceWeather station, Path destDir) {
    try {
      String wstid = station.getValueOr("wst_id", "UNKNOWN");
      String wid = station.getValueOr("wid", "UNKNOWN");
      if (wstid.equals("UNKNOWN")) {
        LOG.error("Cannot write file for unknown wst_id with wid {}", wid);
        return Optional.empty();
      } else {
        String fileName = new StringBuffer(prefix)
          .append("_").append(wstid).append(".txt")
          .toString();
        Path file = destDir.resolve(fileName);
        return Optional.of(file);
      }
    } catch (IOException ex) {
      LOG.error("An unknown IO error occured: {}", ex.getMessage());
      return Optional.empty();
    }
  }

  protected static void writeWeatherStationFile(AceWeather station, Path destDir) {
    try {
      Optional<Path> file = generateFileName("Station", station, destDir);
      if (file.isPresent()) {
        LOG.debug("Writing to file {}", file);
        try (Writer writer = openUTF8FileForWrite(file.get());) {
          // CSVWriter csvwrite = new CSVWriter(writer, '\t');) {
          // Write first line of file (file design specification)
          //writer.write(HEADER);
          // Using the same Writer write TSV for data
          //csvwrite.writeNext(STATION_HEADER, false);
          //List<String> values = new ArrayList<String>();
          //values.add(station.getValueOr("wst_loc_1", ""));
          //values.add(station.getValueOr("wst_id", ""));
          //values.add(station.getValueOr("wst_name", ""));
          //values.add(station.getValueOr("wst_lat", ""));
          //values.add(station.getValueOr("wst_long", ""));
          //values.add(station.getValueOr("wst_elev", ""));
          //values.add(""); //CodeTypeStation not applicable now
          //csvwrite.writeNext(values.toArray(new String[0]), false);
          StringBuffer sb = new StringBuffer();
          sb.append("The ECOTROP Platform (SarraH v3.x and prior) does not allow adding of ");
          sb.append("weather station through files. Please add a new Weather station with ");
          sb.append("the following information:\n\n");
          sb.append("CodePays:   ");
          sb.append(station.getValueOr("wst_loc_1", ""));
          sb.append("\nCode:       ");
          sb.append(station.getValueOr("wst_id", ""));
          sb.append("\nNom:        ");
          sb.append(station.getValueOr("wst_name", ""));
          sb.append("\nLatitude:   ");
          sb.append(station.getValueOr("wst_lat", "-999"));
          sb.append("\nLongitude:  ");
          sb.append(station.getValueOr("wst_long", "-999"));
          sb.append("\nAltitude:   ");
          sb.append(station.getValueOr("wst_elev", "-999"));
          writer.write(sb.toString());
        }
      }
    } catch(IOException ex) {
      LOG.error("An error occured writing a station file: {}", ex.getMessage());
    }
  }

  protected static void writeDailyWeatherFile(AceWeather station, Path destDir) {
    try {
      Optional<Path> file = generateFileName("Meteorologie", station, destDir);
      if (file.isPresent()) {
        try (Writer writer = openUTF8FileForWrite(file.get());
            CSVWriter csvwrite = new CSVWriter(writer, '\t');) {
          //writer.write(HEADER);
          String wstid = station.getValueOr("wst_id", "");
          csvwrite.writeNext(METEOROLOGIE_HEADER, false);
          for( AceRecord rec : station.getDailyWeather()) {
            String wdate = rec.getValueOr("w_date", "");
            String wDateVal = "";
            if (! wdate.equals("")) {
              DateTime wDate = dtfIn.parseDateTime(wdate);
              wDateVal = dtfOut.print(wDate);
            }
            String wind = rec.getValueOr("wind", "");
            String windVal = "";
            if (! wind.equals("")) {
              windVal = Double.toString(Double.parseDouble(wind)/86.4);
            }

            List<String> values = new ArrayList<String>();
            values.add(wstid);
            values.add(wDateVal); //Needs conversion
            values.add(rec.getValueOr("tmax", ""));
            values.add(rec.getValueOr("tmin", ""));
            values.add(rec.getValueOr("tavd", ""));
            values.add(rec.getValueOr("rhuxd", ""));
            values.add(rec.getValueOr("rhumd", ""));
            values.add(rec.getValueOr("sarrah_hmoy", ""));
            values.add(windVal); //Needs conversion
            values.add(rec.getValueOr("sunh", ""));
            values.add(rec.getValueOr("srad", ""));
            values.add(rec.getValueOr("eto", ""));
            csvwrite.writeNext(values.toArray(new String[0]), false);
          }
            }
      }
    } catch (IOException ex) {
      LOG.error("An error occured writing a daily file: {}", ex.getMessage());
    }
  }

  protected static void writeDailyRainfallFile(AceWeather station, Path destDir) {
    try {
      Optional<Path> file = generateFileName("Pluviometrie", station, destDir);
      if (file.isPresent()) {
        try (Writer writer = openUTF8FileForWrite(file.get());
            CSVWriter csvwrite = new CSVWriter(writer, '\t');) {
          //writer.write(HEADER);
          String wstid = station.getValueOr("wst_id", "");
          csvwrite.writeNext(PLUVIOMETRIE_HEADER, false);
          for( AceRecord rec : station.getDailyWeather()) {
            String wdate = rec.getValueOr("w_date", "");
            String wDateVal = "";
            if (! wdate.equals("")) {
              DateTime wDate = dtfIn.parseDateTime(wdate);
              wDateVal = dtfOut.print(wDate);
            }
            List<String> values = new ArrayList<String>();
            values.add(wstid);
            values.add(wDateVal); //Needs conversion
            values.add(rec.getValueOr("rain", ""));
            csvwrite.writeNext(values.toArray(new String[0]), false);
          }
            }
      }
    } catch (IOException ex) {
      LOG.error("An error occured writing a daily file: {}", ex.getMessage());
    }
  }

  /* This method should be promoted to agmip-commons */
  public static Writer openUTF8FileForWrite(Path file) throws IOException {
    return new OutputStreamWriter(
        new FileOutputStream(file.toFile()),
        StandardCharsets.UTF_8);
  }
}
