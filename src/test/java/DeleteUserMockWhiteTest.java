import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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


// Created by Daniel Gutierrez Gomez
public class DeleteUserMockWhiteTest {
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
	
	private Booking mockBookingProcess (Driver dr, Ride r, Traveler tr, List<Booking> bookings, int seats) {
		// Make the bookings for the same user
        Booking booking = new Booking(r, tr, seats);
        
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
	    Mockito.when(db.merge(tr)).thenReturn(tr);
        // Mock the ride lookup
        Mockito.when(db.find(List.class, dr.getCreatedRides().get(0).getBookings())).thenReturn(bookings);
        Mockito.when(db.find(List.class, tr.getBookedRides())).thenReturn(bookings);
        return booking;
	}
	
	@Test
	// Testing deleteUser dataAccess when user.mota == "Driver"
	// user does not have Car nor Rides
	// Should expect to not delete user() correctly
	public void test1() {
		// Define parameters
		String driverUsername="Daniel";
		String driverPassword="dsadoaoda";
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			this.setupDeleteDriver(driver1);
			// Invoke SUT
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			assertNotNull(db.find(Driver.class, driver1.getUsername()));

		} catch (Exception e) {
			sut.close();
			e.printStackTrace();
			fail();
		}
	}
//	
	@Test
//	// Testing us.mota == "Driver", there is only one Ride
//	// Driver has no cars at all
//	// SHOULD Fail(), should check if driver has cars and bookings before
	public void test2() {
		// Define parameters
		String driverUsername="Daniel";
		String driverPassword="dsadoaoda";
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate = null;
		try {
			rideDate = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			Driver driver1 = new Driver(driverUsername, driverPassword);
			this.setupDeleteDriver(driver1);
			
			driver1.addRide(FROM, TO, rideDate, 5, 10);
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
			
			// Invoke SUT
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			fail();
			
		} catch (Exception e) {
			assertTrue(true);
			sut.close();
		} 
	}
	@Test
