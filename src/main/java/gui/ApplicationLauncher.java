package gui;

import java.net.URL;
import java.util.Locale;

import javax.swing.UIManager;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import configuration.ConfigXML;
import dataAccess.DataAccess;
import factoryFacade.FactoryFacade;
import businessLogic.BLFacade;
import businessLogic.BLFacadeImplementation;

public class ApplicationLauncher {

	public static void main(String[] args) {
		String isLocal = "local"; 
		BLFacade blFacade = new FactoryFacade().createBLFacade(isLocal); 
		System.out.println(blFacade);
	}
}
