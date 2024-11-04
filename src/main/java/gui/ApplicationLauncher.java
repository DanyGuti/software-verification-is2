package gui;

import RangeIterator.ExtendedIterator;
import factoryFacade.FactoryFacade;
import businessLogic.BLFacade;

public class ApplicationLauncher {

	public static void main(String[] args) {
		String isLocal = "local"; 
		BLFacade blFacade = new FactoryFacade().createBLFacade(isLocal); 
		System.out.println(blFacade);
		ExtendedIterator i = blFacade.getDepartingCitiesIterator();
		String c; 
		System.out.println("_____________________"); 
		System.out.println("FROM LAST TO FIRST"); 
		i.goLast(); // Go to last element 
		while (i.hasPrevious()) { 
			c = (String) i.previous(); 
			System.out.println(c); 
		} 
		System.out.println(); 
		System.out.println("_____________________"); 
		System.out.println("FROM FIRST TO LAST"); 
		i.goFirst(); // Go to first element 
		while (i.hasNext()) { 
			c = (String) i.next(); 
			System.out.println(c); 
		} 

	}
}