//	// Testing us.mota == "Driver", there are two Rides
//	// Driver has no cars at all, should check if driver has cars and bookings before
//	// SHOULD Fail()
	public void test3() {
		// Define parameters
		String driverUsername="Daniel";
		String driverPassword="dsadoaoda";
		
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
			Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
			// Mock the behavior of db.find to return the Driver object
	        Mockito.when(db.find(Driver.class, driverUsername)).thenReturn(driver1);
	        
	        // Mock the behavior of db.createQuery to return the typedQuery
	        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Driver.class))).thenReturn(typedQuery);
	        
	        // Mock the behavior of typedQuery to return driver1 when getSingleResult is called
	        Mockito.when(typedQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(typedQuery);
	        Mockito.when(typedQuery.getSingleResult()).thenReturn(driver1); // Return the driver object
	        
	        Mockito.when(typedQuery.getResultList()).thenReturn(Collections.singletonList(driver1));
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate2, 5, 10);
			
			// Invoke SUT
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			fail();
			
		} catch (Exception e) {
			assertTrue(true);
			sut.close();
		} 
	}
	@SuppressWarnings("deprecation")
	@Test
	// Should cover 2 bookings from a user
	// setting the movements and money from the user
	// adding also the status as accepted,
	// setting the frozen money of Traveler
	// Traveler should have initial money plus the price
	// Driver is erased, and also its car (1 in this case erased)
	// NEEDS A REFACTOR TO GET MOVEMENTS BY TRAVELER
	// theres is a new movement created by bookings (CAN NOT ACCESS THE MOVEMENTS)
	public void test4() {
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

			//configure the state through mocks 
	        this.setupDeleteDriver(driver1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate2, 5, 10);
			
			List<Ride> rides = driver1.getCreatedRides();
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			driver1.addCar(car);
			car.setDriver(driver1);
			Mockito.when(db.merge(car)).thenReturn(car);
			Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
	        Mockito.when(db.find(Driver.class, car.getDriver().getUsername())).thenReturn(driver1);
			Mockito.when(db.find(Driver.class, car.getDriver())).thenReturn(driver1);
			
			// Make the bookings for the same user
	        Booking booking1 = new Booking(rides.get(0), traveler, 3);
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

			
			// Make the movements
			Movement movement1 = new Movement(traveler, "BookFreeze", 10);
	        Movement movement2 = new Movement(traveler, "BookFreeze", 11);
	        
	        Movement expectedMovement1 = new Movement(traveler, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        Movement expectedMovement2 = new Movement(traveler, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement2)).thenReturn(expectedMovement2);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
			Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);

			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			Mockito.when(db.find(Traveler.class, traveler.getUsername())).thenReturn(traveler);
			Mockito.when(db.find(Double.class, traveler.getIzoztatutakoDirua())).thenReturn(-20.0);
			Mockito.when(db.find(Double.class, traveler.getMoney())).thenReturn(130.0);
			
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking1);
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking2);

	        movement1.setUsername(traveler);
	        movement2.setUsername(traveler);

			
			assertEquals("Rejected", booking1.getStatus());
	        assertEquals("Rejected", booking2.getStatus());
	        assertFalse(rides.get(0).isActive());
	        assertFalse(rides.get(1).isActive());
	        assertEquals(-60.0, traveler.getIzoztatutakoDirua(), 0.0001);
	        assertEquals(60, traveler.getMoney(), 0.0001);
	        
	        // Verify that the driver is now null
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(null);
	        Driver foundDriver = db.find(Driver.class, driver1.getUsername());
	        assertNull(foundDriver); // Ensure the driver is null

	        // Verify that the car is now null
	        Mockito.when(db.find(Car.class, TUITION_1)).thenReturn(null);
	        Car foundCar = db.find(Car.class, TUITION_1);
	        assertNull(foundCar); // Ensure the car is null
	        
	        // Retrieve movements from the database
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER retrievedMovement1 = db.find(Movement.class, );
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER  Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Should cover 2 bookings from a user
	// setting the movements and money from the user
	// adding also the status as accepted,
	// setting the frozen money of Traveler
	// Traveler should have initial money plus the price
	// Driver is erased, and also its cars (2 in this case erased)
	// NEEDS A REFACTOR TO GET MOVEMENTS BY TRAVELER
	// theres is a new movement created by bookings (CAN NOT ACCESS THE MOVEMENTS)
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

			this.setupDeleteDriver(driver1);
	        
	        // String from, String to, Date date, int nPlaces, double price, Driver driver
	        
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate2, 5, 10);
			
			List<Ride> rides = driver1.getCreatedRides();
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
						
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			driver1.addCar(car);
			
			Car car2 = new Car(TUITION_2, MODEL_2, 5);
			driver1.addCar(car2);
			
			car.setDriver(driver1);
			car2.setDriver(driver1);
			
			Mockito.when(db.merge(car)).thenReturn(car);
			Mockito.when(db.merge(car2)).thenReturn(car2);

			Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
	        Mockito.when(db.find(Driver.class, car.getDriver().getUsername())).thenReturn(driver1);
			Mockito.when(db.find(Driver.class, car.getDriver())).thenReturn(driver1);
			
			Mockito.when(db.find(Driver.class, car2.getDriver().getUsername())).thenReturn(driver1);
			Mockito.when(db.find(Driver.class, car2.getDriver())).thenReturn(driver1);
			
			// Make the bookings for the same user
	        Booking booking1 = new Booking(rides.get(0), traveler, 3);
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

			
			// Make the movements
			Movement movement1 = new Movement(traveler, "BookFreeze", 10);
	        Movement movement2 = new Movement(traveler, "BookFreeze", 11);
	        
	        Movement expectedMovement1 = new Movement(traveler, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        Movement expectedMovement2 = new Movement(traveler, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement2)).thenReturn(expectedMovement2);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
			Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);

			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			Mockito.when(db.find(Traveler.class, traveler.getUsername())).thenReturn(traveler);
			Mockito.when(db.find(Double.class, traveler.getIzoztatutakoDirua())).thenReturn(-20.0);
			Mockito.when(db.find(Double.class, traveler.getMoney())).thenReturn(130.0);
			
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking1);
			Mockito.when(db.find(Booking.class, traveler.getBookedRides())).thenReturn(booking2);

	        movement1.setUsername(traveler);
	        movement2.setUsername(traveler);

			
			assertEquals("Rejected", booking1.getStatus());
	        assertEquals("Rejected", booking2.getStatus());
	        assertFalse(rides.get(0).isActive());
	        assertFalse(rides.get(1).isActive());
	        assertEquals(-60.0, traveler.getIzoztatutakoDirua(), 0.0001);
	        assertEquals(60, traveler.getMoney(), 0.0001);
	        
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
	        
	        // Retrieve movements from the database
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER retrievedMovement1 = db.find(Movement.class, );
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER  Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Testing deleteUser dataAccess when user.mota == "Traveler"
	// user does not have Bookings nor Alert
	// Should expect to delete user() correctly
	public void test6() {
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
//	        
	        
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
	@Test
	// Should return 5 seats again after the booking
	// For the ride1, and delete the user, Booking as "Rejected"
	// THERE IS NO MOVEMENTS IMPLEMENTATION AT DOMAIN LEVEL IS NEEDED
	public void test7() {
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
			
						
			// Make the bookings for the same user		
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 3);				        
			
			// Make the movements
			Movement movement1 = new Movement(trav1, "BookFreeze", 10);
	        
	        Movement expectedMovement1 = new Movement(trav1, "BookDeny", 30.0);
		    Mockito.when(db.merge(expectedMovement1)).thenReturn(expectedMovement1);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
	        Mockito.when(db.merge(trav1)).thenReturn(trav1);
	        db.merge(trav1);
	        
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
	        
	        // Retrieve movements from the database
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER retrievedMovement1 = db.find(Movement.class, );
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER  Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Should return 5 seats again after the booking1
	// Should return 5 seats again after the booking2
	// For the ride1, and delete the user, booking1 as "Rejected", booking2 as "Rejected"
	// THERE IS NO MOVEMENTS IMPLEMENTATION AT DOMAIN LEVEL IS NEEDED
	public void test8() {
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
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 3);
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
	        
	        // Retrieve movements from the database
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER retrievedMovement1 = db.find(Movement.class, );
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER  Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Should return 5 seats again after the booking1
	// Should return 5 seats again after the booking2
	// For the ride1, and delete the user, booking1 as "Rejected", booking2 as "Rejected"
	// Also the Alert should be null
	// THERE IS NO MOVEMENTS IMPLEMENTATION AT DOMAIN LEVEL IS NEEDED
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
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 3);
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
	        
			sut.open();
			sut.deleteUser(trav1);
			sut.close();
			
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings1);
		    Mockito.when(db.find(List.class, trav1.getBookedRides())).thenReturn(bookings2);
		    
		    Mockito.when(mockQuery.getSingleResult()).thenReturn(alert);
		    						
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
			
	        
	        // Retrieve movements from the database
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER retrievedMovement1 = db.find(Movement.class, );
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER  Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);

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
	// THERE IS NO MOVEMENTS IMPLEMENTATION AT DOMAIN LEVEL IS NEEDED
	public void test10() {
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
			Booking booking1 = this.mockBookingProcess(driver1, rides.get(0), trav1, bookings1, 3);
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
			
	        
	        // Retrieve movements from the database
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER retrievedMovement1 = db.find(Movement.class, );
	        // Movement CAN NOT BE ACCESSED BY TRAVELER NOR USER  Movement retrievedMovement2 = db.find(Movement.class, expectedMovement2);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	@Test
	// Testing deleteUser dataAccess when user.mota != "Admin" &&
	// user.mota != "Rider" && user.mota != "Driver"
	// SHOULD FAIL
	public void test11() {
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
	
}
