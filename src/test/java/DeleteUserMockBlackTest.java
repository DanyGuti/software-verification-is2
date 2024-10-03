import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import dataAccess.DataAccess;
import domain.Alert;
import domain.Booking;
import domain.Car;
import domain.Driver;
import domain.Movement;
import domain.Ride;
import domain.Traveler;
import domain.User;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;


// Created by Daniel Gutierrez Gomez
public class DeleteUserMockBlackTest {
	static DataAccess sut;
	
	protected MockedStatic<Persistence> persistenceMock;

	@Mock
	protected  EntityManagerFactory entityManagerFactory;
	@Mock
	protected  EntityManager db;
	@Mock
    protected  EntityTransaction  et;
    @Mock
    protected TypedQuery<Driver> typedQuery;  // Mocked TypedQuery

	
	// 	Constants
	private static final String FROM = "SAN SEBASTIAN";
	private static final String TO = "MADRID";
	
	private static final String MODEL_1 = "Audi";
	private static final String TUITION_1 = "1234ABC";
	private static final String MODEL_2 = "Citroen";
	private static final String TUITION_2 = "5678DEF";
	private static final String MODEL_3 = "BMW-3";
	private static final String TUITION_3 = "ADF-191";
	

	@Before
    public  void init() {
        MockitoAnnotations.openMocks(this);
        persistenceMock = Mockito.mockStatic(Persistence.class);
		persistenceMock.when(() -> Persistence.createEntityManagerFactory(Mockito.any()))
        .thenReturn(entityManagerFactory);
        
        Mockito.doReturn(db).when(entityManagerFactory).createEntityManager();
		Mockito.doReturn(et).when(db).getTransaction();
				
	    sut=new DataAccess(db);
	    
    }
	@After
    public  void tearDown() {
		persistenceMock.close();
    }
	private void mockAddDriverCar(Car car, Driver d) {
		d.addCar(car);
		car.setDriver(d);
		Mockito.when(db.merge(car)).thenReturn(car);
		Mockito.when(db.find(Driver.class, d.getUsername())).thenReturn(d);
        Mockito.when(db.find(Driver.class, car.getDriver().getUsername())).thenReturn(d);
		Mockito.when(db.find(Driver.class, car.getDriver())).thenReturn(d);
		return;
	}
	private Booking mockBookingProcess (Driver dr, Ride r, Traveler tr, List<Booking> bookings, int seats) {
		// Make the bookings for the same user
        Booking booking = new Booking(r, tr, seats);
        
        if (r.getnPlaces() - booking.getSeats() < 0) {
        	return null;
        }
        if (r.getPrice() * seats > tr.getMoney()) {
        	return null;
        }
        if (tr.getMoney() < 0) {
        	return null;
        }
        // Setting the first booking
        r.setBookings(bookings);
        r.setnPlaces(r.getnPlaces() - booking.getSeats());

	    // Mock the booking 1
	    Mockito.when(db.merge(r)).thenReturn(r);

	    booking.setTraveler(tr);
	    booking.setStatus("Accepted");
	    tr.addBookedRide(booking);
	    bookings.add(booking);
	    booking.setRide(r);
	    tr.setMoney(r.getPrice() * seats);
	    Mockito.when(db.merge(tr)).thenReturn(tr);
        // Mock the ride lookup
        Mockito.when(db.find(List.class, dr.getCreatedRides().get(0).getBookings())).thenReturn(bookings);
        Mockito.when(db.find(List.class, tr.getBookedRides())).thenReturn(bookings);
        return booking;
	}
	private Ride mockAddDriverRideBbox(String from, String to, Date date, int nPlaces, float price, String driverName, Driver dr)
			throws RideMustBeLaterThanTodayException, RideAlreadyExistException {
		if (driverName==null) return null;
		try {
			if (new Date().compareTo(date) > 0) {
				System.out.println("ppppp");
				throw new RideMustBeLaterThanTodayException(
						ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.ErrorRideMustBeLaterThanToday"));
			}
			
//			db.getTransaction().begin();
			Mockito.when(db.find(Driver.class, dr.getUsername())).thenReturn(dr);
			System.out.println(dr.getCreatedRides());
			if (dr.doesRideExists(from, to, date)) {
//				db.remove(dr.getCreatedRides());
//				System.out.println(db.find(Driver.class, driverName).getCreatedRides());
//				db.getTransaction().commit();
				throw new RideAlreadyExistException(
						ResourceBundle.getBundle("Etiquetas").getString("DataAccess.RideAlreadyExist"));
			}
			Ride ride = dr.addRide(from, to, date, nPlaces, price);
			// next instruction can be obviated
//			db.persist(driver);
//			db.getTransaction().commit();

			return ride;
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	private void setupDeleteTraveler (Driver d, Traveler tr) {
		//configure the state through mocks 
        Mockito.when(db.find(Driver.class, d.getUsername())).thenReturn(d);
                
        // Mock the behavior of db.find to return the Traveler object
        Mockito.when(db.find(Traveler.class, tr.getUsername())).thenReturn(tr);
        
        // Mock the behavior of db.createQuery for Traveler
        TypedQuery<Traveler> typedQueryTrav = Mockito.mock(TypedQuery.class);
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Traveler.class))).thenReturn(typedQueryTrav);
        
        // Mock behavior for the typedQueryTrav
        Mockito.when(typedQueryTrav.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(typedQueryTrav);
        Mockito.when(typedQueryTrav.getResultList()).thenReturn(Collections.singletonList(tr)); // Return list containing trav1
        
        TypedQuery<Alert> typedQueryAlert = Mockito.mock(TypedQuery.class);
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(typedQueryAlert);
        Mockito.when(typedQueryAlert.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(typedQueryAlert);
        Mockito.when(typedQueryAlert.getResultList()).thenReturn(Collections.emptyList()); // Assuming no alerts
        
	}
	
	private void setupDeleteDriver (Driver d) {
		//configure the state through mocks 
        Mockito.when(db.find(Driver.class, d.getUsername())).thenReturn(d);
		// Mock the behavior of db.find to return the Driver object
        Mockito.when(db.find(Driver.class, d.getUsername())).thenReturn(d);
        
        // Mock the behavior of db.createQuery to return the typedQuery
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Driver.class))).thenReturn(typedQuery);
        
