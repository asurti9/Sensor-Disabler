package com.mrchandler.disableprox.xposed.sensormodifications.mock;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import com.mrchandler.disableprox.util.Constants;
import com.mrchandler.disableprox.xposed.sensormodifications.SensorModificationMethod;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * A modification method for API levels 1 - 17. Mocks the values by changing
 * what is returned by ListenerDelegate.onSensorChanged.
 *
 * @author Wardell Bagby
 */

public class MockSensorValuesModificationMethod extends SensorModificationMethod {

    @Override
    public void modifySensor(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
                "android.hardware.SystemSensorManager$ListenerDelegate",
                lpparam.classLoader, "onSensorChanged", Sensor.class,
                float[].class, long[].class, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        //Sensor sensor = (Sensor) param.args[0];
                        SensorEvent actualValues = (SensorEvent) param.thisObject;
                        Context context = AndroidAppHelper.currentApplication();
                        Log.d("FLANCE : ", "afterHookedMethod: modifySensor: Main method");
                        //Use processName here always. Not packageName.
                        if (!isPackageAllowedToSeeTrueSensor(lpparam.processName, actualValues.sensor, context) && getSensorStatus(actualValues.sensor, context) == Constants.SENSOR_STATUS_MOCK_VALUES) {
                            // Get the mock values from the settings.
                            float[] values = getSensorValues(actualValues.sensor, context);
                            //if (lpparam.processName != "com.mrchandler.disableprox") {
                            if (actualValues.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                                values[0] = actualValues.values[0] - values[0];
                                values[1] = actualValues.values[1] - values[1];
                                values[2] = actualValues.values[2] - values[2];
                            }
                            if (actualValues.sensor.getType() == Sensor.TYPE_LIGHT) {
                                values[0] = actualValues.values[0] - values[0];
                            }
                            //}
                            //noinspection SuspiciousSystemArraycopy
                            System.arraycopy(values, 0, param.args[1], 0, values.length);
                        }
                    }
                }
        );
    }
}
