package org.agmip.translators.sarrah;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.agmip.ace.AceEvent;
import org.agmip.ace.AceEventCollection;
import org.agmip.ace.AceEventType;
import org.agmip.ace.AceExperiment;
import org.agmip.ace.AceInitialConditions;
import org.agmip.ace.AceRecord;
import org.agmip.ace.AceWeather;
import org.agmip.common.Functions;

/**
 * Experiment data adaptor class. Considering the amount and size of experiment
 * data in the whole data set and the complexity of translation for experiment
 * data, this class will hold the translation logic to make easier maintenance
 * on templates
 *
 * @author Meng Zhang
 */
public class AceExpAdaptor extends AceDataAdaptor {

//    private static final String DEF_SLTOP = "20";
    private Optional<AceEventCollection> events = Optional.empty();
    private Optional<AceInitialConditions> intCdns = Optional.empty();
    private boolean isEtoExisited;

    public AceExpAdaptor(AceExperiment data) {
        super(data);
        events = Optional.of(data.getEvents());
        try {
            intCdns = Optional.of(data.getInitialConditions());
            AceWeather wth = data.getWeather();
            if (wth != null && !wth.getDailyWeather().isEmpty()) {
                AceRecord daily = wth.getDailyWeather().getByIndex(0);
                isEtoExisited = !daily.getValueOr("eto", "").equals("");
            } else {
                isEtoExisited = false;
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
        }
    }

    public String getIdDossier() {
        // Construct using EXNAME + CRID + CLIM_ID + RAP_ID + MAN_ID.(e.g., "Kouti001-MAZ-0XXX-0-0")
        // No longer use EXNAME - 2016/01/29
        StringBuilder sb = new StringBuilder();
//        sb.append(this.get("exname")).append("_");
        sb.append(this.getCrid()).append("_");
        sb.append(this.get("clim_id")).append("_");
        List<String> domeInfo = TransUtil.getDomeMetaInfoList(this, new String[]{"rap_id", "man_id"}, new String[]{"0", "0"});
        sb.append(domeInfo.get(0)).append("_");
        sb.append(domeInfo.get(1));
        return sb.toString();
    }

    public String getCrid() {
        return TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "crid");
    }

