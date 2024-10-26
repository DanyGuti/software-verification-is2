package factoryFacade;

import businessLogic.BLFacadeImplementation;
import dataAccess.DataAccess;

public class LocalBusinessLogic{
	public DataAccess dataAccess;
	public BLFacadeImplementation BLFacadeImpl;

	public void createLocalBusinessLogic() {
		try {
			this.dataAccess = new DataAccess();
			this.BLFacadeImpl = new BLFacadeImplementation(this.dataAccess);
		}		
		catch(Exception e){
			System.out.println("Error in ApplicationLauncher: " + e.toString());
		}
	}
	public BLFacadeImplementation getBLFacade(){
		return this.BLFacadeImpl;
	}
}

