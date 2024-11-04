package adapter;

import businessLogic.BLFacade;
import domain.Driver;
import factoryFacade.FactoryFacade;

public class Main{
	public static void main(String[] args) { 
	// the BL is local 
	boolean isLocal = true; 
	BLFacade blFacade = new FactoryFacade().createBLFacade("local"); 
	Driver d= blFacade. getDriver("Urtzi"); 
	DriverTableGUI dt=new DriverTableGUI(d); 
	dt.setVisible(true); 
	}
}
