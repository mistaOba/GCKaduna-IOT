/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.weatherstation;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import static java.util.Arrays.fill;

public class WeatherStationActivity extends Activity {
    private static final String TAG = WeatherStationActivity.class.getSimpleName();


    // Default LED brightness
    private static final int LEDSTRIP_BRIGHTNESS = 1;
    private AlphanumericDisplay mDisplay;
    private Apa102 mLedstrip;
    private Bmx280SensorDriver mEnvironmentalSensorDriver;
    private SensorManager mSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Weather Station Started Mercy");
        mSensorManager = getSystemService(SensorManager.class);

        //TODO: Register peripheral drivers here
        //Initialize temperature/pressure sensors
        try {
            Log.d(TAG, "Starting I2C BMP");

            mEnvironmentalSensorDriver = RainbowHat.createSensorDriver();
            //Register data with frame work
            mEnvironmentalSensorDriver.registerTemperatureSensor();
            mEnvironmentalSensorDriver.registerPressureSensor();
            Log.d(TAG, "Initialized I2C BMP280");

        } catch (IOException e) {
            throw new RuntimeException("Error initializing BMP280", e);
        }
        //Initialize 14 segment display
        try {
            mDisplay = RainbowHat.openDisplay();
            mDisplay.setEnabled(true);
            mDisplay.display("1234");
            Log.d(TAG, "Initialized I2C Display");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing display", e);
        }

        // Initialize LED strip
        try {
            mLedstrip = RainbowHat.openLedStrip();
            mLedstrip.setBrightness(LEDSTRIP_BRIGHTNESS);
            int[] colors = new int[7];
            fill(colors, Color.RED);
            mLedstrip.write(colors);
            Log.d(TAG, "Initialized SPI LED strip");
            // Because of a known APA102 issue, write the initial value twice.
            mLedstrip.write(colors);


            Log.d(TAG, "Initialized SPI LED strip");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing LED strip", e);
        }



    }


    @Override
    protected void onStart() {
        super.onStart();

        //TODO: Register for sensor events here
        // Register the BMP280 temperature sensor
        Sensor temperature = mSensorManager
                .getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE).get(0);
        mSensorManager.registerListener(mSensorEventListener, temperature,
                SensorManager.SENSOR_DELAY_NORMAL);
        // Register the BMP280 pressure sensor
        Sensor pressure = mSensorManager
                .getDynamicSensorList(Sensor.TYPE_PRESSURE).get(0);
        mSensorManager.registerListener(mSensorEventListener, pressure,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //TODO: Unregister for sensor events here
        super.onStop();

        mSensorManager.unregisterListener(mSensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (mEnvironmentalSensorDriver != null) {
            try {
                mEnvironmentalSensorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing sensors", e);
            } finally {
                mEnvironmentalSensorDriver = null;
            }
        }
    }



    /**
     * Update the 7-segment display with the latest temperature value.
     *
     * @param temperature Latest temperature value.
     */
    private void updateTemperatureDisplay(float temperature) {
        //TODO: Add code to write a value to the segment display
        if (mDisplay != null) {
            try{
                mDisplay.display(temperature);
            } catch (IOException e){
                Log.e(TAG, "Error updating display", e);
            }
        }
    }

    /**
     * Update LED strip based on the latest pressure value.
     *
     * @param pressure Latest pressure value.
     */
    private void updateBarometerDisplay(float pressure) {
        //TODO: Add code to send color data to the LED strip
        if (mLedstrip != null) {
            try {
                Log.d(TAG, "The pressure value is " + Float.toString(pressure));

                int[] colors = RainbowUtil.getWeatherStripColors(980);
                Log.d(TAG, "The color value is " + Arrays.toString(colors));
                ///print the value of colors

               // Arrays.fill(colors,Color.RED);
                mLedstrip.write(colors);
            } catch (IOException e) {
                Log.e(TAG, "Error updating ledstrip", e);
            }
        }
    }
    // Callback when SensorManager delivers new data.
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final float value = event.values[0];

            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                updateTemperatureDisplay(value);
            }
            if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                updateBarometerDisplay(value);
                    Log.e(TAG, "initializing update");

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "accuracy changed: " + accuracy);
        }
    };



}
