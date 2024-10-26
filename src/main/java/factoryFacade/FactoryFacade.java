package factoryFacade;

import java.util.Locale;

import javax.swing.UIManager;
import businessLogic.BLFacade;
import configuration.ConfigXML;
import gui.MainGUI;

public class FactoryFacade{
	public ConfigXML c;
	public MainGUI a;

	public FactoryFacade () {
		this.c=ConfigXML.getInstance();
		Locale.setDefault(new Locale(c.getLocale()));
		this.a=new MainGUI();
		a.setVisible(true);
	}
	public BLFacade createBLFacade(String BLFacadeType){
		BLFacade appFacadeInterface;
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

			if (BLFacadeType.equals("local")) {
				LocalBusinessLogic lbl = new LocalBusinessLogic();
				lbl.createLocalBusinessLogic();
				appFacadeInterface = lbl.getBLFacade();
				
			}
			
			else { // If remote
				WebBusinessLogic wbl = new WebBusinessLogic(c);
				wbl.createWebBusinessLogic();
				appFacadeInterface = wbl.getBLFacade();
			}

			MainGUI.setBussinessLogic(appFacadeInterface);
			MainGUI a = new MainGUI();
			a.setVisible(true);
			
			return appFacadeInterface;

		} catch (Exception e) {
			// a.jLabelSelectOption.setText("Error: "+e.toString());
			// a.jLabelSelectOption.setForeground(Color.RED);

			System.out.println("Error in ApplicationLauncher: " + e.toString());
		}
		// a.pack();
		return null;
	}
}