package org.agmip.translators.sarrah;

import java.io.IOException;
import org.agmip.ace.AceComponent;
import org.agmip.common.Functions;

/**
 * Adaptor class for using ACE data in the Velocity template. Provide common
 * method for getting variable from data directly.
 *
 * @author Meng Zhang
 */
public class AceDataAdaptor {

    protected final AceComponent data;

    public AceDataAdaptor(AceComponent data) {
        this.data = data;
    }

    public String get(String key) {
        try {
            return data.getValueOr(key, "");
        } catch (IOException e) {
            Functions.getStackTrace(e);
            return "";
        }
    }

    public AceComponent getData() {
        return this.data;
    }
}
