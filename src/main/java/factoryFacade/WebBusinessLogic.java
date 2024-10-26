package factoryFacade;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import businessLogic.BLFacade;
import configuration.ConfigXML;

public class WebBusinessLogic{
	public ConfigXML c; 
	public BLFacade BLFacadeInterface;
	public WebBusinessLogic(ConfigXML c){
		this.c=ConfigXML.getInstance();
	}
	public BLFacade getBLFacade(){
		return this.BLFacadeInterface;
	}
	public void createWebBusinessLogic(){
		try {
			String serviceName = "http://" + c.getBusinessLogicNode() + ":" + c.getBusinessLogicPort() + "/ws/"
					+ c.getBusinessLogicName() + "?wsdl";

			URL url = new URL(serviceName);

			// 1st argument refers to wsdl document above
			// 2nd argument is service name, refer to wsdl document above
			QName qname = new QName("http://businessLogic/", "BLFacadeImplementationService");

			Service service = Service.create(url, qname);

			this.BLFacadeInterface = service.getPort(BLFacade.class);
		}
		catch(Exception e) {
			System.out.println("Error in ApplicationLauncher: " + e.toString());
		}
	}
}

