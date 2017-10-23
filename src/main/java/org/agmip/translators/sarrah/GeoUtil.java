package org.agmip.translators.sarrah;

import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Meng Zhang
 */
public class GeoUtil {

    private static final HashMap<String, String> mapper = new HashMap();

    private static void init() {
        mapper.clear();
        mapper.put("Afghanistan", "AFG");
        mapper.put("Aland Islands", "ALA");
        mapper.put("Albania", "ALB");
        mapper.put("Algeria", "DZA");
        mapper.put("American Samoa", "ASM");
        mapper.put("Andorra", "AND");
        mapper.put("Angola", "AGO");
        mapper.put("Anguilla", "AIA");
        mapper.put("Antarctica", "ATA");
        mapper.put("Antigua and Barbuda", "ATG");
        mapper.put("Argentina", "ARG");
        mapper.put("Armenia", "ARM");
        mapper.put("Aruba", "ABW");
        mapper.put("Australia", "AUS");
        mapper.put("Austria", "AUT");
        mapper.put("Azerbaijan", "AZE");
        mapper.put("Bahamas", "BHS");
        mapper.put("Bahrain", "BHR");
        mapper.put("Bangladesh", "BGD");
        mapper.put("Barbados", "BRB");
        mapper.put("Belarus", "BLR");
        mapper.put("Belgium", "BEL");
        mapper.put("Belize", "BLZ");
        mapper.put("Benin", "BEN");
        mapper.put("Bermuda", "BMU");
        mapper.put("Bhutan", "BTN");
        mapper.put("Bolivia", "BOL");
        mapper.put("Bosnia and Herzegovina", "BIH");
        mapper.put("Botswana", "BWA");
        mapper.put("Bouvet Island", "BVT");
        mapper.put("Brazil", "BRA");
        mapper.put("British Virgin Islands", "VGB");
        mapper.put("British Indian Ocean Territory", "IOT");
        mapper.put("Brunei Darussalam", "BRN");
        mapper.put("Bulgaria", "BGR");
        mapper.put("Burkina Faso", "BFA");
        mapper.put("Burundi", "BDI");
        mapper.put("Cambodia", "KHM");
        mapper.put("Cameroon", "CMR");
        mapper.put("Canada", "CAN");
        mapper.put("Cape Verde", "CPV");
        mapper.put("Cayman Islands", "CYM");
        mapper.put("Central African Republic", "CAF");
        mapper.put("Chad", "TCD");
        mapper.put("Chile", "CHL");
        mapper.put("China", "CHN");
        mapper.put("Hong Kong, Special Administrative Region of China", "HKG");
        mapper.put("Macao, Special Administrative Region of China", "MAC");
        mapper.put("Christmas Island", "CXR");
        mapper.put("Cocos (Keeling) Islands", "CCK");
        mapper.put("Colombia", "COL");
        mapper.put("Comoros", "COM");
        mapper.put("Congo (Brazzaville)", "COG");
        mapper.put("Congo, Democratic Republic of the", "COD");
        mapper.put("Cook Islands", "COK");
        mapper.put("Costa Rica", "CRI");
        mapper.put("Côte d'Ivoire", "CIV");
        mapper.put("Croatia", "HRV");
        mapper.put("Cuba", "CUB");
        mapper.put("Cyprus", "CYP");
        mapper.put("Czech Republic", "CZE");
        mapper.put("Denmark", "DNK");
        mapper.put("Djibouti", "DJI");
        mapper.put("Dominica", "DMA");
        mapper.put("Dominican Republic", "DOM");
        mapper.put("Ecuador", "ECU");
        mapper.put("Egypt", "EGY");
        mapper.put("El Salvador", "SLV");
        mapper.put("Equatorial Guinea", "GNQ");
        mapper.put("Eritrea", "ERI");
        mapper.put("Estonia", "EST");
        mapper.put("Ethiopia", "ETH");
        mapper.put("Falkland Islands (Malvinas)", "FLK");
        mapper.put("Faroe Islands", "FRO");
        mapper.put("Fiji", "FJI");
        mapper.put("Finland", "FIN");
        mapper.put("France", "FRA");
        mapper.put("French Guiana", "GUF");
        mapper.put("French Polynesia", "PYF");
        mapper.put("French Southern Territories", "ATF");
        mapper.put("Gabon", "GAB");
        mapper.put("Gambia", "GMB");
        mapper.put("Georgia", "GEO");
        mapper.put("Germany", "DEU");
        mapper.put("Ghana", "GHA");
        mapper.put("Gibraltar", "GIB");
        mapper.put("Greece", "GRC");
        mapper.put("Greenland", "GRL");
        mapper.put("Grenada", "GRD");
        mapper.put("Guadeloupe", "GLP");
        mapper.put("Guam", "GUM");
        mapper.put("Guatemala", "GTM");
        mapper.put("Guernsey", "GGY");
        mapper.put("Guinea", "GIN");
        mapper.put("Guinea-Bissau", "GNB");
        mapper.put("Guyana", "GUY");
        mapper.put("Haiti", "HTI");
        mapper.put("Heard Island and Mcdonald Islands", "HMD");
        mapper.put("Holy See (Vatican City State)", "VAT");
        mapper.put("Honduras", "HND");
        mapper.put("Hungary", "HUN");
        mapper.put("Iceland", "ISL");
        mapper.put("India", "IND");
        mapper.put("Indonesia", "IDN");
        mapper.put("Iran, Islamic Republic of", "IRN");
        mapper.put("Iraq", "IRQ");
        mapper.put("Ireland", "IRL");
        mapper.put("Isle of Man", "IMN");
        mapper.put("Israel", "ISR");
        mapper.put("Italy", "ITA");
        mapper.put("Jamaica", "JAM");
        mapper.put("Japan", "JPN");
        mapper.put("Jersey", "JEY");
        mapper.put("Jordan", "JOR");
        mapper.put("Kazakhstan", "KAZ");
        mapper.put("Kenya", "KEN");
        mapper.put("Kiribati", "KIR");
        mapper.put("Korea, Democratic People's Republic of", "PRK");
        mapper.put("Korea, Republic of", "KOR");
        mapper.put("Kuwait", "KWT");
        mapper.put("Kyrgyzstan", "KGZ");
        mapper.put("Lao PDR", "LAO");
        mapper.put("Latvia", "LVA");
        mapper.put("Lebanon", "LBN");
        mapper.put("Lesotho", "LSO");
        mapper.put("Liberia", "LBR");
        mapper.put("Libya", "LBY");
        mapper.put("Liechtenstein", "LIE");
        mapper.put("Lithuania", "LTU");
        mapper.put("Luxembourg", "LUX");
        mapper.put("Macedonia, Republic of", "MKD");
        mapper.put("Madagascar", "MDG");
        mapper.put("Malawi", "MWI");
        mapper.put("Malaysia", "MYS");
        mapper.put("Maldives", "MDV");
        mapper.put("Mali", "MLI");
        mapper.put("Malta", "MLT");
        mapper.put("Marshall Islands", "MHL");
        mapper.put("Martinique", "MTQ");
        mapper.put("Mauritania", "MRT");
        mapper.put("Mauritius", "MUS");
        mapper.put("Mayotte", "MYT");
        mapper.put("Mexico", "MEX");
        mapper.put("Micronesia, Federated States of", "FSM");
        mapper.put("Moldova", "MDA");
        mapper.put("Monaco", "MCO");
        mapper.put("Mongolia", "MNG");
        mapper.put("Montenegro", "MNE");
        mapper.put("Montserrat", "MSR");
        mapper.put("Morocco", "MAR");
        mapper.put("Mozambique", "MOZ");
        mapper.put("Myanmar", "MMR");
        mapper.put("Namibia", "NAM");
        mapper.put("Nauru", "NRU");
        mapper.put("Nepal", "NPL");
        mapper.put("Netherlands", "NLD");
        mapper.put("Netherlands Antilles", "ANT");
        mapper.put("New Caledonia", "NCL");
        mapper.put("New Zealand", "NZL");
        mapper.put("Nicaragua", "NIC");
        mapper.put("Niger", "NER");
        mapper.put("Nigeria", "NGA");
        mapper.put("Niue", "NIU");
        mapper.put("Norfolk Island", "NFK");
        mapper.put("Northern Mariana Islands", "MNP");
        mapper.put("Norway", "NOR");
        mapper.put("Oman", "OMN");
        mapper.put("Pakistan", "PAK");
        mapper.put("Palau", "PLW");
        mapper.put("Palestinian Territory, Occupied", "PSE");
        mapper.put("Panama", "PAN");
        mapper.put("Papua New Guinea", "PNG");
        mapper.put("Paraguay", "PRY");
        mapper.put("Peru", "PER");
        mapper.put("Philippines", "PHL");
        mapper.put("Pitcairn", "PCN");
        mapper.put("Poland", "POL");
        mapper.put("Portugal", "PRT");
        mapper.put("Puerto Rico", "PRI");
        mapper.put("Qatar", "QAT");
        mapper.put("Réunion", "REU");
        mapper.put("Romania", "ROU");
        mapper.put("Russian Federation", "RUS");
        mapper.put("Rwanda", "RWA");
        mapper.put("Saint-Barthélemy", "BLM");
        mapper.put("Saint Helena", "SHN");
        mapper.put("Saint Kitts and Nevis", "KNA");
        mapper.put("Saint Lucia", "LCA");
        mapper.put("Saint-Martin (French part)", "MAF");
        mapper.put("Saint Pierre and Miquelon", "SPM");
        mapper.put("Saint Vincent and Grenadines", "VCT");
        mapper.put("Samoa", "WSM");
        mapper.put("San Marino", "SMR");
        mapper.put("Sao Tome and Principe", "STP");
        mapper.put("Saudi Arabia", "SAU");
        mapper.put("Senegal", "SEN");
        mapper.put("Serbia", "SRB");
        mapper.put("Seychelles", "SYC");
        mapper.put("Sierra Leone", "SLE");
        mapper.put("Singapore", "SGP");
        mapper.put("Slovakia", "SVK");
        mapper.put("Slovenia", "SVN");
        mapper.put("Solomon Islands", "SLB");
        mapper.put("Somalia", "SOM");
        mapper.put("South Africa", "ZAF");
        mapper.put("South Georgia and the South Sandwich Islands", "SGS");
        mapper.put("South Sudan", "SSD");
        mapper.put("Spain", "ESP");
        mapper.put("Sri Lanka", "LKA");
        mapper.put("Sudan", "SDN");
        mapper.put("Suriname *", "SUR");
        mapper.put("Svalbard and Jan Mayen Islands", "SJM");
        mapper.put("Swaziland", "SWZ");
        mapper.put("Sweden", "SWE");
        mapper.put("Switzerland", "CHE");
        mapper.put("Syrian Arab Republic (Syria)", "SYR");
        mapper.put("Taiwan, Republic of China", "TWN");
        mapper.put("Tajikistan", "TJK");
        mapper.put("Tanzania *, United Republic of", "TZA");
        mapper.put("Thailand", "THA");
        mapper.put("Timor-Leste", "TLS");
        mapper.put("Togo", "TGO");
        mapper.put("Tokelau", "TKL");
        mapper.put("Tonga", "TON");
        mapper.put("Trinidad and Tobago", "TTO");
        mapper.put("Tunisia", "TUN");
        mapper.put("Turkey", "TUR");
        mapper.put("Turkmenistan", "TKM");
        mapper.put("Turks and Caicos Islands", "TCA");
        mapper.put("Tuvalu", "TUV");
        mapper.put("Uganda", "UGA");
        mapper.put("Ukraine", "UKR");
        mapper.put("United Arab Emirates", "ARE");
        mapper.put("United Kingdom", "GBR");
        mapper.put("United States of America", "USA");
        mapper.put("United States Minor Outlying Islands", "UMI");
        mapper.put("Uruguay", "URY");
        mapper.put("Uzbekistan", "UZB");
        mapper.put("Vanuatu", "VUT");
        mapper.put("Venezuela (Bolivarian Republic of)", "VEN");
        mapper.put("Viet Nam", "VNM");
        mapper.put("Virgin Islands, US", "VIR");
        mapper.put("Wallis and Futuna Islands", "WLF");
        mapper.put("Western Sahara", "ESH");
        mapper.put("Yemen", "YEM");
        mapper.put("Zambia", "ZMB");
        mapper.put("Zimbabwe", "ZWE");

    }
    
    public static String getISO3BitCountryCode(String... countryInfo) {
        
        if (mapper.isEmpty()) {
            init();
        }
        
        Set<String> countries = mapper.keySet();
        for (String info : countryInfo) {
            if (info.trim().equals("")) {
                continue;
            }
            for (String country : countries) {
                if (info.toLowerCase().contains(country.toLowerCase())) {
                    return mapper.get(country);
                }
            }
        }
        return "XXX";
    }
}
