package RangeIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public class RangeCities {
	CitiesIterator extendedIterator;
	Comparator comparator;
	List<String> cities =new Vector<String>();
	
	public RangeCities(List<String> cities) {
		this.cities = cities;
	}
	
	public void initializeCitiesIterator() {
		this.extendedIterator = new CitiesIterator(cities);
		this.comparator = new CitiesComparator();
	}
	public RangeCities getRangeCities() {
		return this;
	}
}