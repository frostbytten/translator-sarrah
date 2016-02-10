package org.agmip.translators.sarrah;

import java.io.IOException;
import java.util.ArrayList;
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

    private final ArrayList<AceDataAdaptor> dailyWeather = new ArrayList();

    public AceWeatherAdaptor(AceWeather data) {
        super(data);
        try {
            for (AceRecord record : data.getDailyWeather()) {
                dailyWeather.add(new AceDataAdaptor(record));
            }
        } catch (IOException e) {
            Functions.getStackTrace(e);
            dailyWeather.clear();
        }
    }

    public ArrayList<AceDataAdaptor> getDailyWeather() {
        return this.dailyWeather;
    }
}
