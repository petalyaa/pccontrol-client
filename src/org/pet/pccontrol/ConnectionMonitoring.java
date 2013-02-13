package org.pet.pccontrol;

public class ConnectionMonitoring extends Thread {

	public ConnectionMonitoring(){
		
	}
	
	public void run(){
		while(true){
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
