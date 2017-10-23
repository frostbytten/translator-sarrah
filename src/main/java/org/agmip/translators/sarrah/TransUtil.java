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
import org.agmip.ace.AceEvent;
import org.agmip.ace.AceEventCollection;
import org.agmip.ace.AceEventType;
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
    protected static final String MISSING_VALUE = "";

    public static String toSarraHDateFormat(String wdate) {
        String wDateSarraH = TransUtil.MISSING_VALUE;
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
            return TransUtil.MISSING_VALUE;
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
                String ret = TransUtil.MISSING_VALUE;
                for (AceEvent event : events.get()) {
                    if (eventType.equals(event.getEventType())) {
                        ret = event.getValueOr(key, "");
                    }
                }
                return ret;
            } else {
                return TransUtil.MISSING_VALUE;
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
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
            return TransUtil.MISSING_VALUE;
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
        if (!seasonalStrategyString.equals("")) {
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

    public static String calcEto(String Alt, String RgMax, String RayGlobal, String TMin, String TMax, String HrMoy, String Tmoy, String TmoyPrec, String Vent) {
        String eActual, eSat, RgRgMax, TLat, delta, KPsy, Eaero, Erad, Rn, G, HrMax, HrMin;
        HrMax = HrMoy;
        HrMin = HrMoy;
//        String VPD;
        String Eto;

//      eSat := 0.3054 * (Exp(17.27 * TMax / (TMax + 237.3)) +
//              exp (17.27 * TMin / (TMin + 237.3)));
        eSat = Functions.multiply("0.3054",
                Functions.sum(
                        Functions.exp(Functions.divide(Functions.multiply("17.27", TMax), Functions.sum(TMax, "237.3"))),
                        Functions.exp(Functions.divide(Functions.multiply("17.27", TMin), Functions.sum(TMin, "237.3")))
                )
        );
        if (HrMax == null) {
//        eActual := eSat * HrMoy / 100
            eActual = Functions.divide(Functions.multiply(eSat, HrMoy), "100");
        } else {
//        eActual := 0.3054 * (Exp(17.27 * TMax / (TMax + 237.3)) *
//                   HrMin/100 + Exp(17.27 * TMin / (TMin + 237.3)) *
//                   HrMax / 100);
            String tmpVal = Functions.exp(Functions.divide(Functions.multiply("17.27", TMax), Functions.sum(TMax, "237.3")));
            eActual = Functions.sum(Functions.product("0.3054", tmpVal, Functions.divide(HrMin, "100")),
                    Functions.multiply(tmpVal, Functions.divide(HrMax, "100")));
        }

//      VPD := eSat-eActual;
//            VPD = Functions.substract(eSat, eActual);
//      RgRgMax := RayGlobal / RgMax;
        RgRgMax = Functions.divide(RayGlobal, RgMax);
//      if (RgRgMax > 1) then
//        RgRgMax := 1;
        if (Functions.compare(RgRgMax, "1", Functions.CompareMode.GREATER)) {
            RgRgMax = "1";
        }
//      Rn := 0.77 * RayGlobal - (1.35 * RgRgMax - 0.35) *
//            (0.34 - 0.14 * Power(eActual, 0.5)) *
//            (Power(TMax + 273.16, 4) + Power(TMin + 273.16, 4)) * 2.45015 * Power(10, -9);
        Rn = Functions.sum(Functions.product("0.77", RayGlobal),
                Functions.product("-1",
                        Functions.substract(Functions.multiply(RgRgMax, "1.35"), "0.35"),
                        Functions.substract("0.34", Functions.multiply("0.14", Functions.pow(eActual, "0.5")))),
                Functions.sum(Functions.pow(Functions.sum(TMax, "273.16"), "4"),
                        Functions.pow(Functions.sum(TMin, "273.16"), "4")),
                "2.45015",
                Functions.pow("10", "-9"));
//      Tlat := 2.501 - 2.361 * power(10, -3) * Tmoy;
        TLat = Functions.substract("2.501", Functions.product("2.361", Functions.pow("10", "-3"), Tmoy));
//      delta := 4098 * (0.6108 * Exp(17.27 * Tmoy / (Tmoy + 237.3))) / Power(Tmoy + 237.3, 2);
        delta = Functions.divide(
                Functions.product("4098", "0.6108",
                        Functions.exp(Functions.divide(Functions.multiply("17.27", Tmoy), Functions.sum(Tmoy, "237.3")))),
                Functions.pow(Functions.sum(Tmoy, "237.2"), "2"));
//      Kpsy  := 0.00163 * 101.3 * power(1 - (0.0065 * Alt / 293), 5.26) / TLat;
        KPsy = Functions.divide(
                Functions.product("0.00163", "101.3",
                        Functions.pow(Functions.substract("1", Functions.divide(Functions.multiply("0.0065", Alt), "293")), "5.26")),
                TLat);
//      // Radiative
//      G := 0.38 * (Tmoy - TmoyPrec);
        G = Functions.multiply("0.38", Functions.substract(Tmoy, TmoyPrec));
//      Erad := 0.408 * (Rn - G) * delta / (delta + Kpsy * ( 1 + 0.34 * Vent));
//      Vent = WIND (need unit convert km/d -> m/s )
        Vent = Functions.divide(Vent, "86.4");
        Erad = Functions.divide(
                Functions.product("0.408", Functions.substract(Rn, G), delta),
                Functions.sum(delta, Functions.product(KPsy, Functions.sum("1", Functions.multiply("0.34", Vent)))));
//      Eaero := (900 / (Tmoy + 273.16)) * ((eSat - eActual) * Vent ) * Kpsy /
//               (delta + Kpsy * ( 1 + 0.34 * Vent));
        Eaero = Functions.product(
                Functions.divide("900", Functions.sum(Tmoy, "273.16")),
                Functions.multiply(Functions.substract(eSat, eActual), Vent),
                Functions.divide(KPsy, Functions.sum(delta, Functions.multiply(KPsy, Functions.sum("1", Functions.multiply("0.34", Vent))))));
//      Eto := Erad + Eaero;
        Eto = Functions.sum(Erad, Eaero);

//    TMoyPrec = TMoy;
        return Eto;
    }

    public static boolean isIrrigationExist(AceEventCollection events) {
        for (AceEvent event : events) {
            if (event.getEventType().equals(AceEventType.ACE_IRRIGATION_EVENT)) {
                return true;
            }
        }
        return false;
    }
}
