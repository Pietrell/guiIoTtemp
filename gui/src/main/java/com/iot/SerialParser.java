package com.iot;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.iot.SerialCommChannel;

public class SerialParser  {
	private SerialCommChannel ch;

	public SerialParser(SerialCommChannel c) throws Exception{
		this.ch = c;
	}

	public ArrayList<Float> getNewChartData(ArrayList<String> rawSerial){
		ArrayList<Float> data = new ArrayList<>();
		for (String item : rawSerial) {
			//System.out.println(item);
			String[] parts = item.split("-");
			System.out.println(parts[0]);
			System.out.println(parts[1]);
			if(parts.length==2 && parts[0]=="waterlevel" ){
				if(parts[1]!="")
				if(!data.add(Float.parseFloat(parts[1]) )){
					System.out.println("non viene inserito niene");
						
				}

			}
		}
		return data;
	}

	public float getServoPosition(ArrayList<String> rawSerial){
		float pos= 360;
		String[] msg;
		//inversed read of data to take only the last opening of the valve
		for (int i = rawSerial.size() - 1; i >0  ; i--) {
			msg = rawSerial.get(i).split("-");
			System.out.print(msg.length);
			if( msg[0]=="servo"){
				pos = Float.parseFloat(msg[1].toString());
				return pos;
			}
		}
		return 0 ;
	}
	   
	public int getState(ArrayList<String> rawSerial){
		int state = -1 ;
		String[] msg;
		//inversed read of data to take only the last state
		for (int i = rawSerial.size() - 1; i >0  ; i--) {
			msg = rawSerial.get(i).split("-");
			if(msg[0]=="state"){

				switch (msg[1]) {
					case "normal":
						return 0;
					case "prealarm":
					return 1;
				
					case "alarm":
					return 2;
				
				
					default:

						return -1;
				}
				
			}
		}
		return -1 ;
	}

	public boolean getSmartLight(ArrayList<String> rawSerial){
		int state = -1 ;
		String[] msg;
		//inversed read of data to take only the last state
		for (int i = rawSerial.size() - 1; i >0  ; i--) {
			msg = rawSerial.get(i).split("-");
			if(msg[0]=="smartlight"){
				switch (msg[1]) {
					case "off":
						return false;
					case "on":
						return true;
					default:
						return false;
				}				
			}
		}
		return false ;
	}
}