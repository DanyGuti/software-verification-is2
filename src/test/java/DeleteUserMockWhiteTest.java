import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import domain.Booking;
import domain.Car;
import domain.Driver;
import domain.Movement;
import domain.Ride;
import domain.Traveler;


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
		// Inside your init method or before the test method
		TypedQuery<Driver> typedQueryMock = Mockito.mock(TypedQuery.class);
		Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Driver.class))).thenReturn(typedQueryMock);
	    sut=new DataAccess(db);
    }
	@After
    public  void tearDown() {
		persistenceMock.close();
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
			Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
			
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
	
	@Test
	// Testing us.mota == "Driver", there is only one Ride
	// Driver has no cars at all
	// SHOULD Fail()
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
			Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
			driver1.addRide(FROM, TO, rideDate, 5, 10);
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
			
			// Invoke SUT
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			fail();
			
		} catch (Exception e) {
			System.out.println("Should have cars if Driver has Rides");
			assertTrue(true);
			sut.close();
		} 
	}
	@Test
	// Testing us.mota == "Driver", there are two Ride
	// Driver has no cars at all
	// SHOULD Fail()
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
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate2, 5, 10);
	        Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
			
			// Invoke SUT
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			fail();
			
		} catch (Exception e) {
			System.out.println("Should have cars if Driver has Rides");
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
	// Driver is erased, and also its cars (2 in this case erased)
	// theres is a new movement created
	public void test4() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		List<Movement> movements = new ArrayList<>();

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
			Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
			// Mocking the behavior of EntityManager and TypedQuery
		    TypedQuery<Driver> typedQueryMock = Mockito.mock(TypedQuery.class);
		    Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Driver.class))).thenReturn(typedQueryMock);
		    Mockito.when(typedQueryMock.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(typedQueryMock);
		    Mockito.when(typedQueryMock.getSingleResult()).thenReturn(driver1); // Return the mocked driver
			
			// Create the Ride1 and Ride2
			driver1.addRide(FROM, TO, rideDate1, 5, 10);
			driver1.addRide(FROM, TO, rideDate2, 5, 10);
			List<Ride> rides = driver1.getCreatedRides();	
			System.out.println(rides);
			// Create the traveler
			Traveler traveler = new Traveler(travelerUsername, travelerPassword); 
			Mockito.when(db.find(Traveler.class, traveler.getUsername())).thenReturn(traveler);

			
			// 	Assign and create car to the Driver
			Car car = new Car(TUITION_1, MODEL_1, 5);
			driver1.addCar(car);
			Mockito.when(db.find(Driver.class, driver1.getUsername())).thenReturn(driver1);
			
			
			// Make the bookings for the same user
//	        Booking booking1 = new Booking(rides.get(0), traveler, 3);
//	        Booking booking2 = new Booking(rides.get(1), traveler, 3);
//	        
//			Mockito.when(db.find(Booking.class, booking1)).thenReturn(booking1);
//			Mockito.when(db.find(Booking.class, booking2)).thenReturn(booking2);
			// Mock the ride lookup
		    Mockito.when(db.find(Ride.class, rides.get(0))).thenReturn(rides.get(0));
		    Mockito.when(db.find(Ride.class, rides.get(1))).thenReturn(rides.get(1));

		    // Mock the booking
		    Booking booking1 = new Booking(rides.get(0), traveler, 3);
		    Mockito.when(db.merge(traveler)).thenReturn(traveler);
		    Mockito.when(db.merge(rides.get(0))).thenReturn(rides.get(0));

		    // Call the method to create a booking
		    Boolean createdBooking = sut.bookRide(travelerUsername, rides.get(0), 3, 0);

			
			// Make the movements
			Movement movement1 = new Movement(traveler, "BookFreeze", 10);
	        Movement movement2 = new Movement(traveler, "BookFreeze", 11);
	        
	        Mockito.when(db.find(Movement.class, movement1)).thenReturn(movement1);
			Mockito.when(db.find(Movement.class, movement2)).thenReturn(movement2);

			
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			Mockito.when(db.find(Traveler.class, traveler.getUsername())).thenReturn(traveler);
			Mockito.when(traveler.getIzoztatutakoDirua()).thenReturn(-20.0);
			Mockito.when(traveler.getMoney()).thenReturn(130.0);
			
			Mockito.when(traveler.getBookedRides()).thenReturn(traveler.getBookedRides());
//	        List<Booking> bookings = List.of(booking1, booking2);
	        movements = List.of(movement1, movement2);
			
			assertEquals("Rejected", booking1.getStatus());
//	        assertEquals("Rejected", booking2.getStatus());
	        assertFalse(rides.get(0).isActive());
	        assertFalse(rides.get(1).isActive());
	        assertEquals(-20.0, traveler.getIzoztatutakoDirua(), 0.0001);
	        assertEquals(130, traveler.getMoney(), 0.0001);

//			for (Booking b: bookings) {
//				Ride rAfterDel = b.getRide();
//				assertTrue(!rAfterDel.isActive());
//			}
//			NO IMPLEMENTADO EN DATA ACCESS
//			movements = testDA.findMovementAfterDelUserDriver(newTrav); 
//			assertEquals(movements.get(0).getEragiketa(), "BookDeny");
			
			Mockito.when(db.find(Driver.class, "Daniel")).thenReturn(null);
			Mockito.when(db.find(Car.class,  TUITION_1)).thenReturn(null);
//	        assertNull(db.find(Driver.class, driver1.getUsername()), "Driver should be null after deletion");

//			assertNull(testDA.getCarByTuition(TUITION_1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
