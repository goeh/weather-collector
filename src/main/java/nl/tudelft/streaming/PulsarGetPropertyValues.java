/*
 *  Copyright 2021 Saverio Guzzo.
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
package nl.tudelft.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Saverio Guzzo <saverio.g.guzzo@gmail.com>
 */
public class PulsarGetPropertyValues extends Properties {

    //static?
    public Properties getPropValues() {
        Properties prop = new Properties();

        try (InputStream input = PulsarGetPropertyValues.class.getClassLoader().getResourceAsStream("pulsar.properties")) {

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }

            //load a properties file from class path, inside static method
            prop.load(input);


        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }
}
