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

import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.DataStore;
import se.technipelago.weather.vantagepro.VantageUtil;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class WeatherLinkImporter {

    private static final Pattern FILE_PATTERN = Pattern.compile("([12][09][\\d][\\d]\\-[01][\\d])\\.[wW][lL][kK]$");
    private static final DateFormat FILE_DATE = new SimpleDateFormat("yyyy-MM");
    private DataStore store = new DataStore();

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("usage: java " + WeatherLinkImporter.class.getName() + " directory");
        }
        WeatherLinkImporter imp = new WeatherLinkImporter();
        imp.startImport(new File(args[0]));
    }

    public void startImport(final File directory) {
        try {
            store.init();

            for (File file : directory.listFiles()) {
                DataInputStream in = null;
                try {
                    String filename = file.getName();
                    Matcher m = FILE_PATTERN.matcher(filename);
                    if (m.find()) {
                        Date filedate = FILE_DATE.parse(m.group(1));
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(filedate);
                        in = new DataInputStream(new FileInputStream(file));
                        System.out.println("Importing " + filename);
                        importMonth(in, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    System.err.println("Invalid file name: " + e.getMessage());
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            Logger.getLogger(WeatherLinkImporter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } finally {
            store.cleanup();
        }
    }

    public void importMonth(final DataInputStream in, int year, int month) throws IOException, SQLException {
        HeaderBlock header = HeaderBlock.createFromFile(in);
        WeatherLinkRecord[] records = new WeatherLinkRecord[header.totalRecords];
        System.out.println("Total number of records: " + header.totalRecords);
        for (int i = 0; i < header.totalRecords; i++) {
            byte recType = in.readByte();
            WeatherLinkRecord rec;
            switch (recType) {
                case WeatherLinkRecord.TYPE_WEATHER_RECORD:
                    rec = WeatherDataRecord.createFromFile(in);
                    break;
                case WeatherLinkRecord.TYPE_DAILY_SUMMARY1:
                    rec = DailySummary1.createFromFile(in);
                    break;
                case WeatherLinkRecord.TYPE_DAILY_SUMMARY2:
                    rec = DailySummary2.createFromFile(in);
                    break;
                default:
                    throw new IOException("Invalid record type: " + recType);
            }
            records[i] = rec;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        //cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        for (int i = 1; i < 32; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            DayIndex di = header.dayIndex[i];
            if (di.recordsInDay > 0) {
                int pos = di.startPos;
                int next = pos + di.recordsInDay;
                DailySummary1 sum1 = (DailySummary1) records[pos++];
                DailySummary2 sum2 = (DailySummary2) records[pos++];
                while (pos < next) {
                    WeatherDataRecord rec = (WeatherDataRecord) records[pos++];
                    store(cal.getTime(), sum1, sum2, rec);
                }
            }
        }

    }

    /**
     * Store archive data in MySQL table.
     * The table is created with the following statement:<br>
     * <pre>
     * CREATE TABLE archive (
     *   id int primary key auto_increment,
     *   ts datetime not null,
     *   temp_out float,
     *   temp_in float,
     *   hum_out smallint,
     *   hum_in smallint,
     *   barometer int,
     *   rain float,
     *   rain_rate float,
     *   wind_avg float,
     *   wind_dir smallint,
     *   wind_high float,
     *   solar smallint,
     *   uv float
     * );
     * </pre>
     * 
     * @param date
     * @param sum1
     * @param sum2
     * @param rec
     */
    private void store(Date date, DailySummary1 sum1, DailySummary2 sum2, WeatherDataRecord rec) throws SQLException {
        Date ts = new Date(date.getTime() + rec.packedTime * 60 * 1000);
        double temp_out = (int) (VantageUtil.fahrenheit2celcius(rec.outTemp / 10.0) * 10) / 10.0;
        double temp_in = (int) (VantageUtil.fahrenheit2celcius(rec.inTemp / 10.0) * 10) / 10.0;
        int hum_out = rec.outHum / 10;
        int hum_in = rec.inHum / 10;
        int barometer = VantageUtil.inchHg2millibar(rec.barometer / 1000.0);
        double rain; // set below.
        double rain_rate; // set below.
        double wind_avg = Math.round(VantageUtil.mph2ms(rec.windSpeed / 10) * 10) / 10.0;
        int wind_dir = getDirection(rec.windDir);
        double wind_high = Math.round(VantageUtil.mph2ms(rec.highWindSpeed / 10) * 10) / 10.0;
        int solar = rec.solar;
        double uv = rec.uv != -1 ? rec.uv / 10.0 : -1;

        byte rain_collector_type = (byte) (rec.rain >>> 28 & 0x0f);
        int rainticks = rec.rain >>> 16 & 0xfff;
        switch (rain_collector_type) {
            case 2:
                rain = Math.round(0.2 * rainticks * 10) / 10.0;
                rain_rate = Math.round(0.2 * rec.highRainRate * 10) / 10.0;
                break;
            case 0:
                rain = Math.round(VantageUtil.inch2mm(0.1 * rainticks) * 10) / 10.0;
                rain_rate = Math.round(VantageUtil.inch2mm(0.1 * rec.highRainRate) * 10) / 10.0;
                break;
            case 1:
                rain = Math.round(VantageUtil.inch2mm(0.01 * rainticks) * 10) / 10.0;
                rain_rate = Math.round(VantageUtil.inch2mm(0.01 * rec.highRainRate) * 10) / 10.0;
                break;
            case 3:
                rain = (double) rainticks;
                rain_rate = (double) rec.highRainRate;
                break;
            case 6:
                rain = Math.round(0.1 * rainticks * 10) / 10.0;
                rain_rate = Math.round(0.1 * rec.highRainRate * 10) / 10.0;
                break;
            default:
                throw new IllegalStateException("Invalid rain collector type found: " + rain_collector_type);
        }

        if (solar == -32768) {
            solar = -1;
        }
        /*System.out.println(
                ts + ";" +
                temp_out + ";" +
                temp_in + ";" +
                hum_out + ";" +
                hum_in + ";" +
                barometer + ";" +
                rain + ";" +
                rain_rate + ";" +
                wind_avg + ";" +
                wind_dir + ";" +
                wind_high + ";" +
                solar + ";" +
                uv);*/

        ArchiveRecord archive = new ArchiveRecord();
        archive.setTimestamp(ts);
        archive.setOutsideTemperature(temp_out);
        archive.setInsideTemperature(temp_in);
        archive.setOutsideHumidity(hum_out);
        archive.setInsideHumidity(hum_in);
        archive.setBarometer(barometer);
        archive.setRainFall(rain);
        archive.setRainRateHigh(rain_rate);
        archive.setWindSpeedAvg(wind_avg);
        archive.setWindDirection(wind_dir);
        archive.setWindSpeedHigh(wind_high);
        archive.setSolarRadiation(solar);
        archive.setUvIndex(uv);

        store.insertData(archive);
    }

    private static int readUnsignedByte(DataInputStream in) throws IOException {
        return ((int) in.readByte()) & 0xff;
    }

    private static short readShort(DataInputStream in) throws IOException {
        short s = in.readShort();
        return Short.reverseBytes(s);
    }

    private static int readUnsignedShort(DataInputStream in) throws IOException {
        int i = in.readUnsignedShort();
        return Integer.reverseBytes(i);
    }

    private static int readInt(DataInputStream in) throws IOException {
        int i = in.readInt();
        return Integer.reverseBytes(i);
    }
    private static final int[] WIND_DIRECTION_DEGREES = new int[16];

    static {
        float d = 0.0f;
        for (int i = 0; i < 16; i++) {
            WIND_DIRECTION_DEGREES[i] = Math.round(d);
            d += 22.5;
        }
    }

    private int getDirection(byte dir) {
        return dir != -1 ? WIND_DIRECTION_DEGREES[dir] : -1;
    }

    private static class DayIndex {

        private short recordsInDay;
        private int startPos;

        public static DayIndex createFromFile(DataInputStream in) throws IOException {
            DayIndex di = new DayIndex();
            di.recordsInDay = readShort(in);
            di.startPos = readInt(in);
            return di;
        }
    }

    private static class HeaderBlock {

        private char[] idCode = new char[16];
        private int totalRecords;
        private DayIndex[] dayIndex = new DayIndex[32]; // Index 0 is not used.

        public static HeaderBlock createFromFile(DataInputStream in) throws IOException {
            byte[] buf = new byte[16];

            if (in.read(buf) != 16) {
                throw new IOException("Unexpected EOF");
            }

            String id = new String(buf, 0, 6);
            if (!"WDAT5.".equals(id)) {
                throw new IOException("Not a weatherlink data file");
            }

            System.out.println("WeatherLink " + (int) buf[14] + "." + (int) buf[15]);
            HeaderBlock header = new HeaderBlock();
            for (int i = 0; i < 16; i++) {
                header.idCode[i] = (char) buf[i];
            }
            header.totalRecords = readInt(in);
            for (int i = 0; i < 32; i++) {
                header.dayIndex[i] = DayIndex.createFromFile(in);
            }
            return header;
        }
    }

    /**
     * 88 bytes.
     */
    private static class DailySummary1 implements WeatherLinkRecord {

        private byte reserved;
        private short dataSpan; // total # of minutes accounted for by physical records for this day.
        private short highOutTemp; // tenths of a degree F
        private short lowOutTemp;
        private short highInTemp;
        private short lowInTemp;
        private short avgOutTemp;
        private short avgInTemp;
        private short highChill;
        private short lowChill;
        private short highDew;
        private short lowDew;
        private short avgChill;
        private short avgDew;
        private short highOutHum;
        private short lowOutHum;
        private short highInHum;
        private short lowInHum;
        private short avgOutHum;
        private short highBar;
        private short lowBar;
        private short avgBar;
        private short highSpeed;
        private short avgSpeed;
        private short dailyWindRunTotal;
        private short high10MinSpeed;
        private byte dirHighSpeed;
        private byte dirHigh10MinSpeed;
        private short dailyRainTotal;
        private short highRainRate;
        private short dailyUVDose;
        private short highUV;
        private byte[] timeValues = new byte[27];

        public static DailySummary1 createFromFile(DataInputStream in) throws IOException {
            DailySummary1 rec = new DailySummary1();
            byte[] buf = new byte[87];
            in.read(buf);
            return rec;
        }

        public byte getType() {
            return WeatherLinkRecord.TYPE_DAILY_SUMMARY1;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("Summary1 " + lowOutTemp + "-" + highOutTemp + "/" + avgOutTemp);
            return buf.toString();
        }
    }

    /**
     * 88 bytes.
     */
    private static class DailySummary2 implements WeatherLinkRecord {

        private byte[] bytes = new byte[87];

        public static DailySummary2 createFromFile(DataInputStream in) throws IOException {
            DailySummary2 rec = new DailySummary2();
            in.read(rec.bytes);
            return rec;
        }

        public byte getType() {
            return WeatherLinkRecord.TYPE_DAILY_SUMMARY2;
        }

        @Override
        public String toString() {
            return "Summary2";
        }
    }

    /**
     * 88 bytes.
     */
    private static class WeatherDataRecord implements WeatherLinkRecord {

        private byte archiveInterval;
        private byte iconFlags;
        private byte moreFlags;
        private short packedTime;
        private short outTemp;
        private short highOutTemp;
        private short lowOutTemp;
        private short inTemp;
        private short barometer;
        private short outHum;
        private short inHum;
        private int rain; // unsiged short!
        private short highRainRate;
        private short windSpeed;
        private short highWindSpeed;
        private byte windDir;
        private byte highWindDir;
        private short numWindSamples;
        private short solar;
        private short highSolar;
        private byte uv;
        private byte highUV;
        private byte[] leafTemp = new byte[4]; // (whole degrees F) + 90
        private short[] newSensors = new short[7];
        private int forecast; // unsigned byte!
        private byte et; // in thousands of an inch.
        private byte[] soilTemp = new byte[6];
        private byte[] soilMoisture = new byte[6];
        private byte[] leafWetness = new byte[4];
        private byte[] extraTemp = new byte[7]; // (whole degrees F) + 90
        private byte[] extraHum = new byte[7]; // whole percent

        public static WeatherDataRecord createFromFile(DataInputStream in) throws IOException {
            WeatherDataRecord rec = new WeatherDataRecord();
            rec.archiveInterval = in.readByte();
            rec.iconFlags = in.readByte();
            rec.moreFlags = in.readByte();
            rec.packedTime = readShort(in);
            rec.outTemp = readShort(in);
            rec.highOutTemp = readShort(in);
            rec.lowOutTemp = readShort(in);
            rec.inTemp = readShort(in);
            rec.barometer = readShort(in);
            rec.outHum = readShort(in);
            rec.inHum = readShort(in);
            rec.rain = readUnsignedShort(in);
            rec.highRainRate = readShort(in);
            rec.windSpeed = readShort(in);
            rec.highWindSpeed = readShort(in);
            rec.windDir = in.readByte();
            rec.highWindDir = in.readByte();
            rec.numWindSamples = readShort(in);
            rec.solar = readShort(in);
            rec.highSolar = readShort(in);
            rec.uv = in.readByte();
            rec.highUV = in.readByte();
            in.read(rec.leafTemp);
            for (int i = 0; i < rec.newSensors.length; i++) {
                rec.newSensors[i] = readShort(in);
            }
            rec.forecast = readUnsignedByte(in);
            rec.et = in.readByte();
            in.read(rec.soilTemp);
            in.read(rec.soilMoisture);
            in.read(rec.leafWetness);
            in.read(rec.extraTemp);
            in.read(rec.extraHum);

            return rec;
        }

        public byte getType() {
            return WeatherLinkRecord.TYPE_WEATHER_RECORD;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("Record " + lowOutTemp + "-" + highOutTemp + "/" + outTemp);
            return buf.toString();
        }
    }
}
