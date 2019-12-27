package org.winplus.serial;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import org.winplus.serial.utils.SerialPort;
import org.winplus.serial.utils.SerialPortFinder;

public class Application extends android.app.Application {
	public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;

    private final static String TAG= Application.class.getSimpleName();

	public SerialPort getSerialPort() throws SecurityException, IOException,
			InvalidParameterException {

		for (String allDevice : mSerialPortFinder.getAllDevices()) {
			Log.d(TAG,"设备" + allDevice);
		}

		for (String allDevice : mSerialPortFinder.getAllDevicesPath()) {
			Log.d(TAG,"路径: " + allDevice);
		}


		if (mSerialPort == null) {
			String path = "/dev/ttyS3";
			int baudrate = 115200;

			/* Check parameters */
			if ((path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}

			/* Open the serial port */
			mSerialPort = new SerialPort(new File(path), baudrate, 0);
		}
		return mSerialPort;
	}

    public void closeSerialPort() {
            if (mSerialPort != null) {
                 mSerialPort.close();
                 mSerialPort = null;
            }
    }
}
