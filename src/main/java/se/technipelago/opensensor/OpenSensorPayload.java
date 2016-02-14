package se.technipelago.opensensor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data we post to opensensor.net.
 */
public class OpenSensorPayload implements Serializable {
    private String sid;
    private List<MeasuredData> data = new ArrayList<>();

    public OpenSensorPayload() {
    }

    public OpenSensorPayload(String sid) {
        this.sid = sid;
    }

    public OpenSensorPayload(String sid, List<MeasuredData> data) {
        this.sid = sid;
        this.data = new ArrayList<>(data);
    }

    public OpenSensorPayload(String sid, Date timestamp, Number value) {
        this.sid = sid;
        this.data.add(new MeasuredData(timestamp, value));

    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public List<MeasuredData> getData() {
        return data;
    }

    public OpenSensorPayload addValue(Date timestamp, Number value) {
        data.add(new MeasuredData(timestamp, value));
        return this;
    }

    public int size() {
        return data.size();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("PostPayload{");
        s.append(sid);
        s.append("} = ");
        s.append(String.valueOf(data.size()));
        if (!data.isEmpty()) {
            MeasuredData from = data.get(0);
            MeasuredData to = data.get(data.size() - 1);
            s.append(' ');
            s.append(from.toString());
            s.append(" - ");
            s.append(to.toString());
        }
        return s.toString();
    }

    public static class MeasuredData implements Serializable, Comparable<MeasuredData> {
        private Date timestamp;
        private BigDecimal value;

        public MeasuredData() {
        }

        public MeasuredData(Date timestamp, Number value) {
            this.timestamp = timestamp;
            this.value = BigDecimal.valueOf(value.doubleValue());
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = BigDecimal.valueOf(value);
        }

        public void setValue(Integer value) {
            this.value = BigDecimal.valueOf(value);
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public String toString() {
            return timestamp + "=" + value;
        }

        @Override
        public int compareTo(MeasuredData o) {
            return this.timestamp.compareTo(o.getTimestamp());
        }
    }
}
