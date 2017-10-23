package org.agmip.translators.sarrah;

import java.io.IOException;
import java.util.Optional;
import org.agmip.ace.AceEventType;
import org.agmip.ace.AceExperiment;
import org.agmip.ace.AceRecord;
import org.agmip.ace.AceRecordCollection;
import org.agmip.ace.AceSoil;
import org.agmip.common.Functions;

/**
 * Soil profile adaptor class. Considering the amount and size of soil data in
 * the whole data set and the complexity of translation for soil, this class
 * will hold the translation logic to make easier maintenance on templates
 *
 * @author Meng Zhang
 */
public class AceSoilAdaptor extends AceDataAdaptor {

    private static final String DEF_SLTOP = "20"; // TODO need to be comfirmed
    private Optional<AceRecordCollection> soilLayers = Optional.empty();
    private Optional<AceRecordCollection> icList = Optional.empty();
    private AceExperiment exp;

    public AceSoilAdaptor(AceSoil data, AceExperiment exp) throws Exception {
        super(data);
        try {
            this.exp = exp;
            soilLayers = Optional.of(data.getSoilLayers());
            this.icList = Optional.of(exp.getInitialConditions().getSoilLayers());
            if (soilLayers.isPresent() && this.icList.isPresent() && soilLayers.get().size() != this.icList.get().size()) {
                throw new Exception("Ininitial condition is not matched with corresponding soil layer");
            } 
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
        }
    }

