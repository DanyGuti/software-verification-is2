package RangeIterator;

import java.util.Comparator;

public class CitiesComparator implements Comparator {
	@Override
	public int compare(Object o1, Object o2) {
		String city1 = (String)o1;
		String city2 = (String)o2;
		return (city1.compareTo(city2));
	}
}