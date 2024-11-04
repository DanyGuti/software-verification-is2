package RangeIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CitiesIterator implements ExtendedIterator {
	int start = 0;
	int end;
	List<String> cities;
	public CitiesIterator(List<String> cities) {
		this.cities = cities;
		this.end = cities.size();
	}
	@Override
	public Object previous() {
		if (this.hasPrevious()) {
			return (Object) cities.get(--this.end);
		}
		return null;
	}
	//true if ther is a previous element 
	@Override
	public boolean hasPrevious() {
		return end > 0;
	}
	//It is placed in the first element
	@Override
	public void goFirst() {
		this.start = 0;
	}
	// It is placed in the last element
	@Override
	public void goLast() {
		this.end = cities.size();
	}
	@Override
	public boolean hasNext() {
		return start < this.cities.size();
	}
	@Override
	public Object next() {
	    if (this.hasNext()) {
	        return (Object) cities.get(this.start++);
		}
	    return null;
	}
}