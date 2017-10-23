package org.agmip.translators.sarrah;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.agmip.ace.AceComponent;
import org.agmip.ace.AceRecord;
import org.agmip.ace.AceWeather;
import org.agmip.common.Functions;

/**
 * Weather station profile adaptor class. To save memory consuming, this class
 * will not hold the translation detail for weather station profile variables
 *
 * @author Meng Zhang
 */
public class AceWeatherAdaptor extends AceDataAdaptor {

    private final ArrayList<AceDailyWeatherAdaptor> dailyWeather = new ArrayList();

    public AceWeatherAdaptor(AceWeather data) {
        super(data);
        try {
            for (AceRecord record : data.getDailyWeather()) {
                dailyWeather.add(new AceDailyWeatherAdaptor(record, data));
            }
        } catch (IOException e) {
            Functions.getStackTrace(e);
            dailyWeather.clear();
        }
    }

    public ArrayList<AceDailyWeatherAdaptor> getDailyWeather() {
        return this.dailyWeather;
    }

    public String getCodePays() {

        return GeoUtil.getISO3BitCountryCode(
                super.get("wst_notes"),
                super.get("wst_name"),
                super.get("wst_loc_1"),
                super.get("wst_site"));
    }
    
    public String getNom() {
        String ret = super.get("wst_name");
        if (ret.equals("")) {
            return super.get("wst_notes");
        } else {
            return ret;
        }
    }

    public class AceDailyWeatherAdaptor extends AceDataAdaptor {
        
        private AceWeather wst;

        public AceDailyWeatherAdaptor(AceComponent data, AceWeather wst) {
            super(data);
            this.wst = wst;
        }
        
        public String getHMoy() {
            String ret = super.get("rhavd");
            if (ret.equals("")) {
                ret = super.get("rhumd");
            }
            return ret;
        }
        
        public String getEtoCalc() {
            String Alt;
            try {
                Alt = wst.getValueOr("wst_elev", "");
            } catch (IOException ex) {
                Alt = "";
            }
            String RgMax = super.get("srad");
            String RayGlobal = super.get("srad");
            String TMin = super.get("tmin");
            String TMax = super.get("tmax");
//            String HrMin = super.get("rhumd");
//            String HrMax = super.get("rhmxd");
            String HrMoy = super.get("rhumd");
            String Tmoy = super.get("tavd");
            String TmoyPrec = super.get("");
            String Vent = super.get("wind");
            String ret = TransUtil.calcEto(Alt, RgMax, RayGlobal, TMin, TMax, HrMoy, Tmoy, TmoyPrec, Vent);
            return ""; // TODO temporal value for debug
        }

    }
}
