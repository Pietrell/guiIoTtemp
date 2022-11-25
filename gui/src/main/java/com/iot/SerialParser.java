package com.iot;

import java.util.ArrayList;

public class SerialParser {

	public SerialParser() throws Exception { }

	public ArrayList<Float> getNewChartData(ArrayList<String> rawSerial) {

		ArrayList<Float> data = new ArrayList<>();
		for (String item : rawSerial) {
			// System.out.println(item);
			String[] parts = item.split("-");

			System.out.println("valore di " + parts[0] + "valore letto: " + parts[1]);
			parts[0].trim();
			parts[0].toLowerCase();

			if (parts[0].equals(("waterlevel"))) {
				if (parts[1] != "")
					if (!data.add(Float.parseFloat(parts[1]))) {
						System.out.println("non viene inserito niene");
					}
			}
		}

		System.out.println("-----------------INIZIO STAMPA DATI ------------------");
		for (Float float1 : data) {
			System.out.println(float1.toString());
		}
		System.out.println("--------------FINE STAMPA DATI --------------");
		return data;
	}

	public float getServoPosition(ArrayList<String> rawSerial) {
		float pos = 360;
		String[] msg;
		// inversed read of data to take only the last opening of the valve
		for (int i = rawSerial.size() - 1; i > 0; i--) {
			msg = rawSerial.get(i).split("-");
			System.out.print(msg.length);
			msg[0].trim();
			msg[0].toLowerCase();
			if (msg[0] == "servo") {
				pos = Float.parseFloat(msg[1].toString());
				return pos;
			}
		}
		return 0;
	}

	public String getState(ArrayList<String> rawSerial) {

		String[] msg;
		// inversed read of data to take only the last state
		for (int i = rawSerial.size() - 1; i > 0; i--) {
			msg = rawSerial.get(i).split("-");
			msg[0].trim();
			msg[0].toLowerCase();
			if (msg[0] == "state") {
				return msg[1];
			}
		}
		return "unknown";
	}

	public String getSmartLight(ArrayList<String> rawSerial) {

		String[] msg;
		// inversed read of data to take only the last state
		for (int i = rawSerial.size() - 1; i > 0; i--) {
			msg = rawSerial.get(i).split("-");
			msg[0].trim();
			msg[0].toLowerCase();
			if (msg[0] == "smartlight") {
				return msg[1];
			}
		}
		return "unknown";
	}
}