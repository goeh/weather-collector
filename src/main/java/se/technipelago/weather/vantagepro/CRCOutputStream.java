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

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class CRCOutputStream extends OutputStream {

    private CRC16 crc = new CRC16();
    private final OutputStream out;
    
    public CRCOutputStream(final OutputStream out) {
        this.out = out;
    }

    /**
     * Update a running CRC with a single byte. 
     */
    @Override
    public void write(int b) throws IOException {
        out.write(b);
        crc.add((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    /**
     * Update a running CRC. 
     */
    @Override
    public void write(byte[] buf, int offset, int length) throws IOException {
        out.write(buf, offset, length);
        for (int n = offset; n < offset + length; n++) {
            crc.add(buf[n]);
        }
    }

    public void writeCRC() throws IOException {
        out.write(crc.getCrc());
    }

    public byte[] getCRC() {
        return crc.getCrc();
    }
    
    public void resetCRC() {
        crc.reset();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }
}
