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
 * Constants used by various commands.
 * 
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public interface Constants {

    public static final byte ACK = 0x06;
    public static final byte NAK = 0x21;
    
    public static final byte WIND_DIR_N = 0;
    public static final byte WIND_DIR_NNE = 1;
    public static final byte WIND_DIR_NE = 2;
    public static final byte WIND_DIR_ENE = 3;
    public static final byte WIND_DIR_E = 4;
    public static final byte WIND_DIR_ESE = 5;
    public static final byte WIND_DIR_SE = 6;
    public static final byte WIND_DIR_SSE = 7;
    public static final byte WIND_DIR_S = 8;
    public static final byte WIND_DIR_SSW = 9;
    public static final byte WIND_DIR_SW = 10;
    public static final byte WIND_DIR_WSW = 11;
    public static final byte WIND_DIR_W = 12;
    public static final byte WIND_DIR_WNW = 13;
    public static final byte WIND_DIR_NW = 14;
    public static final byte WIND_DIR_NNW = 15;
    public static final byte WIND_DIR_UNKNOWN = (byte) 255;
    /**
     * Names of wind direction, index 0-15 clockwise (N = 0, NE = 2, S = 8, NNW = 15)
     */
    public static final String[] WIND_DIR_NAMES = {
        "N",
        "NNE",
        "NE",
        "ENE",
        "E",
        "ESE",
        "SE",
        "SSE",
        "S",
        "SSW",
        "SW",
        "WSW",
        "W",
        "WNW",
        "NW",
        "NNW"
    };
}
