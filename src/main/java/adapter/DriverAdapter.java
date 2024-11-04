package adapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import domain.Driver;
import domain.Ride;

public class DriverAdapter extends AbstractTableModel {
	protected Driver driver;
	protected String [] columnNames = new  String[] {"from", "to","date","places","price"};
	private final List<Ride> rides;
	  public DriverAdapter(Driver d) {
		    this.driver = d;
		    rides = new ArrayList<Ride>(d.getCreatedRides());
		  }
	  
	  public int getColumnCount(){
		  return 5;
	  }
	  public int getRowCount(){
		  return rides.size();
	  }
	  public Object getValueAt(int row, int col){
		  switch (col) {
		  	//case 0: return ((Object) rides.get(row));
		  	case 0: return ((Object) rides.get(row).getFrom());
		  	case 1: return ((Object) rides.get(row).getTo()); 
			case 2: return ((Object) rides.get(row).getDate()); 
			case 3: return ((Object) rides.get(row).getnPlaces()); 
			case 4: return ((Object) rides.get(row).getPrice()); 
		  }
		  return null;
	  }
	  public String getColumnName(int col){
		  switch (col) {
		  	case 0: return "from";
		  	case 1: return "to";
		  	case 2: return "date";
			case 3: return "places"; 
			case 4: return "price";
		  }
		  return null;
	  }

}
