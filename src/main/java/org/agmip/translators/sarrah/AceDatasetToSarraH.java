package org.agmip.translators.sarrah;

//import java.io.OutputStreamWriter;
import java.io.Writer;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.math.BigDecimal;
//import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//import com.opencsv.CSVWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import org.agmip.ace.*; //Eventually we will be touching all the components
import org.agmip.common.Functions;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
//import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AceDatasetToSarraH implements IFromAceDataset {

    private static final Logger LOG = LoggerFactory.getLogger(AceDatasetToSarraH.class);
    //private static String HEADER = "#separateur|\t;date_format|dd/MM/yyyy;heure_format|hh:mm:ss;float_separateur|.;\n";
    //private static final String[] STATION_HEADER = {"CodePays", "Code", "Nom", "Latitude", "Longitude", "Altitude", "CodeTypeStation"};
//  private static final String[] METEOROLOGIE_HEADER = {"CodeStation", "Jour", "TMax", "TMin", "TMoy", "HMax", "HMin", "HMoy", "Vt", "Ins", "Rg", "ETO"};
//  private static final String[] PLUVIOMETRIE_HEADER = {"CodeStation", "Jour", "Pluie"};
//  private static final DateTimeFormatter dtfIn = ISODateTimeFormat.basicDate();
//  private static final DateTimeFormatter dtfOut = DateTimeFormat.forPattern("dd/MM/yyyy");
//  private static final BigDecimal windConversion = new BigDecimal(86.4);

    private AceDatasetToSarraH() {
    } // Cannot initialize this class