    public String getNom() {
        try {
            return data.getValueOr("soil_name", data.getValueOr("soil_id", ""));
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

    public String getStockIniSurf() {
        
//        // If it is a non-transplant crop, the surface initial water should be 0
//        String crid = TransUtil.getFstEventVar(Optional.of(exp.getEvents()), AceEventType.ACE_PLANTING_EVENT, "crid");
//        String page = TransUtil.getFstEventVar(Optional.of(exp.getEvents()), AceEventType.ACE_PLANTING_EVENT, "crid");
//        if (!"RIC".equals(crid.toUpperCase()) || !page.equals("")) {
//            return "0";
//        }
        // Otherwise use formula to calculate initial top water
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return TransUtil.MISSING_VALUE;
        }
        // Σ(depth = 0-SLTOP) [ICH2O(i)*(layer thickness-cm)*10.]
        try {
            String ret = "0";
            String sltop = data.getValueOr("sltop", DEF_SLTOP);
            if (sltop.equals("")) {
                if (soilLayers.get().isEmpty()) {
                    return TransUtil.MISSING_VALUE;
                } else {
                    AceRecord topLayer = soilLayers.get().getByIndex(0);
                    sltop = topLayer.getValueOr("sllb", "");
                }
            }
            String lastSllb = "0";
            for (int i = 0; i < soilLayers.get().size(); i++) {
                String sllb = soilLayers.get().getByIndex(i).getValueOr("sllb", "");
                String ich2o = icList.get().getByIndex(i).getValueOr("ich2o", "");
                if (Functions.compare(sllb, sltop, Functions.CompareMode.NOTGREATER)) {
                    String thickness = Functions.substract(sllb, lastSllb);
                    ret = Functions.sum(ret, Functions.product(ich2o, thickness, "10"));
                    if (Functions.compare(sllb, sltop, Functions.CompareMode.EQUAL)) {
                        break;
                    }
                    lastSllb = sllb;
                } else {
                    String thickness = Functions.substract(sltop, lastSllb);
                    ret = Functions.sum(ret, Functions.product(ich2o, thickness, "10"));
                    break;
                }
            }
            if (ret == null) {
                return "-99";
            } else {
                return ret;
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return "-98";
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "-97";
        }
    }

    public String getStockIniProf() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return " ";
        }
        // Σ(depth = SLTOP to SLLB(last)) [ICH2O(i)*(layer thickness-cm)*10.]
        try {
            String ret = "0";
            String sltop = data.getValueOr("sltop", DEF_SLTOP);
            if (sltop.equals("")) {
                if (soilLayers.get().isEmpty()) {
                    return TransUtil.MISSING_VALUE;
                } else {
                    AceRecord topLayer = soilLayers.get().getByIndex(0);
                    sltop = topLayer.getValueOr("sllb", "");
                }
            }
            String lastSllb = "0";
            for (int i = 0; i < soilLayers.get().size(); i++) {
                String sllb = soilLayers.get().getByIndex(i).getValueOr("sllb", "");
                String ich2o = icList.get().getByIndex(i).getValueOr("ich2o", "");
                if (Functions.compare(lastSllb, sltop, Functions.CompareMode.GREATER)) {
                    String thickness = Functions.substract(sllb, lastSllb);
                    ret = Functions.sum(ret, Functions.product(ich2o, thickness, "10"));
                } else if (Functions.compare(sllb, sltop, Functions.CompareMode.GREATER)) {
                    String thickness = Functions.substract(sllb, sltop);
                    ret = Functions.sum(ret, Functions.product(ich2o, thickness, "10"));
                }
                lastSllb = sllb;
            }
            if (ret == null) {
                return "-99";
            } else {
                return ret;
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return " ";
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return " ";
        }
    }
    
    public String getEpaisseurSurf() {
        // SLTOP * 10 (cm -> mm)
        return Functions.multiply(super.get("sltop"), "10");
    }

    public String getEpaisseurProf() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return "-98";
        }
        // SLLB(last element) * 10 (cm -> mm)
        try {
            AceRecord baseLayer = soilLayers.get().getByIndex(soilLayers.get().size() - 1);
            return Functions.multiply(baseLayer.getValueOr("sllb", ""), "10");
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "-97";
        }

    }

    public String getSeuilRuiss() {
        try {
            String slro = data.getValueOr("slro", "");
            if (slro.equals("")) {
                return TransUtil.MISSING_VALUE;
            }

            // SeuilRuiss = 38.1 * (100/SLRO - 1)
            String ret = Functions.multiply("38.1", Functions.substract(Functions.divide("100", slro), "1"));
            if (ret == null) {
                return TransUtil.MISSING_VALUE;
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

    public String getPourcRuiss() {
        try {
            String slro = data.getValueOr("slro", "");
            if (slro.equals("")) {
                return TransUtil.MISSING_VALUE;
            }

            // getPourcRuiss =1/(2540/SLRO-24.4)*100.%
            String ret = Functions.divide("100", Functions.substract(Functions.divide("2540", slro), "24.4"), 1);
            if (ret == null) {
                return TransUtil.MISSING_VALUE;
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

    public String getRu() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return TransUtil.MISSING_VALUE;
        }

        // {Σ[(SLDUL-SLLL)*(thickness)]/Σ(thickness)} *1000.
        try {
            String lastSllb = "0";
            String sumWater = "0";
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String sldul = layer.getValueOr("sldul", "");
                String slll = layer.getValueOr("slll", "");
                String thickness = Functions.substract(sllb, lastSllb);
                sumWater = Functions.sum(sumWater, Functions.multiply(Functions.substract(sldul, slll), thickness));
                lastSllb = sllb;
            }
            String ret = Functions.multiply(Functions.divide(sumWater, lastSllb), "1000");
            if (ret == null) {
                return TransUtil.MISSING_VALUE;
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

    public String getHumCR() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return TransUtil.MISSING_VALUE;
        }

        // Σ(SLDUL*thickness) 
        try {
            String lastSllb = "0";
            String ret = "0";
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String sldul = layer.getValueOr("sldul", "");
                String thickness = Functions.divide(Functions.substract(sllb, lastSllb), "100");
                ret = Functions.sum(ret, Functions.multiply(sldul, thickness));
                lastSllb = sllb;
            }
            if (ret == null) {
                return TransUtil.MISSING_VALUE;
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

    public String getHumPF() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return TransUtil.MISSING_VALUE;
        }

        // Σ(SLLL*thickness)
        try {
            String lastSllb = "0";
            String ret = "0";
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String slll = layer.getValueOr("slll", "");
                String thickness = Functions.divide(Functions.substract(sllb, lastSllb), "100");
                ret = Functions.sum(ret, Functions.multiply(slll, thickness));
                lastSllb = sllb;
            }
            if (ret == null) {
                return TransUtil.MISSING_VALUE;
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

    public String getHumFC() {
        return getHumCR();
    }

    public String getHumSat() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return TransUtil.MISSING_VALUE;
        }

        // Σ(SLSAT*thickness)
        try {
            String lastSllb = "0";
            String ret = "0";
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String slsat = layer.getValueOr("slsat", "");
                String thickness = Functions.divide(Functions.substract(sllb, lastSllb), "100");
                ret = Functions.sum(ret, Functions.multiply(slsat, thickness));
                lastSllb = sllb;
            }
            if (ret == null) {
                return TransUtil.MISSING_VALUE;
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

    public String getSarrah_pevap__soil() {
        String ret = get("sarrah_pevap__soil");
        if (ret.equals("")) {
            ret = "0.3";
        }
        return ret;
    }

    public String getPercolationMax() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return TransUtil.MISSING_VALUE;
        }

        // Σ[(SLDUL-SLLL)*(thickness)]*SLDR *10.
        try {
            String lastSllb = "0";
            String sumWater = "0";
            String sldr = data.getValueOr("sldr", "");
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String sldul = layer.getValueOr("sldul", "");
                String slll = layer.getValueOr("slll", "");
                String thickness = Functions.substract(sllb, lastSllb);
                sumWater = Functions.sum(sumWater, Functions.multiply(Functions.substract(sldul, slll), thickness));
                lastSllb = sllb;
            }
            String ret = Functions.product(sumWater, sldr, "10");
            if (ret == null) {
                return TransUtil.MISSING_VALUE;
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return TransUtil.MISSING_VALUE;
        }
    }

}