    public String getPdate() {
        return TransUtil.toSarraHDateFormat(TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "date"));
    }

    public String getPldp() {
        return TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "pldp");
    }

    public String getDensite() {
        // Densite = PLPOP / 10000 -> should be #/m2 -> #/ha, which means multiple by 10000
        String plpop = TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "plpop");
        String ret = Functions.multiply(plpop, "10000");
        if (ret == null) {
            return TransUtil.MISSING_VALUE;
        } else {
            return ret;
        }
    }

    public String getMulch() {
        //  MULCH = EXP(-37. * 1.E-5 * ICRAG) * 100%
        try {
            if (intCdns.isPresent()) {
                String icrag = intCdns.get().getValueOr("icrag", "");
                String ret = Functions.multiply(Functions.exp(Functions.product("-37", "0.00001", icrag)), "100");
                if (ret == null) {
                    return TransUtil.MISSING_VALUE;
                } else {
                    return ret;
                }
            } else {
                return TransUtil.MISSING_VALUE;
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

    public String getIdIrrigation() {
        if (TransUtil.isIrrigationExist(events.get())) {
            // TODO we might change to another better rule later
            return getIdItineraireTechnique();
        }
        return "";
    }

    public String getSeuilEauSemis() {
        // (Number of mm of rainfall after start of simulation to trigger planting) - no need to translate further.
        // replaced by the DOME function for auto-planting
        return TransUtil.MISSING_VALUE;
    }

    public String getIdGestionEau() {
        // TODO we might change to another better rule later
        return TransUtil.MISSING_VALUE;
    }

    public String getPlph() {
        return TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "plph");
    }

    public String getAbund() {
        // Bund height (e.g., for flooded rice)
        try {
            if (events.isPresent()) {
                String abund = "";
                for (AceEvent event : events.get()) {
                    if (AceEventType.ACE_IRRIGATION_EVENT.equals(event.getEventType())) {
                        String code = event.getValueOr("irop", "");
                        if (code.equals("IR009")) {
                            abund = event.getValueOr("irval", "");
                        }
                    }
                }
                return abund;
            } else {
                return TransUtil.MISSING_VALUE;
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

    public String getIrrigAuto() {
        String irrig = TransUtil.getFstEventVar(events, AceEventType.ACE_AUTO_IRRIG_EVENT, "irrig");
        // TODO need code defination mapping
        if (irrig.equals("")) {
            return "0";
        } else {
            return "0";
        }
    }

    public String getIrrigAutoTarget() {
        // IrrigAutoTarget = IRTHR/100
        String irthr = TransUtil.getFstEventVar(events, AceEventType.ACE_AUTO_IRRIG_EVENT, "irthr");
        String ret = Functions.divide(irthr, "100");
        if (ret == null) {
            return TransUtil.MISSING_VALUE;
        } else {
            return ret;
        }
    }

    public String getTrdate() {
        return TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "trdate");
    }

    public String getPage() {
        return TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "page");
    }

    public String getNplsb() {
        return TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "nplsb");
    }

    public String getDensityField() {
        String ret = TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "plpop");
        if (ret.equals("")) {
            ret = getPlph();
        }
        return ret;
    }

    public String getFTSWIrrig() {
        // TODO need to confirm if this is true.
        return getIrrigAutoTarget();
    }

    public List<AceDataAdaptor> getIrrigations() {
        return TransUtil.getEventListByType(events, AceEventType.ACE_IRRIGATION_EVENT);
    }

    public String getDescription() {
        // construct text string from REG_ID + "_" + CRID + "_" + <date> (just like file names)
        return TransUtil.getFileNameExt(this);
    }

    public String getIdVariete() {
        String cul_id = TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "sarrah_cul_id");
        if (cul_id.equals("")) {
            cul_id = TransUtil.getFstEventVar(events, AceEventType.ACE_PLANTING_EVENT, "cul_id");
        }
        return cul_id;
    }

    public String getIdItineraireTechnique() {
        // Construct using EXNAME + CLIM_ID + RAP_ID + MAN_ID + Treatment #.
        // EXNAME usually already contains treatment # and is always unique
//        StringBuilder sb = new StringBuilder();
//        sb.append(this.get("exname")).append("_");
//        sb.append(this.get("clim_id")).append("_");
//        List<String> domeInfo = TransUtil.getDomeMetaInfoList(this, new String[]{"rap_id", "man_id"}, new String[]{"0", "0"});
//        sb.append(domeInfo.get(0)).append("_");
//        sb.append(domeInfo.get(1));
//        String trtno = this.get("trtno");
//        if (!trtno.equals("")) {
//            sb.append("_").append(trtno);
//        }
//        return sb.toString();
        
        // since this ID is not suggested to be longer than 15-bits, so only use EXNAME instead
        return this.get("exname");
    }

    public String getSdat() {
        return TransUtil.toSarraHDateFormat(this.get("sdat"));
    }

    public String getEndat() {
        return TransUtil.toSarraHDateFormat(this.get("endat"));
    }

    public String getAnDebutSimul() {
        return TransUtil.getYear(this.get("sdat"));
    }

    public String getAnFinSimul() {
        return TransUtil.getYear(this.get("endat"));
    }

    public String getNbAnSim() {
//        String expDur = this.get("exp_dur");
//        if (expDur.equals("")) {
//            String start = this.getAnDebutSimul();
//            String end = this.getAnFinSimul();
//            expDur = Functions.sum(Functions.substract(end, start), "1");
//            if (expDur == null) {
//                return TransUtil.MISSING_VALUE;
//            }
//        }
//        return expDur;

        return "1"; // Seasonal srategy always make 30-year simulation into 30 separated simulation and each one is a 1-year simulation
    }
    
    public String getIdEtp() {
        if (isEtoExisited) {
            return "ETpImp";
        } else {
            return "ETo";
        }
    }
}