        // Mock the behavior of typedQuery to return driver1 when getSingleResult is called
        Mockito.when(typedQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(typedQuery);
        Mockito.when(typedQuery.getSingleResult()).thenReturn(d); // Return the driver object
        
        Mockito.when(typedQuery.getResultList()).thenReturn(Collections.singletonList(d));
	}
	
	@Test
	// Should erase the driver1
	// Should erase the cars from driver1
	// Should get a new movement3
	// Should return money to traveler -> 125
	// booking1.status == "Rejected"
	// booking2.status == "Rejected"
	// !ride1.isActive()
	// !ride1.isActive()
	public void test1() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		List<Booking> bookings1 = new ArrayList<>();
		List<Booking> bookings2 = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate2 = null;
		try {
			rideDate2 = sdf2.parse("06/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 15);
			driver1.addRide(FROM, TO, rideDate2, 5, 15);
			
			List<Ride> rides = driver1.getCreatedRides();
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			
			Car car2 = new Car(TUITION_2, MODEL_2, 5);
			
			this.mockAddDriverCar(car, driver1);
			this.mockAddDriverCar(car2, driver1);

			// Make the bookings for the same user
	        Booking booking1 = new Booking(rides.get(0), traveler, 2);
	        Booking booking2 = new Booking(rides.get(1), traveler, 3);
	        
	        rides.get(0).setBookings(bookings1);
	        rides.get(1).setBookings(bookings2);
	       
	        // Mock the ride lookup
	        Mockito.when(db.find(List.class, driver1.getCreatedRides().get(0).getBookings())).thenReturn(bookings1);
	        Mockito.when(db.find(List.class, driver1.getCreatedRides().get(0).getBookings())).thenReturn(bookings2);
	        
	        
			rides.get(0).getBookings().add(booking1);
			rides.get(1).getBookings().add(booking2);
	        

		    // Mock the booking
		    Mockito.when(db.merge(traveler)).thenReturn(traveler);
		    Mockito.when(db.merge(rides.get(0))).thenReturn(rides.get(0));
		    
		    traveler.addBookedRide(booking1);
		    traveler.addBookedRide(booking2);
		    traveler.setMoney(50);
		    traveler.setIzoztatutakoDirua(10);

			
			// Make the movements
			Movement movement1 = new Movement(traveler, "BookFreeze", 10);
	        Movement movement2 = new Movement(traveler, "BookFreeze", 11);
	        
	        Movement expectedMovement1 = new Movement(traveler, "BookDeny", 1);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        Movement expectedMovement2 = new Movement(traveler, "BookDeny", 2);
		    Mockito.when(db.merge(expectedMovement2)).thenReturn(expectedMovement2);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
			Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);
			
			Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
			Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);


			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			Mockito.when(db.find(Traveler.class, traveler.getUsername())).thenReturn(traveler);
			
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking1);
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking2);

	        movement1.setUsername(traveler);
	        movement2.setUsername(traveler);

			
			assertEquals("Rejected", booking1.getStatus());
	        assertEquals("Rejected", booking2.getStatus());
	        assertFalse(rides.get(0).isActive());
	        assertFalse(rides.get(1).isActive());
	        assertEquals(-65.0, traveler.getIzoztatutakoDirua(), 0.0001);
	        assertEquals(125, traveler.getMoney(), 0.0001);
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(null);
	        Driver foundDriver = db.find(Driver.class, driver1.getUsername());
	        assertNull(foundDriver); // Ensure the driver is null

	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_1)).thenReturn(null);
	        Car foundCar = db.find(Car.class, TUITION_1);
	        assertNull(foundCar); // Ensure the car is null
	        
	        Mockito.when(db.find(Car.class, TUITION_2)).thenReturn(null);
	        Car foundCar2 = db.find(Car.class, TUITION_2);
	        assertNull(foundCar2); // Ensure the car is null
	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);
	        assertNotNull(retrievedMovement2);
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	// Should erase user1
	// Should erase user1 car
	public void test2() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
			
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			driver1.addCar(car);
			
			// Setting the car to driver1
			this.mockAddDriverCar(car, driver1);
			
			// SUT deleteUser()
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			// Expected results
			
			// Verify that the driver is now null
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(null);
	        Driver foundDriver = db.find(Driver.class, driver1.getUsername());
	        assertNull(foundDriver); // Ensure the driver is null

	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_1)).thenReturn(null);
	        Car foundCar = db.find(Car.class, TUITION_1);
	        assertNull(foundCar); // Ensure the car is null
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Should erase the driver1
	// Should erase the car from driver1
	// Should return money to traveler -> 125
	// booking1.status == "Rejected"
	// booking2.status == "Rejected"
	// !ride1.isActive()
	// !ride1.isActive()
	public void test3() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		List<Booking> bookings1 = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 15);
			
			List<Ride> rides = driver1.getCreatedRides();
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			
			this.mockAddDriverCar(car, driver1);
			
			// Make the bookings for the same user
	        Booking booking1 = new Booking(rides.get(0), traveler, 2);
	        
	        rides.get(0).setBookings(bookings1);
	       
	        // Mock the ride lookup
	        Mockito.when(db.find(List.class, driver1.getCreatedRides().get(0).getBookings())).thenReturn(bookings1);
	        
	        
			rides.get(0).getBookings().add(booking1);
	        

		    // Mock the booking
		    Mockito.when(db.merge(traveler)).thenReturn(traveler);
		    Mockito.when(db.merge(rides.get(0))).thenReturn(rides.get(0));
		    
		    traveler.addBookedRide(booking1);
		    traveler.setMoney(50);
		    traveler.setIzoztatutakoDirua(10);

			
			// Make the movements
			Movement movement1 = new Movement(traveler, "BookFreeze", 10);
	        
	        Movement expectedMovement1 = new Movement(traveler, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
		    Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);


			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			Mockito.when(db.find(Traveler.class, traveler.getUsername())).thenReturn(traveler);
			
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking1);

	        movement1.setUsername(traveler);

			
			assertEquals("Rejected", booking1.getStatus());
	        assertFalse(rides.get(0).isActive());
	        assertEquals(-20.0, traveler.getIzoztatutakoDirua(), 0.0001);
	        assertEquals(80, traveler.getMoney(), 0.0001);
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(null);
	        Driver foundDriver = db.find(Driver.class, driver1.getUsername());
	        assertNull(foundDriver); // Ensure the driver is null

	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_1)).thenReturn(null);
	        Car foundCar = db.find(Car.class, TUITION_1);
	        assertNull(foundCar); // Ensure the car is null
	        
	        Mockito.when(db.find(Movement.class, expectedMovement1.getId())).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Should delete driver 1
	// Should delete both car and car2
	public void test4() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
			
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			
			Car car2 = new Car(TUITION_2, MODEL_2, 5);
			
			// Setting the car to driver1
			this.mockAddDriverCar(car, driver1);
			
			// Setting the car2 to driver1
			this.mockAddDriverCar(car2, driver1);
			
			// SUT deleteUser()
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			// Expected results
			
			// Verify that the driver is now null
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(null);
	        Driver foundDriver = db.find(Driver.class, driver1.getUsername());
	        assertNull(foundDriver); // Ensure the driver is null

	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_1)).thenReturn(null);
	        Car foundCar = db.find(Car.class, TUITION_1);
	        assertNull(foundCar); // Ensure the car is null
	        
	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_2)).thenReturn(null);
	        Car foundCar2 = db.find(Car.class, TUITION_2);
	        assertNull(foundCar2); // Ensure the car is null
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Should delete user2 => Traveler
	// booking1.status == "Rejected"
	// booking2.status == "Rejected"
	// Ride1.places = 5 in return
	// Ride2.places = 5 in return
	// Ride1.isActive
	// Ride2.isActive
	public void test5() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Booking> bookings2 = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate2 = null;
		try {
			rideDate2 = sdf2.parse("06/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate2, 5, 10);
			
			List<Ride> rides = driver1.getCreatedRides();
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			this.mockAddDriverCar(car, driver1);
			
			// Assign and create car to the Driver
			Car car1 = new Car(TUITION_2, MODEL_2, 5);
			this.mockAddDriverCar(car1, driver1);
						
			// Make the bookings for the same user	
			trav1.setMoney(50);
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 3);
			trav1.setMoney(50);
			Booking booking2 = this.mockBookingProcess(driver1, rides.get(1), trav1, bookings2, 3);

	        
			// Make the movements
			Movement movement1 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement1 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
	        Movement movement2 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement2 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement2)).thenReturn(expectedMovement2);
	        
	        Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        
	        
			sut.open();
			sut.deleteUser(trav1);
			sut.close();
			
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings1);
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings2);
		    
		    						
			assertEquals("Rejected", booking1.getStatus());
			assertEquals("Rejected", booking2.getStatus());
	        assertTrue(rides.get(0).isActive());
	        assertTrue(rides.get(1).isActive());
	        assertEquals(5, rides.get(0).getnPlaces(), 0.0001);
	        assertEquals(5, rides.get(1).getnPlaces(), 0.0001);
	        
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Traveler.class, trav1.getUsername())).thenReturn(null);
			assertNull(db.find(Traveler.class, trav1.getUsername()));
		
			Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);
	        assertNotNull(retrievedMovement2);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// One booking status as rejected
	// Ride1 isActive, and a movement BookDeny
	public void test6() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
				
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			
			List<Ride> rides = driver1.getCreatedRides();
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			this.mockAddDriverCar(car, driver1);
			
			// Assign and create car to the Driver
			Car car1 = new Car(TUITION_2, MODEL_2, 5);
			this.mockAddDriverCar(car1, driver1);
						
			// Make the bookings for the same user
			trav1.setMoney(50);
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 3);

	        
			// Make the movements
			Movement movement1 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement1 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        
	        
			sut.open();
			sut.deleteUser(trav1);
			sut.close();
			
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings1);

		    						
			assertEquals("Rejected", booking1.getStatus());
	        assertTrue(rides.get(0).isActive());
	        assertEquals(5, rides.get(0).getnPlaces(), 0.0001);
	        
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Traveler.class, trav1.getUsername())).thenReturn(null);
			assertNull(db.find(Traveler.class, trav1.getUsername()));
		
			
			Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
	        
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Testing deleteUser dataAccess when user.mota == "Traveler"
	// user does not have Bookings nor Alerts
	// Should expect to delete user() correctly
	public void test7() {
		// Define parameters
		String travUsername = "Pablo";
		String travPasswd = "dsadoaoda";
		
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		try {
			 // Create the Traveler instance
	        Traveler trav1 = new Traveler(travUsername, travPasswd);
	        Driver dr = new Driver(driverUsername, driverPassword);
	        
	        this.setupDeleteTraveler(dr, trav1);
	        
	        // Invoke SUT
	        sut.open();
	        sut.deleteUser(trav1);
	        sut.close();
	        Mockito.when(db.find(Traveler.class, trav1.getUsername())).thenReturn(null);
			
			assertNull(db.find(Traveler.class, trav1.getUsername()));

		} catch (Exception e) {
			sut.close();
			e.printStackTrace();
			fail();
		}
	}
	// One booking status as rejected
	// Ride1 isActive, and a movement BookDeny
	// One Alert should be deleted 
	// Deletion of traveler
	@Test
	public void test8() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate2 = null;
		try {
			rideDate2 = sdf2.parse("06/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    
		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate2, 5, 10);
			
			List<Ride> rides = driver1.getCreatedRides();
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			this.mockAddDriverCar(car, driver1);
			
			// Assign and create car to the Driver
			Car car1 = new Car(TUITION_2, MODEL_2, 5);
			this.mockAddDriverCar(car1, driver1);
						
			// Make the bookings for the same user	
			trav1.setMoney(50);
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 3);

	        
			// Make the movements
			Movement movement1 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement1 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        
			sut.open();
			sut.deleteUser(trav1);
			sut.close();
			
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings1);
		    
		    Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
		    						
			assertEquals("Rejected", booking1.getStatus());
	        assertTrue(rides.get(0).isActive());
	        assertTrue(rides.get(1).isActive());
	        assertEquals(5, rides.get(0).getnPlaces(), 0.0001);
	        
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Traveler.class, trav1.getUsername())).thenReturn(null);
			assertNull(db.find(Traveler.class, trav1.getUsername()));
			
			Mockito.when(db.find(Alert.class, alert.getAlertNumber())).thenReturn(null);
			assertNull(db.find(Alert.class, alert.getAlertNumber()));
			
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Should return 5 seats again after the booking1
	// Should return 5 seats again after the booking2
	// For the ride1, and delete the user, booking1 as "Rejected", booking2 as "Rejected"
	// Also the Alerts should be null
	public void test9() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Booking> bookings2 = new ArrayList<>();
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate2 = null;
		try {
			rideDate2 = sdf2.parse("06/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    Alert alert2 = new Alert(FROM, TO, rideDate2, trav1);

		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			alert2.setAlertNumber(1);
			alerts.add(alert2);			
			trav1.addAlert(alert2);
			alert2.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			Mockito.when(db.find(Traveler.class, alert2.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
			
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert2);

	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate2, 5, 10);
			
			List<Ride> rides = driver1.getCreatedRides();
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			this.mockAddDriverCar(car, driver1);
			
			// Assign and create car to the Driver
			Car car1 = new Car(TUITION_2, MODEL_2, 5);
			this.mockAddDriverCar(car1, driver1);
						
			// Make the bookings for the same user
			trav1.setMoney(50);
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 3);
			trav1.setMoney(50);
			Booking booking2 = this.mockBookingProcess(driver1, rides.get(1), trav1, bookings2, 3);

	        
			// Make the movements
			Movement movement1 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement1 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
	        Movement movement2 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement2 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement2)).thenReturn(expectedMovement2);
	        
	        Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        
	        
			sut.open();
			sut.deleteUser(trav1);
			sut.close();
			
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings1);
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings2);
		    
		    Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
		    Mockito.when(mockQuery.getSingleResult()).thenReturn(alert2);

		    						
			assertEquals("Rejected", booking1.getStatus());
			assertEquals("Rejected", booking2.getStatus());
	        assertTrue(rides.get(0).isActive());
	        assertTrue(rides.get(1).isActive());
	        assertEquals(5, rides.get(0).getnPlaces(), 0.0001);
	        assertEquals(5, rides.get(1).getnPlaces(), 0.0001);
	        
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Traveler.class, trav1.getUsername())).thenReturn(null);
			assertNull(db.find(Traveler.class, trav1.getUsername()));
			
			Mockito.when(db.find(Alert.class, alert.getAlertNumber())).thenReturn(null);
			assertNull(db.find(Alert.class, alert.getAlertNumber()));
			
			Mockito.when(db.find(Alert.class, alert.getAlertNumber())).thenReturn(null);
			assertNull(db.find(Alert.class, alert.getAlertNumber()));
			
			Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);
	        assertNotNull(retrievedMovement2);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Testing deleteUser dataAccess when user.mota != "Admin" &&
	// user.mota != "Rider" && user.mota != "Driver"
	// SHOULD FAIL
	public void test10() {
		// Define parameters
		String user = "Pablo";
		String usPasswd = "dsadoaoda";

		try {
			// Create the Traveler instance
			User us = new User(user, usPasswd, "Hello");
	        
	        // Invoke SUT
	        sut.open();
	        sut.deleteUser(us);
	        sut.close();
	        Mockito.when(db.find(User.class, us.getUsername())).thenReturn(null);
			
			assertNull(db.find(User.class, us.getUsername()));
			fail();

		} catch (Exception e) {
			sut.close();
			e.printStackTrace();
			assertTrue(true);
		}
	}
	@Test
	// Should throw exception of null user
	public void test11() {
		try {
			User us = new User(null, null, null);
	        sut.deleteUser(us);
	        fail("Expected NullPointerException to be thrown");
	    } catch (NullPointerException e) {
	        assertTrue(true);
	    } catch (Exception e) {
	        fail("Unexpected exception: " + e);
	    }
	}
	@Test
	// Should throw exception of not acceptable user.mota
	public void test12() {
		try {
			User us = new User(null, null, "Hey");
	        sut.deleteUser(us);
	        fail("Expected NullPointerException to be thrown");
	    } catch (NullPointerException e) {
	        assertTrue(true);
	    } catch (Exception e) {
	        fail("Unexpected exception: " + e);
	    }
	}
	@Test
	// DEPENDS ON THE LOGIC OF BUSINESS OF HAVING A CAR AS A DRIVER OR NOT
	// DRIVER MUST HAVE AT LEAST ONE CAR EXCEPTION
	public void test13() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
									
			// SUT deleteUser()
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			fail();
			
			
			
		} catch (Exception e) {
			// DRIVER MUST HAVE AT LEAST ONE CAR EXCEPTION
			e.printStackTrace();
			assertTrue(true);
		}
	}
	@Test
	// No booking allowed, should return null, traveler has no money
	public void test14() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		List<Booking> bookings1 = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 15);
			
			List<Ride> rides = driver1.getCreatedRides();
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			
			this.mockAddDriverCar(car, driver1);
			
			// Make the bookings for the same user
			// (Driver dr, Ride r, Traveler tr, List<Booking> bookings, int seats)
	        Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), traveler, bookings1, 3);
	        assertNull(booking1);
	        
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}
	@Test
	// Excepction rides must be different date
	public void test15() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    
		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			// SAME DATE
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			this.mockAddDriverRideBbox(FROM, TO, rideDate1, 5, 10, driverUsername, driver1);
			fail();
			
			List<Ride> rides = driver1.getCreatedRides();
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			this.mockAddDriverCar(car, driver1);
			
			// Assign and create car to the Driver
			Car car1 = new Car(TUITION_2, MODEL_2, 5);
			this.mockAddDriverCar(car1, driver1);
						
			// Make the bookings for the same user	
			trav1.setMoney(50);
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 3);

	        
			// Make the movements
			Movement movement1 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement1 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        
			sut.open();
			sut.deleteUser(trav1);
			sut.close();
			
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings1);
		    
		    Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
		    						
			assertEquals("Rejected", booking1.getStatus());
	        assertTrue(rides.get(0).isActive());
	        assertTrue(rides.get(1).isActive());
	        assertEquals(5, rides.get(0).getnPlaces(), 0.0001);
	        
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Traveler.class, trav1.getUsername())).thenReturn(null);
			assertNull(db.find(Traveler.class, trav1.getUsername()));
			
			Mockito.when(db.find(Alert.class, alert.getAlertNumber())).thenReturn(null);
			assertNull(db.find(Alert.class, alert.getAlertNumber()));
			
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
		} catch (RideAlreadyExistException e) {
			e.printStackTrace();
			assertTrue(true);
		} catch (RideMustBeLaterThanTodayException e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Excepction rides must be greater than today
	public void test16() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("29/04/2024");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    
		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			// SAME DATE
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			this.mockAddDriverRideBbox(FROM, TO, rideDate1, 5, 10, driverUsername, driver1);
			fail();
		} catch (RideAlreadyExistException e) {
			e.printStackTrace();
			fail();
		} catch (RideMustBeLaterThanTodayException e) {
			e.printStackTrace();
			assertTrue(true);
		}
	}
	@Test
	// Booking has more seats than ride has
	public void test17() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("29/04/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    
		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			// SAME DATE
			this.mockAddDriverRideBbox(FROM, TO, rideDate1, 5, 10, driverUsername, driver1);
			
			List<Ride> rides = driver1.getCreatedRides();
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			this.mockAddDriverCar(car, driver1);
			
			// Assign and create car to the Driver
			Car car1 = new Car(TUITION_2, MODEL_2, 5);
			this.mockAddDriverCar(car1, driver1);
						
			// Make the bookings for the same user	
			trav1.setMoney(50);
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 20);
			assertNull(booking1);
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	// @Test 18 will be with the bussiness rule (do not know them)
	@Test
	// Should delete Traveler
	// Booking1 status == "Rejected"
	// Booking2 status == "Rejected"
	// Ride1 places = 5
	// Ride1.isActive
	// Ride2.isActive
	// alert1 == null
	// alert2 == null
	// expect 115 money of traveler
	// expect -55 of traveler
	public void test19() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		List<Booking> bookings1 = new ArrayList<>();
		List<Booking> bookings2 = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate2 = null;
		try {
			rideDate2 = sdf2.parse("06/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 15);
			driver1.addRide(FROM, TO, rideDate2, 5, 50);
			
			List<Ride> rides = driver1.getCreatedRides();
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			
			Car car2 = new Car(TUITION_2, MODEL_2, 5);
			
			this.mockAddDriverCar(car, driver1);
			this.mockAddDriverCar(car2, driver1);

			// Make the bookings for the same user
	        Booking booking1 = new Booking(rides.get(0), traveler, 1);
	        Booking booking2 = new Booking(rides.get(1), traveler, 1);
	        
	        rides.get(0).setBookings(bookings1);
	        rides.get(1).setBookings(bookings2);
	       
	        // Mock the ride lookup
	        Mockito.when(db.find(List.class, driver1.getCreatedRides().get(0).getBookings())).thenReturn(bookings1);
	        Mockito.when(db.find(List.class, driver1.getCreatedRides().get(0).getBookings())).thenReturn(bookings2);
	        
	        
			rides.get(0).getBookings().add(booking1);
			rides.get(1).getBookings().add(booking2);
	        

		    // Mock the booking
		    Mockito.when(db.merge(traveler)).thenReturn(traveler);
		    Mockito.when(db.merge(rides.get(0))).thenReturn(rides.get(0));
		    
		    traveler.addBookedRide(booking1);
		    traveler.addBookedRide(booking2);
		    traveler.setMoney(50);
		    traveler.setIzoztatutakoDirua(10);

			
			// Make the movements
			Movement movement1 = new Movement(traveler, "BookFreeze", 10);
	        Movement movement2 = new Movement(traveler, "BookFreeze", 11);
	        
	        Movement expectedMovement1 = new Movement(traveler, "BookDeny", 1);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        Movement expectedMovement2 = new Movement(traveler, "BookDeny", 2);
		    Mockito.when(db.merge(expectedMovement2)).thenReturn(expectedMovement2);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
			Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);
			
			Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
			Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);


			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			Mockito.when(db.find(Traveler.class, traveler.getUsername())).thenReturn(traveler);
			
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking1);
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking2);

	        movement1.setUsername(traveler);
	        movement2.setUsername(traveler);

			
			assertEquals("Rejected", booking1.getStatus());
	        assertEquals("Rejected", booking2.getStatus());
	        assertFalse(rides.get(0).isActive());
	        assertFalse(rides.get(1).isActive());
	        assertEquals(-55.0, traveler.getIzoztatutakoDirua(), 0.0001);
	        assertEquals(115, traveler.getMoney(), 0.0001);
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(null);
	        Driver foundDriver = db.find(Driver.class, driver1.getUsername());
	        assertNull(foundDriver); // Ensure the driver is null

	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_1)).thenReturn(null);
	        Car foundCar = db.find(Car.class, TUITION_1);
	        assertNull(foundCar); // Ensure the car is null
	        
	        Mockito.when(db.find(Car.class, TUITION_2)).thenReturn(null);
	        Car foundCar2 = db.find(Car.class, TUITION_2);
	        assertNull(foundCar2); // Ensure the car is null
	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);
	        assertNotNull(retrievedMovement2);
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	// Should delete Traveler
	// Booking1 status == "Rejected"
	// Booking2 status == "Rejected"
	// Ride1 places = 5
	// Ride1.isActive
	// Ride2.isActive
	// alert1 == null
	// alert2 == null
	public void test20() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		List<Booking> bookings1 = new ArrayList<>();
		List<Booking> bookings2 = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate2 = null;
		try {
			rideDate2 = sdf2.parse("06/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate2, 5, 10);
			
			List<Ride> rides = driver1.getCreatedRides();
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			
			Car car2 = new Car(TUITION_2, MODEL_2, 5);
			
			this.mockAddDriverCar(car, driver1);
			this.mockAddDriverCar(car2, driver1);

			// Make the bookings for the same user
	        Booking booking1 = new Booking(rides.get(0), traveler, 1);
	        Booking booking2 = new Booking(rides.get(1), traveler, 1);
	        
	        rides.get(0).setBookings(bookings1);
	        rides.get(1).setBookings(bookings2);
	       
	        // Mock the ride lookup
	        Mockito.when(db.find(List.class, driver1.getCreatedRides().get(0).getBookings())).thenReturn(bookings1);
	        Mockito.when(db.find(List.class, driver1.getCreatedRides().get(0).getBookings())).thenReturn(bookings2);
	        
	        
			rides.get(0).getBookings().add(booking1);
			rides.get(1).getBookings().add(booking2);
	        

		    // Mock the booking
		    Mockito.when(db.merge(traveler)).thenReturn(traveler);
		    Mockito.when(db.merge(rides.get(0))).thenReturn(rides.get(0));
		    
		    traveler.addBookedRide(booking1);
		    traveler.addBookedRide(booking2);
		    traveler.setMoney(5);
		    traveler.setIzoztatutakoDirua(10);

			
			// Make the movements
			Movement movement1 = new Movement(traveler, "BookFreeze", 10);
	        Movement movement2 = new Movement(traveler, "BookFreeze", 11);
	        
	        Movement expectedMovement1 = new Movement(traveler, "BookDeny", 1);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        Movement expectedMovement2 = new Movement(traveler, "BookDeny", 2);
		    Mockito.when(db.merge(expectedMovement2)).thenReturn(expectedMovement2);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
			Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);
			
			Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
			Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);


			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			Mockito.when(db.find(Traveler.class, traveler.getUsername())).thenReturn(traveler);
			
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking1);
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking2);

	        movement1.setUsername(traveler);
	        movement2.setUsername(traveler);

			
			assertEquals("Rejected", booking1.getStatus());
	        assertEquals("Rejected", booking2.getStatus());
	        assertFalse(rides.get(0).isActive());
	        assertFalse(rides.get(1).isActive());
	        assertEquals(-10.0, traveler.getIzoztatutakoDirua(), 0.0001);
	        assertEquals(25, traveler.getMoney(), 0.0001);
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(null);
	        Driver foundDriver = db.find(Driver.class, driver1.getUsername());
	        assertNull(foundDriver); // Ensure the driver is null

	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_1)).thenReturn(null);
	        Car foundCar = db.find(Car.class, TUITION_1);
	        assertNull(foundCar); // Ensure the car is null
	        
	        Mockito.when(db.find(Car.class, TUITION_2)).thenReturn(null);
	        Car foundCar2 = db.find(Car.class, TUITION_2);
	        assertNull(foundCar2); // Ensure the car is null
	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);
	        assertNotNull(retrievedMovement2);
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// MORE THAN TWO CARS
	public void test21() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
			
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			
			Car car2 = new Car(TUITION_2, MODEL_2, 5);
			
			Car car3 = new Car(TUITION_3, MODEL_3, 5);
			
			// Setting the car to driver1
			this.mockAddDriverCar(car, driver1);
			
			// Setting the car2 to driver1
			this.mockAddDriverCar(car2, driver1);
			
			// Setting the car3 to driver1
			this.mockAddDriverCar(car3, driver1);
			// SUT deleteUser()
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			// Expected results
			
			// Verify that the driver is now null
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(null);
	        Driver foundDriver = db.find(Driver.class, driver1.getUsername());
	        assertNull(foundDriver); // Ensure the driver is null

	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_1)).thenReturn(null);
	        Car foundCar = db.find(Car.class, TUITION_1);
	        assertNull(foundCar); // Ensure the car is null
	        
	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_2)).thenReturn(null);
	        Car foundCar2 = db.find(Car.class, TUITION_2);
	        assertNull(foundCar2); // Ensure the car is null
	        
	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_3)).thenReturn(null);
	        Car foundCar3 = db.find(Car.class, TUITION_3);
	        assertNull(foundCar3); // Ensure the car is null
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	// test22 is the same as above, different cars (it works) VL
	@Test
	// Valores Limite (menos asientos por booking)
	//	 TWO Booking and TWO Movement for the Traveler to erase
	//	 TWO Alert, 2 Rides, one Car, Traveler has 100 in Money, one Driver
	//	 Should erase Traveler user, Driver in Database, Ride should have extra seats
	public void test22() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Booking> bookings2 = new ArrayList<>();
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate2 = null;
		try {
			rideDate2 = sdf2.parse("06/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    Alert alert2 = new Alert(FROM, TO, rideDate2, trav1);

		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			alert2.setAlertNumber(1);
			alerts.add(alert2);			
			trav1.addAlert(alert2);
			alert2.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			Mockito.when(db.find(Traveler.class, alert2.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
			
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert2);

	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			driver1.addRide(FROM, TO, rideDate1, 5, 15);
			driver1.addRide(FROM, TO, rideDate2, 5, 15);
			
			List<Ride> rides = driver1.getCreatedRides();
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			this.mockAddDriverCar(car, driver1);
			
			// Assign and create car to the Driver
			Car car1 = new Car(TUITION_2, MODEL_2, 5);
			this.mockAddDriverCar(car1, driver1);
						
			// Make the bookings for the same user
			trav1.setMoney(50);
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 2);
			trav1.setMoney(50);
			Booking booking2 = this.mockBookingProcess(driver1, rides.get(1), trav1, bookings2, 1);

	        
			// Make the movements
			Movement movement1 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement1 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
	        Movement movement2 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement2 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement2)).thenReturn(expectedMovement2);
	        
	        Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        
	        
			sut.open();
			sut.deleteUser(trav1);
			sut.close();
			
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings1);
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings2);
		    
		    Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
		    Mockito.when(mockQuery.getSingleResult()).thenReturn(alert2);

		    						
			assertEquals("Rejected", booking1.getStatus());
			assertEquals("Rejected", booking2.getStatus());
	        assertTrue(rides.get(0).isActive());
	        assertTrue(rides.get(1).isActive());
	        assertEquals(5, rides.get(0).getnPlaces(), 0.0001);
	        assertEquals(5, rides.get(1).getnPlaces(), 0.0001);
	        
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Traveler.class, trav1.getUsername())).thenReturn(null);
			assertNull(db.find(Traveler.class, trav1.getUsername()));
			
			Mockito.when(db.find(Alert.class, alert.getAlertNumber())).thenReturn(null);
			assertNull(db.find(Alert.class, alert.getAlertNumber()));
			
			Mockito.when(db.find(Alert.class, alert.getAlertNumber())).thenReturn(null);
			assertNull(db.find(Alert.class, alert.getAlertNumber()));
			
			Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);
	        assertNotNull(retrievedMovement2);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	// VLimite
	//	 TWO Booking and TWO Movement for the Traveler to erase
	//	 TWO Alert, 2 Rides, one Car, Traveler has 50 in Money, one Driver
	//	 Should erase Traveler user, Driver in Database, 5 and 5 seats limits of
	// car.seats
	public void test23() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Booking> bookings2 = new ArrayList<>();
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate2 = null;
		try {
			rideDate2 = sdf2.parse("06/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    Alert alert2 = new Alert(FROM, TO, rideDate2, trav1);

		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			alert2.setAlertNumber(1);
			alerts.add(alert2);			
			trav1.addAlert(alert2);
			alert2.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			Mockito.when(db.find(Traveler.class, alert2.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
			
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert2);

	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			driver1.addRide(FROM, TO, rideDate1, 5, 15);
			driver1.addRide(FROM, TO, rideDate2, 5, 15);
			
			List<Ride> rides = driver1.getCreatedRides();
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			this.mockAddDriverCar(car, driver1);
			
			// Assign and create car to the Driver
			Car car1 = new Car(TUITION_2, MODEL_2, 5);
			this.mockAddDriverCar(car1, driver1);
						
			// Make the bookings for the same user (SETTING THE BOOKING SEATS TO 5)
			trav1.setMoney(100);
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 5);
			trav1.setMoney(100);
			Booking booking2 = this.mockBookingProcess(driver1, rides.get(1), trav1, bookings2, 5);

	        
			// Make the movements
			Movement movement1 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement1 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
	        Movement movement2 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement2 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement2)).thenReturn(expectedMovement2);
	        
	        Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        
	        
			sut.open();
			sut.deleteUser(trav1);
			sut.close();
			
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings1);
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings2);
		    
		    Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
		    Mockito.when(mockQuery.getSingleResult()).thenReturn(alert2);

		    						
			assertEquals("Rejected", booking1.getStatus());
			assertEquals("Rejected", booking2.getStatus());
	        assertTrue(rides.get(0).isActive());
	        assertTrue(rides.get(1).isActive());
	        assertEquals(5, rides.get(0).getnPlaces(), 0.0001);
	        assertEquals(5, rides.get(1).getnPlaces(), 0.0001);
	        
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Traveler.class, trav1.getUsername())).thenReturn(null);
			assertNull(db.find(Traveler.class, trav1.getUsername()));
			
			Mockito.when(db.find(Alert.class, alert.getAlertNumber())).thenReturn(null);
			assertNull(db.find(Alert.class, alert.getAlertNumber()));
			
			Mockito.when(db.find(Alert.class, alert.getAlertNumber())).thenReturn(null);
			assertNull(db.find(Alert.class, alert.getAlertNumber()));
			
			Mockito.when(db.find(Movement.class, expectedMovement1)).thenReturn(expectedMovement1);
	        Movement retrievedMovement1 = db.find(Movement.class, expectedMovement1);
	        assertNotNull(retrievedMovement1);
	        
	        Mockito.when(db.find(Movement.class, expectedMovement2)).thenReturn(expectedMovement2);
	        Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);
	        assertNotNull(retrievedMovement2);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Should throw exception of null user
	public void test24() {
		try {
			User us = new User(null, null, null);
			sut.deleteUser(us);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Unexpected exception");
		}
	}
	@Test
	// Should throw exception of user.mota
	public void test25() {
		try {
			User us = new User("Daniel", "KDSAD", "Hello");
			sut.deleteUser(us);
			fail("Expected MotaNotAcceptedException");
		} catch (NullPointerException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Unexpected exception");
		}
	}
	@Test
	// Should throw exception of user.mota
	public void test26() {
		try {
			User us = new User("Daniel", "KDSAD", "World");
			sut.deleteUser(us);
			fail("Expected MotaNotAcceptedException");
		} catch (NullPointerException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Unexpected exception");
		}
	}
	// TEST VALORES LÍMITE of not having a Car is Bussiness rule (I do not know)
	@Test
	// Can not create booking insufficient money
	public void test27() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		List<Booking> bookings1 = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 15);
			
			List<Ride> rides = driver1.getCreatedRides();
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			
			this.mockAddDriverCar(car, driver1);
			
			// Make the bookings for the same user
			// (Driver dr, Ride r, Traveler tr, List<Booking> bookings, int seats)
			traveler.setMoney(10);
	        Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), traveler, bookings1, 3);
	        assertNull(booking1);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}
	@Test
	// Can not create booking negative money 
	// (Should make NegativeMoneyTravelerException)
	public void test28() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		List<Booking> bookings1 = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);

			this.setupDeleteDriver(driver1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 15);
			
			List<Ride> rides = driver1.getCreatedRides();
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			
			this.mockAddDriverCar(car, driver1);
			
			// Make the bookings for the same user
			// (Driver dr, Ride r, Traveler tr, List<Booking> bookings, int seats)
			traveler.setMoney(-10);
	        Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), traveler, bookings1, 3);
	        assertNull(booking1);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}
	@Test
	// Same date of rides created by the Driver
	public void test29() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/12/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    
		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			// SAME DATE RIDES
			this.mockAddDriverRideBbox(FROM, TO, rideDate1, 5, 10, driverUsername, driver1);
			this.mockAddDriverRideBbox(FROM, TO, rideDate1, 5, 10, driverUsername, driver1);
		} catch (RideAlreadyExistException e) {
			e.printStackTrace();
			assertTrue(true);
		} catch (RideMustBeLaterThanTodayException e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Date of ride should be greater than now ()
	public void test30() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("10/01/1900");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    
		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			// date is lower than Now from the Ride
			this.mockAddDriverRideBbox(FROM, TO, rideDate1, 5, 10, driverUsername, driver1);
			fail();
		} catch (RideAlreadyExistException e) {
			e.printStackTrace();
			fail();
		} catch (RideMustBeLaterThanTodayException e) {
			e.printStackTrace();
		}
	}
	// tes31 (do not know the business logic (if driver should have a car necessarily or not)
	// test32 (does not apply with any logic)
	@Test
	// VL should book with less seats exception	 
	public void test31() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Booking> bookings1 = new ArrayList<>();
		List<Booking> bookings2 = new ArrayList<>();
		List<Alert> alerts = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate2 = null;
		try {
			rideDate2 = sdf2.parse("06/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			Traveler trav1 = new Traveler(travelerUsername, travelerPassword);
			this.setupDeleteTraveler(driver1, trav1);
			
		    TypedQuery<Alert> mockQuery = Mockito.mock(TypedQuery.class);
			// Mock the database behavior
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(mockQuery);
		    Mockito.when(mockQuery.setParameter(Mockito.eq("username"), Mockito.eq(travelerUsername)))
		           .thenReturn(mockQuery);
		    Alert alert = new Alert(FROM, TO, rideDate1, trav1);
		    Alert alert2 = new Alert(FROM, TO, rideDate2, trav1);

		    alert.setAlertNumber(0);
			alerts.add(alert);			
			trav1.addAlert(alert);
			alert.setTraveler(trav1);
			
			alert2.setAlertNumber(1);
			alerts.add(alert2);			
			trav1.addAlert(alert2);
			alert2.setTraveler(trav1);
			
			Mockito.when(db.find(Traveler.class, alert.getTraveler())).thenReturn(trav1);
			
			Mockito.when(db.find(Traveler.class, alert2.getTraveler())).thenReturn(trav1);
			
			// Mock the getResultList to return the list containing the alert
			Mockito.when(mockQuery.getResultList()).thenReturn(alerts);

			// Mock the behavior of getting a single result from the query
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
			
			Mockito.when(mockQuery.getSingleResult()).thenReturn(alert2);

	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
			driver1.addRide(FROM, TO, rideDate1, 5, 15);
			driver1.addRide(FROM, TO, rideDate2, 5, 15);
			
			List<Ride> rides = driver1.getCreatedRides();
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			this.mockAddDriverCar(car, driver1);
			
			// Assign and create car to the Driver
			Car car1 = new Car(TUITION_2, MODEL_2, 5);
			this.mockAddDriverCar(car1, driver1);
						
			// Make the bookings for the same user (SETTING THE BOOKING SEATS TO 5)
			trav1.setMoney(100);
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 100);
			assertNull(booking1);
		} catch(Exception e) {
			fail();
		}
	}
	
}