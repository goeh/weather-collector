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
package se.technipelago.weather.dataimport;

/**
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public interface WeatherLinkRecord {
    public static final byte TYPE_WEATHER_RECORD = 1;
    public static final byte TYPE_DAILY_SUMMARY1 = 2;
    public static final byte TYPE_DAILY_SUMMARY2 = 3;
    
    byte getType();
}
