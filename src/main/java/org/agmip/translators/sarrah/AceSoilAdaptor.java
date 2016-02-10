package org.agmip.translators.sarrah;

import java.io.IOException;
import java.util.Optional;
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

//    private static final String DEF_SLTOP = "20";
    private Optional<AceRecordCollection> soilLayers = Optional.empty();

    public AceSoilAdaptor(AceSoil data) {
        super(data);
        try {
            soilLayers = Optional.of(data.getSoilLayers());
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
        }
    }

    public String getNom() {
        try {
            return data.getValueOr("soil_name", data.getValueOr("soil_id", ""));
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

    public String getStockIniSurf() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return "";
        }
        // Σ(depth = 0-SLTOP) [ICH2O(i)*(layer thickness-cm)*10.]
        try {
            String ret = "0";
            String sltop = data.getValueOr("sltop", "");
            if (sltop.equals("")) {
                return "";
            }
            String lastSllb = "0";
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String ich2o = layer.getValueOr("ich2o", "");
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
                return "";
            } else {
                return ret;
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

    public String getStockIniProf() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return "";
        }
        // Σ(depth = SLTOP to SLLB(last)) [ICH2O(i)*(layer thickness-cm)*10.]
        try {
            String ret = "0";
            String sltop = data.getValueOr("sltop", "");
            if (sltop.equals("")) {
                return "";
            }
            String lastSllb = "0";
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String ich2o = layer.getValueOr("ich2o", "");
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
                return "";
            } else {
                return ret;
            }
        } catch (IOException ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

    public String getEpaisseurProf() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return "";
        }
        // SLLB(last element)
        try {
            AceRecord baseLayer = soilLayers.get().getByIndex(soilLayers.get().size() - 1);
            return baseLayer.getValueOr("sllb", "");
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "";
        }

    }

    public String getSeuilRuiss() {
        try {
            String slro = data.getValueOr("slro", "");
            if (slro.equals("")) {
                return "";
            }

            // SeuilRuiss = 38.1 * (100/SLRO - 1)
            String ret = Functions.multiply("38.1", Functions.substract(Functions.divide("100", slro), "1"));
            if (ret == null) {
                return "";
            } else {
                return "";
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

    public String getPourcRuiss() {
        try {
            String slro = data.getValueOr("slro", "");
            if (slro.equals("")) {
                return "";
            }

            // getPourcRuiss =1/(2540/SLRO-24.4)*100.%
            String ret = Functions.divide("100", Functions.divide(Functions.divide("2540", slro), "24.4"), 1);
            if (ret == null) {
                return "";
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

    public String getRu() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return "";
        }

        // {Σ[(SLDUL-SLLL)*(thickness)]/Σ(thickness)} *1000.
        try {
            String lastSllb = "0";
            String sumWater = "0";
            String totalThickness = getEpaisseurProf();
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String sldul = layer.getValueOr("sldul", "");
                String slll = layer.getValueOr("slll", "");
                String thickness = Functions.substract(sllb, lastSllb);
                sumWater = Functions.sum(sumWater, Functions.multiply(Functions.substract(sldul, slll), thickness));
                lastSllb = sllb;
            }
            String ret = Functions.multiply(Functions.divide(sumWater, totalThickness), "1000");
            if (ret == null) {
                return "";
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

    public String getHumCR() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return "";
        }

        // Σ(SLDUL*thickness) 
        try {
            String lastSllb = "0";
            String ret = "0";
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String sldul = layer.getValueOr("sldul", "");
                String thickness = Functions.substract(sllb, lastSllb);
                ret = Functions.sum(ret, Functions.multiply(sldul, thickness));
                lastSllb = sllb;
            }
            if (ret == null) {
                return "";
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

    public String getHumPF() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return "";
        }

        // Σ(SLLL*thickness)
        try {
            String lastSllb = "0";
            String ret = "0";
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String slll = layer.getValueOr("slll", "");
                String thickness = Functions.substract(sllb, lastSllb);
                ret = Functions.sum(ret, Functions.multiply(slll, thickness));
                lastSllb = sllb;
            }
            if (ret == null) {
                return "";
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

    public String getHumFC() {
        return getHumCR();
    }

    public String getHumSat() {
        if (!soilLayers.isPresent() || soilLayers.get().isEmpty()) {
            return "";
        }

        // Σ(SLSAT*thickness)
        try {
            String lastSllb = "0";
            String ret = "0";
            for (AceRecord layer : soilLayers.get()) {
                String sllb = layer.getValueOr("sllb", "");
                String slsat = layer.getValueOr("slsat", "");
                String thickness = Functions.substract(sllb, lastSllb);
                ret = Functions.sum(ret, Functions.multiply(slsat, thickness));
                lastSllb = sllb;
            }
            if (ret == null) {
                return "";
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "";
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
            return "";
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
                return "";
            } else {
                return ret;
            }
        } catch (Exception ex) {
            Functions.getStackTrace(ex);
            return "";
        }
    }

}
