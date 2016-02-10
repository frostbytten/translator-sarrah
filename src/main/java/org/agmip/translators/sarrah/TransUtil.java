package org.agmip.translators.sarrah;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.agmip.ace.AceDataset;
import org.agmip.ace.AceEvent;
import org.agmip.ace.AceEventCollection;
import org.agmip.ace.AceEventType;
import org.agmip.ace.AceExperiment;
import org.agmip.common.Functions;
import org.agmip.dome.DomeUtil;
import org.agmip.util.MapUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Provide static String/number handling method for SarraH translations.
 *
 * @author Meng Zhang
 */
public class TransUtil {

    private static final DateTimeFormatter DTF_IN = ISODateTimeFormat.basicDate();
    private static final DateTimeFormatter DTF_OUT = DateTimeFormat.forPattern("dd/MM/yyyy");
    private static final String WIND_CONVERSION = "86.4";
    private static final String DEF_GIS_VAL = "-999";

    public static String toSarraHDateFormat(String wdate) {
        String wDateSarraH = "";
        if (!wdate.equals("")) {
            DateTime wDate = DTF_IN.parseDateTime(wdate);
            wDateSarraH = DTF_OUT.print(wDate);
        }
        return wDateSarraH;
    }

    public static String toSarraHWind(String wind) {
        String ret = Functions.divide(wind, WIND_CONVERSION);
        if (ret != null) {
            return ret;
        } else {
            return "";
        }
    }

    public static String toGisVal(String gisVal) {
        if (gisVal == null || gisVal.trim().equals("")) {
            return DEF_GIS_VAL;
        } else {
            return gisVal;
        }
    }

    public static String getFstEventVar(Optional<AceEventCollection> events, AceEventType eventType, String key) {
        try {
            if (events.isPresent()) {
                String ret = "";
                for (AceEvent event : events.get()) {
                    if (eventType.equals(event.getEventType())) {
                        ret = event.getValueOr(key, "");
                    }
                }
                return ret;
            } else {
                return "";
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

    public static List<AceDataAdaptor> getEventListByType(Optional<AceEventCollection> events, AceEventType type) {
        List<AceDataAdaptor> ret = new ArrayList<>();
        if (events.isPresent()) {
            for (AceEvent event : events.get()) {
                if (type.equals(event.getEventType())) {
                    ret.add(new AceDataAdaptor(event));
                }
            }
        }
        return ret;
    }

    public static String getNowDate(String format) {
        SimpleDateFormat matter = new SimpleDateFormat(format);
        return matter.format(new Date());
    }

    public static String getFileNameExt(AceExpAdaptor data) {
        return getFileNameExt(getDomeMetaInfo(data, "reg_id", "Unknown"), data.getCrid());
    }

    public static String getFileNameExt(String redId, String crid) {
        // construct text string from REG_ID + "_" + CRID + "_" + <date>
        StringBuilder sb = new StringBuilder();
        sb.append(redId).append("_");
        sb.append(crid).append("_");
        sb.append(TransUtil.getNowDate("yyyyMMdd_HHmmss"));
        return sb.toString();
    }

    public static String getYear(String date) {
        if (date != null && date.length() > 4) {
            return date.substring(0, 4);
        } else {
            return "";
        }
    }
    
    public static String getDomeMetaInfo(AceExpAdaptor data, String key, String defVal) {
        return getDomeMetaInfoList(data, new String[]{key}, new String[]{defVal}).get(0);
    }
    
    public static List<String> getDomeMetaInfoList(AceExpAdaptor data, String[] keys, String[] defVals) {
        
        List<String> ret = new ArrayList();
        String fieldOverlayString = data.get("field_overlay");
        String seasonalStrategyString = data.get("seasonal_strategy");
        ArrayList<HashMap<String, String>> domeBases = new ArrayList();
        if (! seasonalStrategyString.equals("")) {
            domeBases.addAll(getDomeMetaInfos(seasonalStrategyString));
        }
        domeBases.addAll(getDomeMetaInfos(fieldOverlayString));

        for (int i = 0; i < keys.length && i < defVals.length; i++) {
            ret.add(getDomeMetaInfo(domeBases, keys[i], defVals[i]));
        }
        
        return ret;
        
    }
    
    private static ArrayList<HashMap<String, String>> getDomeMetaInfos(String domeStr) {
        ArrayList<HashMap<String, String>> ret = new ArrayList();
        String[] domes = domeStr.split("[|]");
        for (String dome : domes) {
            ret.add(DomeUtil.unpackDomeName(dome));
        }
        return ret;
    }

    private static String getDomeMetaInfo(ArrayList<HashMap<String, String>> domeBases, String metaId, String defVal) {
        String ret = "";
        for (HashMap<String, String> domeBase : domeBases) {
            ret = MapUtil.getValueOr(domeBase, metaId, "");
            if (!ret.equals("")) {
                break;
            }
        }
        if (ret.equals("")) {
            return defVal;
        } else {
            return ret;
        }
    }

    /**
     * Generate a Writer object, encoding with UTF_8.
     * 
     * @param file
     * @return writer w/ UTF_8
     * @throws IOException 
     */
    public static Writer openUTF8FileForWrite(Path file) throws IOException {
        return new OutputStreamWriter(
                new FileOutputStream(file.toFile()),
                StandardCharsets.UTF_8);
    }
}
