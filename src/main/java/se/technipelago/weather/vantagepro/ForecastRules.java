/*
 *  Copyright 2006 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package se.technipelago.weather.vantagepro;

/**
 * Utility class to get the forecast text messages that are
 * displayed on the Vantage Pro(2) console.
 * 
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class ForecastRules {

    private static final String[] FORECAST_RULE = new String[197];

    static {
        FORECAST_RULE[  0] = "Mostly clear and cooler.";
        FORECAST_RULE[  1] = "Mostly clear with little temperature change.";
        FORECAST_RULE[  2] = "Mostly clear for 12 hours with little temperature change.";
        FORECAST_RULE[  3] = "Mostly clear for 12 to 24 hours and cooler.";
        FORECAST_RULE[  4] = "Mostly clear with little temperature change.";
        FORECAST_RULE[  5] = "Partly cloudy and cooler.";
        FORECAST_RULE[  6] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[  7] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[  8] = "Mostly clear and warmer.";
        FORECAST_RULE[  9] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 10] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 11] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 12] = "Increasing clouds and warmer. Precipitation possible within 24 to 48 hours.";
        FORECAST_RULE[ 13] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 14] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 15] = "Increasing clouds with little temperature change. Precipitation possible within 24 hours.";
        FORECAST_RULE[ 16] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 17] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 18] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 19] = "Increasing clouds with little temperature change. Precipitation possible within 12 hours.";
        FORECAST_RULE[ 20] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 21] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 22] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 23] = "Increasing clouds and warmer. Precipitation possible within 24 hours.";
        FORECAST_RULE[ 24] = "Mostly clear and warmer. Increasing winds.";
        FORECAST_RULE[ 25] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 26] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 27] = "Increasing clouds and warmer. Precipitation possible within 12 hours. Increasing winds.";
        FORECAST_RULE[ 28] = "Mostly clear and warmer. Increasing winds.";
        FORECAST_RULE[ 29] = "Increasing clouds and warmer.";
        FORECAST_RULE[ 30] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 31] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 32] = "Increasing clouds and warmer. Precipitation possible within 12 hours. Increasing winds.";
        FORECAST_RULE[ 33] = "Mostly clear and warmer. Increasing winds.";
        FORECAST_RULE[ 34] = "Increasing clouds and warmer.";
        FORECAST_RULE[ 35] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 36] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 37] = "Increasing clouds and warmer. Precipitation possible within 12 hours. Increasing winds.";
        FORECAST_RULE[ 38] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 39] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 40] = "Mostly clear and warmer. Precipitation possible within 48 hours.";
        FORECAST_RULE[ 41] = "Mostly clear and warmer.";
        FORECAST_RULE[ 42] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 43] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 44] = "Increasing clouds with little temperature change. Precipitation possible within 24 to 48 hours.";
        FORECAST_RULE[ 45] = "Increasing clouds with little temperature change.";
        FORECAST_RULE[ 46] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 47] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 48] = "Increasing clouds and warmer. Precipitation possible within 12 to 24 hours.";
        FORECAST_RULE[ 49] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 50] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 51] = "Increasing clouds and warmer. Precipitation possible within 12 to 24 hours. Windy.";
        FORECAST_RULE[ 52] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 53] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 54] = "Increasing clouds and warmer. Precipitation possible within 12 to 24 hours. Windy.";
        FORECAST_RULE[ 55] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 56] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 57] = "Increasing clouds and warmer. Precipitation possible within 6 to 12 hours.";
        FORECAST_RULE[ 58] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 59] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 60] = "Increasing clouds and warmer. Precipitation possible within 6 to 12 hours. Windy.";
        FORECAST_RULE[ 61] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 62] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 63] = "Increasing clouds and warmer. Precipitation possible within 12 to 24 hours. Windy.";
        FORECAST_RULE[ 64] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 65] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 66] = "Increasing clouds and warmer. Precipitation possible within 12 hours.";
        FORECAST_RULE[ 67] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 68] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 69] = "Increasing clouds and warmer. Precipitation likley.";
        FORECAST_RULE[ 70] = "Clearing and cooler. Precipitation ending within 6 hours.";
        FORECAST_RULE[ 71] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 72] = "Clearing and cooler. Precipitation ending within 6 hours.";
        FORECAST_RULE[ 73] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 74] = "Clearing and cooler. Precipitation ending within 6 hours.";
        FORECAST_RULE[ 75] = "Partly cloudy and cooler.";
        FORECAST_RULE[ 76] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 77] = "Mostly clear and cooler.";
        FORECAST_RULE[ 78] = "Clearing and cooler. Precipitation ending within 6 hours.";
        FORECAST_RULE[ 79] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 80] = "Clearing and cooler. Precipitation ending within 6 hours.";
        FORECAST_RULE[ 81] = "Mostly clear and cooler.";
        FORECAST_RULE[ 82] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 83] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 84] = "Increasing clouds with little temperature change. Precipitation possible within 24 hours.";
        FORECAST_RULE[ 85] = "Mostly cloudy and cooler. Precipitation continuing.";
        FORECAST_RULE[ 86] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 87] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 88] = "Mostly cloudy and cooler. Precipitation likely.";
        FORECAST_RULE[ 89] = "Mostly cloudy with little temperature change. Precipitation continuing.";
        FORECAST_RULE[ 90] = "Mostly cloudy with little temperature change. Precipitation likely.";
        FORECAST_RULE[ 91] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 92] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 93] = "Increasing clouds and cooler. Precipitation possible and windy within 6 hours.";
        FORECAST_RULE[ 94] = "Increasing clouds with little temperature change. Precipitation possible and windy within 6 hours.";
        FORECAST_RULE[ 95] = "Mostly cloudy and cooler. Precipitation continuing. Increasing winds.";
        FORECAST_RULE[ 96] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[ 97] = "Mostly clear with little temperature change.";
        FORECAST_RULE[ 98] = "Mostly cloudy and cooler. Precipitation likely. Increasing winds.";
        FORECAST_RULE[ 99] = "Mostly cloudy with little temperature change. Precipitation continuing. Increasing winds.";
        FORECAST_RULE[100] = "Mostly cloudy with little temperature change. Precipitation likely. Increasing winds.";
        FORECAST_RULE[101] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[102] = "Mostly clear with little temperature change.";
        FORECAST_RULE[103] = "Increasing clouds and cooler. Precipitation possible within 12 to 24 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[104] = "Increasing clouds with little temperature change. Precipitation possible within 12 to 24 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[105] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[106] = "Mostly clear with little temperature change.";
        FORECAST_RULE[107] = "Increasing clouds and cooler. Precipitation possible within 6 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[108] = "Increasing clouds with little temperature change. Precipitation possible within 6 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[109] = "Mostly cloudy and cooler. Precipitation ending within 12 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[110] = "Mostly cloudy and cooler. Possible wind shift to the W, NW, or N.";
        FORECAST_RULE[111] = "Mostly cloudy with little temperature change. Precipitation ending within 12 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[112] = "Mostly cloudy with little temperature change. Possible wind shift to the W, NW, or N.";
        FORECAST_RULE[113] = "Mostly cloudy and cooler. Precipitation ending within 12 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[114] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[115] = "Mostly clear with little temperature change.";
        FORECAST_RULE[116] = "Mostly cloudy and cooler. Precipitation possible within 24 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[117] = "Mostly cloudy with little temperature change. Precipitation ending within 12 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[118] = "Mostly cloudy with little temperature change. Precipitation possible within 24 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[119] = "Clearing, cooler and windy. Precipitation ending within 6 hours.";
        FORECAST_RULE[120] = "Clearing, cooler and windy.";
        FORECAST_RULE[121] = "Mostly cloudy and cooler. Precipitation ending within 6 hours. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[122] = "Mostly cloudy and cooler. Windy with possible wind shift o the W, NW, or N.";
        FORECAST_RULE[123] = "Clearing, cooler and windy.";
        FORECAST_RULE[124] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[125] = "Mostly clear with little temperature change.";
        FORECAST_RULE[126] = "Mostly cloudy with little temperature change. Precipitation possible within 12 hours. Windy.";
        FORECAST_RULE[127] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[128] = "Mostly clear with little temperature change.";
        FORECAST_RULE[129] = "Increasing clouds and cooler. Precipitation possible within 12 hours, possibly heavy at times. Windy.";
        FORECAST_RULE[130] = "Mostly cloudy and cooler. Precipitation ending within 6 hours. Windy.";
        FORECAST_RULE[131] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[132] = "Mostly clear with little temperature change.";
        FORECAST_RULE[133] = "Mostly cloudy and cooler. Precipitation possible within 12 hours. Windy.";
        FORECAST_RULE[134] = "Mostly cloudy and cooler. Precipitation ending in 12 to 24 hours.";
        FORECAST_RULE[135] = "Mostly cloudy and cooler.";
        FORECAST_RULE[136] = "Mostly cloudy and cooler. Precipitation continuing, possible heavy at times. Windy.";
        FORECAST_RULE[137] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[138] = "Mostly clear with little temperature change.";
        FORECAST_RULE[139] = "Mostly cloudy and cooler. Precipitation possible within 6 to 12 hours. Windy.";
        FORECAST_RULE[140] = "Mostly cloudy with little temperature change. Precipitation continuing, possibly heavy at times. Windy.";
        FORECAST_RULE[141] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[142] = "Mostly clear with little temperature change.";
        FORECAST_RULE[143] = "Mostly cloudy with little temperature change. Precipitation possible within 6 to 12 hours. Windy.";
        FORECAST_RULE[144] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[145] = "Mostly clear with little temperature change.";
        FORECAST_RULE[146] = "Increasing clouds with little temperature change. Precipitation possible within 12 hours, possibly heavy at times. Windy.";
        FORECAST_RULE[147] = "Mostly cloudy and cooler. Windy.";
        FORECAST_RULE[148] = "Mostly cloudy and cooler. Precipitation continuing, possibly heavy at times. Windy.";
        FORECAST_RULE[149] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[150] = "Mostly clear with little temperature change.";
        FORECAST_RULE[151] = "Mostly cloudy and cooler. Precipitation likely, possibly heavy at times. Windy.";
        FORECAST_RULE[152] = "Mostly cloudy with little temperature change. Precipitation continuing, possibly heavy at times. Windy.";
        FORECAST_RULE[153] = "Mostly cloudy with little temperature change. Precipitation likely, possibly heavy at times. Windy.";
        FORECAST_RULE[154] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[155] = "Mostly clear with little temperature change.";
        FORECAST_RULE[156] = "Increasing clouds and cooler. Precipitation possible within 6 hours. Windy.";
        FORECAST_RULE[157] = "Increasing clouds with little temperature change. Precipitation possible within 6 hours. Windy";
        FORECAST_RULE[158] = "Increasing clouds and cooler. Precipitation continuing. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[159] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[160] = "Mostly clear with little temperature change.";
        FORECAST_RULE[161] = "Mostly cloudy and cooler. Precipitation likely. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[162] = "Mostly cloudy with little temperature change. Precipitation continuing. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[163] = "Mostly cloudy with little temperature change. Precipitation likely. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[164] = "Increasing clouds and cooler. Precipitation possible within 6 hours. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[165] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[166] = "Mostly clear with little temperature change.";
        FORECAST_RULE[167] = "Increasing clouds and cooler. Precipitation possible within 6 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[168] = "Increasing clouds with little temperature change. Precipitation possible within 6 hours. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[169] = "Increasing clouds with little temperature change. Precipitation possible within 6 hours possible wind shift to the W, NW, or N.";
        FORECAST_RULE[170] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[171] = "Mostly clear with little temperature change.";
        FORECAST_RULE[172] = "Increasing clouds and cooler. Precipitation possible within 6 hours. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[173] = "Increasing clouds with little temperature change. Precipitation possible within 6 hours. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[174] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[175] = "Mostly clear with little temperature change.";
        FORECAST_RULE[176] = "Increasing clouds and cooler. Precipitation possible within 12 to 24 hours. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[177] = "Increasing clouds with little temperature change. Precipitation possible within 12 to 24 hours. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[178] = "Mostly cloudy and cooler. Precipitation possibly heavy at times and ending within 12 hours. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[179] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[180] = "Mostly clear with little temperature change.";
        FORECAST_RULE[181] = "Mostly cloudy and cooler. Precipitation possible within 6 to 12 hours, possibly heavy at times. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[182] = "Mostly cloudy with little temperature change. Precipitation ending within 12 hours. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[183] = "Mostly cloudy with little temperature change. Precipitation possible within 6 to 12 hours, possibly heavy at times. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[184] = "Mostly cloudy and cooler. Precipitation continuing.";
        FORECAST_RULE[185] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[186] = "Mostly clear with little temperature change.";
        FORECAST_RULE[187] = "Mostly cloudy and cooler. Precipitation likely. Windy with possible wind shift to the W, NW, or N.";
        FORECAST_RULE[188] = "Mostly cloudy with little temperature change. Precipitation continuing.";
        FORECAST_RULE[189] = "Mostly cloudy with little temperature change. Precipitation likely.";
        FORECAST_RULE[190] = "Partly cloudy with little temperature change.";
        FORECAST_RULE[191] = "Mostly clear with little temperature change.";
        FORECAST_RULE[192] = "Mostly cloudy and cooler. Precipitation possible within 12 hours, possibly heavy at times. Windy.";
        FORECAST_RULE[193] = "FORECAST REQUIRES 3 HOURS OF RECENT DATA";
        FORECAST_RULE[194] = "Mostly clear and cooler.";
        FORECAST_RULE[195] = "Mostly clear and cooler.";
        FORECAST_RULE[196] = "Mostly clear and cooler.";
    }

    private ForecastRules() {
    // No instances allowed.
    }

    /**
     * Return the forecast text associated with a specific rule number.
     * @param rule forecast rule as stored in the LOOP packet (0-196)
     * @return forecast text message
     */
    public static String getText(int rule) {
        return FORECAST_RULE[rule];
    }
}