//  public static void write(AceDataset ds, Path baseDir) {
//    Optional<Path> destDir = mkDestDirectory(baseDir, "SarraH");
//    if (destDir.isPresent()) {
//      List<AceWeather> stations = ds.getWeathers();
//      if (stations.size() > 0) {
//        writeWeatherFiles(stations, destDir.get());
//      }
//    }
//  }
//
//  /* This method should be promoted to agmip-commons */
//  /* This is actually handled by QuadUI, but it is fine to included in the commons - Meng */
//  public static Optional<Path> mkDestDirectory(Path baseDir, String newDirName) {
//    try {
//      Path newDir = baseDir.toAbsolutePath().resolve(newDirName);
//      int i = 1;
//      while(Files.exists(newDir)) {
//        newDir = baseDir.toAbsolutePath().resolve(new StringBuilder(newDirName)
//            .append("-").append(i).toString());
//        i++;
//      }
//      Files.createDirectories(newDir);
//      return Optional.of(newDir);
//    } catch (Exception ex) {
//      LOG.error("Unable to create destination directory: {}", ex.getMessage());
//      return Optional.empty();
//    }
//  }
    public static void write(AceDataset ace, Path outputDir) {
        if (!Files.exists(outputDir)) {
            try {
                Files.createDirectories(outputDir);
            } catch (IOException ex) {
                Functions.getStackTrace(ex);
                return;
            }
        }
        String fileNameExt = getFileNameExt(ace);
        writeWeatherFiles(ace.getWeathers(), outputDir, fileNameExt);
        writeSoilFiles(ace.getSoils(), outputDir, fileNameExt);
        writeExperimentFiles(ace.getExperiments(), outputDir, fileNameExt);

    }

    protected static void writeWeatherFiles(List<AceWeather> stations, Path destDir, String fileNameExt) {

        if (stations.isEmpty()) {
            return;
        }
        List<AceDataAdaptor> dataList = new ArrayList();
        for (AceWeather station : stations) {
            dataList.add(new AceWeatherAdaptor(station));
        }
        writeWeatherFile(dataList, destDir, "Meteorologie", fileNameExt);
        writeWeatherFile(dataList, destDir, "Pluviometrie", fileNameExt);
        writeWeatherFile(dataList, destDir, "Site", fileNameExt);
        writeWeatherFile(dataList, destDir, "Station", fileNameExt);
    }

    protected static void writeWeatherFile(List<AceDataAdaptor> stations, Path destDir, String fileType, String fileNameExt) {

        Optional<Path> file = generateFileName(fileType, destDir, fileNameExt);
        if (file.isPresent()) {
            LOG.debug("Writing to file {}", file);
            try (Writer writer = TransUtil.openUTF8FileForWrite(file.get());) {
                Velocity.init();
                VelocityContext context = new VelocityContext();
                Reader R = new InputStreamReader(AceDatasetToSarraH.class.getClassLoader().getResourceAsStream("Wth_" + fileType + ".template"));

                context.put("weathers", stations);
                context.put("util", new TransUtil());
                Velocity.evaluate(context, writer, "Generate " + fileType, R);
                writer.close();
            } catch (IOException ex) {
                LOG.error("An error occured writing a {} file: {}", fileType, ex.getMessage());
            }
        }
    }

    protected static void writeSoilFiles(List<AceSoil> soils, Path destDir, String fileNameExt) {

        if (soils.isEmpty()) {
            return;
        }
        validateSoilData(soils, destDir, fileNameExt);
        List<AceDataAdaptor> dataList = new ArrayList();
        for (AceSoil soil : soils) {
            dataList.add(new AceSoilAdaptor(soil));
        }
        writeSoilFile(dataList, destDir, "Parcelle", fileNameExt);
        writeSoilFile(dataList, destDir, "TypeSol", fileNameExt);
    }

    protected static void validateSoilData(List<AceSoil> soils, Path destDir, String fileNameExt) {

        // Validation
        String[] soilCheckList = {"sltop", "slro", "sldr"};
        String[] layerCheckList = {"sllb", "sldul", "slll", "slsat"};
        StringBuilder sbSoils = new StringBuilder();
        try {

            for (AceSoil soil : soils) {

                StringBuilder sbSoil = new StringBuilder();
                String soilId = soil.getValueOr("soil_id", "N/A");
                boolean isMissing = false;

                for (String check : soilCheckList) {
                    if ("".equals(soil.getValueOr(check, "").trim())) {
                        sbSoil.append(check.toUpperCase()).append(", ");
                        isMissing = true;
                    }
                }
                if ("".equals(soil.getValueOr("sarrah_pevap__soil", "").trim())) {
                    sbSoil.append("SARRAH_PEVAP__SOIL (apply 0.3 as default), ");
                    isMissing = true;
                }

                if (isMissing) {
                    sbSoil.append("\r\n");
                }

                int layerCnt = 1;
                for (AceRecord layer : soil.getSoilLayers()) {

                    boolean isLayerMissing = false;
                    StringBuilder sbSoilLayer = new StringBuilder();
                    sbSoilLayer.append(" - Layer ").append(layerCnt).append(" : ");

                    for (String check : layerCheckList) {
                        if ("".equals(layer.getValueOr(check, "").trim())) {
                            sbSoilLayer.append(check.toUpperCase()).append(", ");
                            isLayerMissing = true;
                        }
                    }
                    if (isLayerMissing) {
                        sbSoil.append(sbSoilLayer).append("\r\n");
                        isMissing = true;
                    }

                    layerCnt++;
                }

                if (isMissing) {
                    sbSoils.append("Soil [").append(soilId).append("] does not have the following required variable for SarraH:\r\n");
                    sbSoils.append(sbSoil).append("\r\n");
                }
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
        }

        // Generate error report
        Optional<Path> file = generateFileName("Error", destDir, fileNameExt);
        if (file.isPresent()) {
            LOG.debug("Writing to file {}", file);
        }
        try (Writer writer = TransUtil.openUTF8FileForWrite(file.get());) {

            writer.write(sbSoils.toString());
            writer.close();
        } catch (IOException ex) {
            LOG.error("An error occured writing a {} file: {}", "Error_soil", ex.getMessage());
        }
    }

    protected static void writeSoilFile(List<AceDataAdaptor> soils, Path destDir, String fileType, String fileNameExt) {

        Optional<Path> file = generateFileName(fileType, destDir, fileNameExt);
        if (file.isPresent()) {
            LOG.debug("Writing to file {}", file);
            try (Writer writer = TransUtil.openUTF8FileForWrite(file.get());) {
                Velocity.init();
                VelocityContext context = new VelocityContext();
                Reader R = new InputStreamReader(AceDatasetToSarraH.class.getClassLoader().getResourceAsStream("Soil_" + fileType + ".template"));

                context.put("soils", soils);
//                context.put("util", new TransUtil());
                Velocity.evaluate(context, writer, "Generate " + fileType, R);
                writer.close();
            } catch (IOException ex) {
                LOG.error("An error occured writing a {} file: {}", fileType, ex.getMessage());
            }
        }
    }

    protected static void writeExperimentFiles(List<AceExperiment> exps, Path destDir, String fileNameExt) {

        if (exps.isEmpty()) {
            return;
        }
//        validateExpData(exps, destDir);
        List<AceDataAdaptor> dataList = new ArrayList();
        for (AceExperiment exp : exps) {
            dataList.add(new AceExpAdaptor(exp));
        }
        writeMgnFile(dataList, destDir, "ItineraireTechnique", fileNameExt);
        writeMgnFile(dataList, destDir, "Irrigation", fileNameExt);
        writeSimFileSingle(dataList, destDir, "Dossier", fileNameExt);
        writeSimFileSingle(dataList, destDir, "Modele", fileNameExt);
        writeSimFileSingle(dataList, destDir, "ListeRequete", fileNameExt);
        writeSimFile(dataList, destDir, "Simule", fileNameExt);
        writeSimFile(dataList, destDir, "ListeSimule", fileNameExt);
    }

    protected static void writeMgnFile(List<AceDataAdaptor> exps, Path destDir, String fileType, String fileNameExt) {

        Optional<Path> file = generateFileName(fileType, destDir, fileNameExt);
        if (file.isPresent()) {
            LOG.debug("Writing to file {}", file);
            try (Writer writer = TransUtil.openUTF8FileForWrite(file.get());) {
                Velocity.init();
                VelocityContext context = new VelocityContext();
                Reader R = new InputStreamReader(AceDatasetToSarraH.class.getClassLoader().getResourceAsStream("Mgn_" + fileType + ".template"));

                context.put("exps", exps);
//                context.put("util", new TransUtil());
                Velocity.evaluate(context, writer, "Generate " + fileType, R);
                writer.close();
            } catch (IOException ex) {
                LOG.error("An error occured writing a {} file: {}", fileType, ex.getMessage());
            }
        }
    }

    protected static void writeSimFile(List<AceDataAdaptor> exps, Path destDir, String fileType, String fileNameExt) {

        // TODO need to change to single record rather than multiple records for a single data set
        // Check Marya's comments
        Optional<Path> file = generateFileName(fileType, destDir, fileNameExt);
        if (file.isPresent()) {
            LOG.debug("Writing to file {}", file);
            try (Writer writer = TransUtil.openUTF8FileForWrite(file.get());) {
                Velocity.init();
                VelocityContext context = new VelocityContext();
                Reader R = new InputStreamReader(AceDatasetToSarraH.class.getClassLoader().getResourceAsStream("Sim_" + fileType + ".template"));
                context.put("exps", exps);
//                context.put("util", new TransUtil());
                Velocity.evaluate(context, writer, "Generate " + fileType, R);
                writer.close();
            } catch (IOException ex) {
                LOG.error("An error occured writing a {} file: {}", fileType, ex.getMessage());
            }
        }
    }

    protected static void writeSimFileSingle(List<AceDataAdaptor> exps, Path destDir, String fileType, String fileNameExt) {

        // TODO need to change to single record rather than multiple records for a single data set
        // Check Marya's comments
        Optional<Path> file = generateFileName(fileType, destDir, fileNameExt);
        if (file.isPresent()) {
            LOG.debug("Writing to file {}", file);
            try (Writer writer = TransUtil.openUTF8FileForWrite(file.get());) {
                Velocity.init();
                VelocityContext context = new VelocityContext();
                Reader R = new InputStreamReader(AceDatasetToSarraH.class.getClassLoader().getResourceAsStream("Sim_" + fileType + ".template"));

                AceDataAdaptor exp;
                if (exps.isEmpty()) {
                    exp = new AceDataAdaptor(new AceExperiment());
                } else {
                    exp = exps.get(0);
                }
                context.put("exp", exp);
//                context.put("util", new TransUtil());
                Velocity.evaluate(context, writer, "Generate " + fileType, R);
                writer.close();
            } catch (IOException ex) {
                LOG.error("An error occured writing a {} file: {}", fileType, ex.getMessage());
            }
        }
    }

    protected static String getFileNameExt(AceDataset ace) {
        List<AceExperiment> exps = ace.getExperiments();
        if (exps.isEmpty()) {
            return TransUtil.getFileNameExt("", "");
        } else {
            return TransUtil.getFileNameExt(new AceExpAdaptor(exps.get(0)));
        }
    }

    // TODO: Update to the latest naming rule
    protected static Optional<Path> generateFileName(String prefix, AceComponent station, Path destDir) {
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

    protected static Optional<Path> generateFileName(String prefix, Path destDir, String fileNameExt) {
        String fileName = new StringBuffer(prefix)
                .append("_").append(fileNameExt).append(".txt")
                .toString();
        Path file = destDir.resolve(fileName);
        return Optional.of(file);
    }
}
