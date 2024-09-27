import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import dataAccess.DataAccess;
import domain.Booking;
import domain.Driver;
import domain.Movement;
import domain.Ride;
import domain.Traveler;
import domain.User;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;
import testOperations.TestDataAccess;

// Created by Daniel Gutierrez Gomez
public class DeleteUserBDBlackTest {

	static DataAccess sut = new DataAccess();
	 
	// Additional operations needed to execute the test 
	static TestDataAccess testDA = new TestDataAccess();
	 
	// Constants
	private static final String FROM = "SAN SEBASTIAN";
	private static final String TO = "MADRID";
	
	private static final String MODEL_1 = "Audi";
	private static final String TUITION_1 = "ADF-189";
	private static final String MODEL_2 = "BMW";
	private static final String TUITION_2 = "ADF-190";
	private static final String MODEL_3 = "BMW-3";
	private static final String TUITION_3 = "ADF-191";
	
	@Test
	// Should delete User, should delete Car1
	// Should delete Car2, should add third movement
	// Booking1.status == Rejected
	// Booking2.status == Rejected
	// Ride1.inactive
	// Ride2.inactive
	// User2.money = Money spent returned (200 expected)
	public void test1() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
		    System.out.println(testDA.existDriver(driverUsername));
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 15);
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 15);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
			
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 5, 50, 10);
			testDA.createBooking(driver1, rides.get(1), traveler, 5, 50, 10);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 11);
			testDA.close();
			
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			testDA.open();
			Traveler newTrav1 = testDA.getTraveler(travelerUsername);
			List<Booking> bks1 = testDA.getBookingsByTraveler(traveler);
			testDA.close();
			
			assertEquals("Rejected", bks1.get(0).getStatus().toString());
			assertEquals("Rejected", bks1.get(1).getStatus().toString());

			for (Booking b: bks1) {
				Ride rAfterDel = b.getRide();
				assertTrue(!rAfterDel.isActive());
			}
			assertEquals(-140.0, newTrav1.getIzoztatutakoDirua(), 0.0001);
			assertEquals(200, newTrav1.getMoney(), 0.0001);
			
			testDA.open();
			movements = testDA.findMovementAfterDelUserDriver(newTrav1); 
			assertEquals(movements.get(0).getEragiketa(), "BookDeny");
			Driver newDriver = testDA.getDriver(driverUsername);
			assertNull(newDriver);
			
			
			assertNull(testDA.getCarByTuition(TUITION_1));
			assertNull(testDA.getCarByTuition(TUITION_2));

			testDA.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				testDA.eraseBookingsAfterDel();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.removeTraveler(travelerUsername);
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}	
	}
	@Test
	// Should delete User
	// should delete car (without Rides)
	public void test2() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		Driver driver1 = null;
		
		try {
			testDA.open();
		    System.out.println(testDA.existDriver(driverUsername));
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.close();
			
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			testDA.open();
			Driver newDriver = testDA.getDriver(driverUsername);
			assertNull(newDriver);
			assertNull(testDA.getCarByTuition(TUITION_1));
			
		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
		
	}
	@Test
	// Should delete User
	// Should delete car1
	// Should add New movement
	// Booking1 status == "Rejected"
	// Ride1.active() == false
	// user2.money == 125
	public void test3() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			testDA.open();
		    System.out.println(testDA.existDriver(driverUsername));
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 15);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.close();
			
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 5, 50, 10);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 11);
			testDA.close();
			
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			testDA.open();
			Traveler newTrav = testDA.getTraveler(travelerUsername);
			List<Booking> bks1 = testDA.getBookingsByTraveler(traveler);
			testDA.close();
			
			assertEquals("Rejected", bks1.get(0).getStatus().toString());

			for (Booking b: bks1) {
				Ride rAfterDel = b.getRide();
				assertTrue(!rAfterDel.isActive());
			}
			assertEquals(-65.0, newTrav.getIzoztatutakoDirua(), 0.0001);
			assertEquals(125, newTrav.getMoney(), 0.0001);
			
			testDA.open();
			movements = testDA.findMovementAfterDelUserDriver(newTrav); 
			assertEquals(movements.get(0).getEragiketa(), "BookDeny");
			Driver newDriver = testDA.getDriver(driverUsername);
			assertNull(newDriver);
			
			
			assertNull(testDA.getCarByTuition(TUITION_1));

			testDA.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				testDA.eraseBookingsAfterDel();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.removeTraveler(travelerUsername);
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}	
	}
	@Test
	// Should delete User
	// Should delete car1
	// Should delete car2
	public void test4() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		Driver driver1 = null;
		
		try {
			testDA.open();
		    System.out.println(testDA.existDriver(driverUsername));
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
			
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			testDA.open();
			Driver newDriver = testDA.getDriver(driverUsername);
			assertNull(newDriver);
			assertNull(testDA.getCarByTuition(TUITION_1));
			assertNull(testDA.getCarByTuition(TUITION_2));
			
		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	
	@Test
	// Should delete Traveler
	// Booking1 status == "Rejected"
	// Booking2 status == "Rejected"
	// Ride1 places = 5
	// Ride2 places = 5
	public void test5() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 5);
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 5);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
			
			System.out.println(rides);
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 3, 50, 15);
			testDA.createBooking(driver1, rides.get(1), traveler, 5, 50, 15);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 10);

			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.getBookingsByTraveler(newTrav);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));
			Booking afterDelBooking2 = testDA.getBookingByRide(driver1, afterDel.get(1));

			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			assertEquals(5, afterDel.get(1).getnPlaces(), 0.00001);
			
			assertTrue(afterDel.get(0).isActive());
			assertTrue(afterDel.get(1).isActive());

			assertNull(testDA.getTraveler(travelerUsername));
			assertEquals("Rejected", afterDelBooking.getStatus().toString());
			assertEquals("Rejected", afterDelBooking2.getStatus().toString());
			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// Should delete Traveler
	// Booking1 status == "Rejected"
	// Ride1 places = 5
	// Ride is still active
	public void test6() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 5);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
						
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 3, 50, 15);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);

			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.getBookingsByTraveler(newTrav);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));

			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			
			assertTrue(afterDel.get(0).isActive());

			assertNull(testDA.getTraveler(travelerUsername));
			assertEquals("Rejected", afterDelBooking.getStatus().toString());
			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// Should delete Traveler
	public void test7() {
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Traveler traveler = null;
		try {
			testDA.open();
			
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			sut.open();
			sut.deleteUser(traveler);
			sut.close();
			testDA.open();
			assertNull(testDA.getTraveler(travelerUsername));
			testDA.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// Should delete Traveler
	// Booking1 status == "Rejected"
	// Ride1 places = 5
	// Ride.isActive
	// alert1 == null
	public void test8() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 5);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
						
			// Make the bookings for the same user
			testDA.open();
			// Create the Alert
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate1);
			testDA.createBooking(driver1, rides.get(0), traveler, 3, 50, 15);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);

			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.getBookingsByTraveler(newTrav);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));

			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			
			assertTrue(afterDel.get(0).isActive());

			assertNull(testDA.getTraveler(travelerUsername));
			assertEquals(0, testDA.getAlertsByUser(newTrav).size(), 0.000001);
			assertEquals("Rejected", afterDelBooking.getStatus().toString());
			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// Should delete Traveler
	// Booking1 status == "Rejected"
	// Ride1 places = 5
	// Ride.isActive
	// alert1 == null
	// alert2 == null
	public void test9() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 5);
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 5);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
						
			// Make the bookings for the same user
			testDA.open();
			// Create the Alert
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate1);
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate2);

			testDA.createBooking(driver1, rides.get(0), traveler, 3, 50, 15);
			testDA.createBooking(driver1, rides.get(1), traveler, 5, 50, 15);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 10);


			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.getBookingsByTraveler(newTrav);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));
			Booking afterDelBooking2 = testDA.getBookingByRide(driver1, afterDel.get(1));


			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			assertEquals(5, afterDel.get(1).getnPlaces(), 0.00001);

			
			assertTrue(afterDel.get(0).isActive());
			assertTrue(afterDel.get(1).isActive());

			assertEquals(0, testDA.getAlertsByUser(newTrav).size(), 0.000001);
			assertEquals("Rejected", afterDelBooking.getStatus().toString());
			assertEquals("Rejected", afterDelBooking2.getStatus().toString());
			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// Should delete Admin
	public void test10() {
		// Define parameters
		String adminUsername = "Daniel";
		String adminPasswd = "dsadjasld";
		User us1 = null;
		try {
			testDA.open();
			us1 = testDA.createAdmin(adminUsername, adminPasswd);
			testDA.close();
			sut.open();
			sut.deleteUser(us1);
			sut.close();
			
			testDA.open();
			System.out.println(testDA.getUser(adminUsername));
			assertNull(testDA.getUser(adminUsername));
			testDA.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getUser(adminUsername) != null) {
					testDA.deleteUser(adminUsername);
				}
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// Should throw exception of user.mota
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
	// Should throw exception of user.mota
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
	public void test13() {
		// Define parameters
		String driverUsername="Daniel";
		String driverPassword="dsadoaoda";
		Driver driver1 = null;		
		boolean driverCreated = false;
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
				driver1 = testDA.createDriver(driverUsername, driverPassword);
			    driverCreated = true;
			}			
			testDA.close();		
			
			// Invoke SUT
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			testDA.open();
			Driver newDriver = testDA.getDriver(driverUsername);
			assertNull(newDriver);
			testDA.close();
			fail();
		} catch (Exception e) {
			System.out.println("Should have a Car if you are a driver");
			assertTrue(true);
			sut.close();
			testDA.close();
		} finally {
			testDA.open();
			if (driverCreated || testDA.existDriver(driverUsername))
				testDA.removeDriver(driverUsername);
			testDA.close();
		}
	}
	@Test
	public void test14() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 15);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
						
			// Make the bookings for the same user
			testDA.open();
			Booking bookingExpected = testDA.bookRideBlackBox(driver1, rides.get(0), traveler, 3, 10, 15);
			assertNull(bookingExpected);

			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			assertTrue(true);
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	//sut.createRide:  the date of the ride must be later than today. The RideMustBeLaterThanTodayException 
	// exception must be thrown. 
	public void test15 () {
		
		String driverUsername="Driver Test";
		String driverPassword="123";

		String rideFrom="Donostia";
		String rideTo="Zarautz";
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate=null;
		
		boolean driverCreated=false;

		try {
			rideDate = sdf.parse("05/10/2018");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		try {
			
			//define parameters
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
				testDA.createDriver(driverUsername,driverPassword);
			    driverCreated=true;
			}
			testDA.close();		
			
			//invoke System Under Test (sut)  
			sut.open();
		    sut.createRide(rideFrom, rideTo, rideDate, 2, 10, driverUsername);
			sut.close();
			
			fail();
			
		   } catch (RideMustBeLaterThanTodayException  e) {
			 //verify the results
				sut.close();
				assertTrue(true);
			} catch (RideAlreadyExistException e) {
				sut.close();
				fail();
		} finally {
				  //Remove the created objects in the database (cascade removing)   
				testDA.open();
				  if (driverCreated) 
					  testDA.removeDriver(driverUsername);
		          testDA.close();
		        }
		   }
	@Test
	// Same date RIDE
	public void test16() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2 SAME DATE
			if (driver1 != null) {
				testDA.createRideBlackBox(FROM, TO, rideDate1, 2, 5, driverUsername);
				testDA.createRideBlackBox(FROM, TO, rideDate1, 2, 5, driverUsername);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
						
			// Make the bookings for the same user
			testDA.open();
			// Create the Alert
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate1);
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate2);

			testDA.createBooking(driver1, rides.get(0), traveler, 3, 50, 15);
			testDA.createBooking(driver1, rides.get(1), traveler, 5, 50, 15);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 10);


			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.getBookingsByTraveler(newTrav);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));
			Booking afterDelBooking2 = testDA.getBookingByRide(driver1, afterDel.get(1));


			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			assertEquals(5, afterDel.get(1).getnPlaces(), 0.00001);

			
			assertTrue(afterDel.get(0).isActive());
			assertTrue(afterDel.get(1).isActive());

			assertEquals(0, testDA.getAlertsByUser(newTrav).size(), 0.000001);
			assertEquals("Rejected", afterDelBooking.getStatus().toString());
			assertEquals("Rejected", afterDelBooking2.getStatus().toString());
			testDA.close();

		} catch (RideAlreadyExistException e) {
			// if the program goes to this point fail  
			assertTrue(true);
		} catch (RideMustBeLaterThanTodayException e) {
			// if the program goes to this point fail  
			fail();
		}
		finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	
	
	@Test
	// LESS THAN NOW date RIDE
	public void test17() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2000");
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
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2 SAME DATE
			if (driver1 != null) {
				testDA.createRideBlackBox(FROM, TO, rideDate1, 2, 5, driverUsername);
				testDA.createRideBlackBox(FROM, TO, rideDate1, 2, 5, driverUsername);
			} else {
			    System.out.println("Driver null");
			}
		}  catch (RideMustBeLaterThanTodayException  e) {
			//verify the results
			assertTrue(true);
		} catch (RideAlreadyExistException e) {
			sut.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	public void test18() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 15);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
						
			// THE BOOKING HAS MORE SEATS THAN CAR SEATS
			testDA.open();
			Booking bookingExpected = testDA.bookRideBlackBox(driver1, rides.get(0), traveler, 20, 10, 15);
			assertNull(bookingExpected);

			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			assertTrue(true);
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
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
	public void test19() {
	// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
		    System.out.println(testDA.existDriver(driverUsername));
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 50);
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 15);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
			
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 1, 50, 10);
			testDA.createBooking(driver1, rides.get(1), traveler, 1, 50, 10);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 11);
			testDA.close();
			
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			testDA.open();
			Traveler newTrav1 = testDA.getTraveler(travelerUsername);
			List<Booking> bks1 = testDA.getBookingsByTraveler(traveler);
			testDA.close();
			
			assertEquals("Rejected", bks1.get(0).getStatus().toString());
			assertEquals("Rejected", bks1.get(1).getStatus().toString());

			for (Booking b: bks1) {
				Ride rAfterDel = b.getRide();
				assertTrue(!rAfterDel.isActive());
			}
			assertEquals(-55.0, newTrav1.getIzoztatutakoDirua(), 0.0001);
			assertEquals(115, newTrav1.getMoney(), 0.0001);
			
			testDA.open();
			movements = testDA.findMovementAfterDelUserDriver(newTrav1); 
			assertEquals(movements.get(0).getEragiketa(), "BookDeny");
			Driver newDriver = testDA.getDriver(driverUsername);
			assertNull(newDriver);
			
			
			assertNull(testDA.getCarByTuition(TUITION_1));
			assertNull(testDA.getCarByTuition(TUITION_2));

			testDA.close();
		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				testDA.eraseBookingsAfterDel();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.removeTraveler(travelerUsername);
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
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
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
		    System.out.println(testDA.existDriver(driverUsername));
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 10);
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 10);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
			
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 1, 5, 10);
			testDA.createBooking(driver1, rides.get(1), traveler, 1, 5, 10);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 11);
			testDA.close();
			
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			testDA.open();
			Traveler newTrav1 = testDA.getTraveler(travelerUsername);
			List<Booking> bks1 = testDA.getBookingsByTraveler(traveler);
			testDA.close();
			
			assertEquals("Rejected", bks1.get(0).getStatus().toString());
			assertEquals("Rejected", bks1.get(1).getStatus().toString());

			for (Booking b: bks1) {
				Ride rAfterDel = b.getRide();
				assertTrue(!rAfterDel.isActive());
			}
			assertEquals(-10.0, newTrav1.getIzoztatutakoDirua(), 0.0001);
			assertEquals(25, newTrav1.getMoney(), 0.0001);
			
			testDA.open();
			movements = testDA.findMovementAfterDelUserDriver(newTrav1); 
			assertEquals(movements.get(0).getEragiketa(), "BookDeny");
			Driver newDriver = testDA.getDriver(driverUsername);
			assertNull(newDriver);
			
			
			assertNull(testDA.getCarByTuition(TUITION_1));
			assertNull(testDA.getCarByTuition(TUITION_2));

			testDA.close();
		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				testDA.eraseBookingsAfterDel();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.removeTraveler(travelerUsername);
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}	
	}
	@Test
	// MORE THAN TWO CARS
	public void test21() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 5);
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 5);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.createCar(TUITION_3, MODEL_2, 5, driver1);

			testDA.close();
			
			sut.open();
			sut.deleteUser(driver1);
			sut.close();
			
			testDA.open();
			
			Driver newDriver = testDA.getDriver(driverUsername);
			assertNull(newDriver);
			
			
			assertNull(testDA.getCarByTuition(TUITION_1));
			assertNull(testDA.getCarByTuition(TUITION_2));
			testDA.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			testDA.close();
			sut.close();
			fail();
		} finally {
			try {
				testDA.open();
				testDA.eraseBookingsAfterDel();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.removeTraveler(travelerUsername);
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}	
	}
	@Test
//	 TWO Booking and TWO Movement for the Traveler to erase
//	 TWO Alert, 2 Rides, one Car, Traveler has 100 in Money, one Driver
//	 Should erase Traveler user, Driver in Database, Ride should have extra seats
	public void test22() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
		    System.out.println(testDA.existDriver(driverUsername));
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 15);
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 50);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.close();
			
			System.out.println(rides);
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 2, 50, 10);
			testDA.createBooking(driver1, rides.get(1), traveler, 1, 50, 10);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 10);
			
			// Create the Alert
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate1);
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate2);

			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.getBookingsByTraveler(newTrav);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));
			Booking afterDelBooking2 = testDA.getBookingByRide(driver1, afterDel.get(1));
			
			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			assertEquals(5, afterDel.get(1).getnPlaces(), 0.00001);

			assertNull(testDA.getTraveler(travelerUsername));
			assertEquals("Rejected", afterDelBooking.getStatus().toString());
			assertEquals("Rejected", afterDelBooking2.getStatus().toString());
			assertEquals(0, testDA.getAlertsByUser(newTrav).size(), 0.000001);
			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
//	 TWO Booking and TWO Movement for the Traveler to erase
//	 TWO Alert, 2 Rides, one Car, Traveler has 100 in Money, one Driver
//	 Should erase Traveler user, Driver in Database, Ride should have extra seats
	public void test23() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
		    System.out.println(testDA.existDriver(driverUsername));
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 15);
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 50);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.close();
			
			System.out.println(rides);
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 5, 50, 10);
			testDA.createBooking(driver1, rides.get(1), traveler, 5, 50, 10);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 10);
			
			// Create the Alert
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate1);
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate2);

			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.getBookingsByTraveler(newTrav);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));
			Booking afterDelBooking2 = testDA.getBookingByRide(driver1, afterDel.get(1));
			
			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			assertEquals(5, afterDel.get(1).getnPlaces(), 0.00001);

			assertNull(testDA.getTraveler(travelerUsername));
			assertEquals("Rejected", afterDelBooking.getStatus().toString());
			assertEquals("Rejected", afterDelBooking2.getStatus().toString());
			assertEquals(0, testDA.getAlertsByUser(newTrav).size(), 0.000001);
			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// Should throw exception of user.mota
	public void test24() {
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
	// Should throw exception of user.mota
	public void test25() {
		try {
			User us = new User(null, null, "Hello");
	        sut.deleteUser(us);
	        fail("Expected NullPointerException to be thrown");
	    } catch (NullPointerException e) {
	        assertTrue(true);
	    } catch (Exception e) {
	        fail("Unexpected exception: " + e);
	    }
	}
	@Test
	// Should throw exception of user.mota
	public void test26() {
		try {
			User us = new User(null, null, "World");
	        sut.deleteUser(us);
	        fail("Expected NullPointerException to be thrown");
	    } catch (NullPointerException e) {
	        assertTrue(true);
	    } catch (Exception e) {
	        fail("Unexpected exception: " + e);
	    }
	}
	@Test
	// Can not create booking insufficient money
	public void test27() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 15);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
						
			// THE BOOKING HAS MORE SEATS THAN CAR SEATS
			testDA.open();
			Booking bookingExpected = testDA.bookRideBlackBox(driver1, rides.get(0), traveler, 0, 10, 15);
			assertNull(bookingExpected);

			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			assertTrue(true);
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// Can not create booking insufficient money
	public void test28() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, -1, 15);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
						
			// THE BOOKING HAS MORE SEATS THAN CAR SEATS
			testDA.open();
			Booking bookingExpected = testDA.bookRideBlackBox(driver1, rides.get(0), traveler, 0, 10, 15);
			assertNull(bookingExpected);

			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			sut.close();
			testDA.close();
			assertTrue(true);
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// Same date RIDE VL1
	public void test29() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2 SAME DATE
			if (driver1 != null) {
				testDA.createRideBlackBox(FROM, TO, rideDate1, 2, 5, driverUsername);
				testDA.createRideBlackBox(FROM, TO, rideDate1, 2, 5, driverUsername);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.createCar(TUITION_2, MODEL_2, 5, driver1);
			testDA.close();
						
			// Make the bookings for the same user
			testDA.open();
			// Create the Alert
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate1);
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate1);

			testDA.createBooking(driver1, rides.get(0), traveler, 3, 50, 15);
			testDA.createBooking(driver1, rides.get(1), traveler, 5, 50, 15);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 10);


			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.getBookingsByTraveler(newTrav);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));
			Booking afterDelBooking2 = testDA.getBookingByRide(driver1, afterDel.get(1));


			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			assertEquals(5, afterDel.get(1).getnPlaces(), 0.00001);

			
			assertTrue(afterDel.get(0).isActive());
			assertTrue(afterDel.get(1).isActive());

			assertEquals(0, testDA.getAlertsByUser(newTrav).size(), 0.000001);
			assertEquals("Rejected", afterDelBooking.getStatus().toString());
			assertEquals("Rejected", afterDelBooking2.getStatus().toString());
			testDA.close();

		} catch (RideAlreadyExistException e) {
			// if the program goes to this point fail  
			assertTrue(true);
		} catch (RideMustBeLaterThanTodayException e) {
			// if the program goes to this point fail  
			fail();
		}
		finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	@Test
	// LESS THAN NOW date RIDE VL
	public void test30() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadjasld";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dijaida";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
		List<Movement> movements = new ArrayList<>();

		// Creating both dates
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate1 = null;
		try {
			rideDate1 = sdf.parse("26/09/2024");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
				
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2 SAME DATE
			if (driver1 != null) {
				testDA.createRideBlackBox(FROM, TO, rideDate1, 2, 5, driverUsername);
				testDA.createRideBlackBox(FROM, TO, rideDate1, 2, 5, driverUsername);
			} else {
			    System.out.println("Driver null");
			}
		}  catch (RideMustBeLaterThanTodayException  e) {
			//verify the results
			assertTrue(true);
		} catch (RideAlreadyExistException e) {
			sut.close();
			fail();
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
	
	@Test
	// VL should book with less seats	 
	public void test31() {
		// Define parameters
		String driverUsername = "Daniel";
		String driverPassword = "dsadoaoda";
		
		String travelerUsername = "Pablo";
		String travelerPassword = "dsadas";
		Driver driver1 = null;
		Traveler traveler = null;
		List<Ride> rides = null;
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
			testDA.open();
		    System.out.println(testDA.existDriver(driverUsername));
			if (!testDA.existDriver(driverUsername)) {
			    driver1 = testDA.createDriver(driverUsername, driverPassword);
			} else {
				driver1 = testDA.getDriver(driverUsername);
			}
			// Create the Ride1 and Ride2
			if (driver1 != null) {
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 15);
				testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 50);
			} else {
			    System.out.println("Driver null");
			}
			
			// Assign the rides numbers
			rides = testDA.getRidesByDriver(driver1);
			
			// Create the traveler
			if (testDA.getTraveler(travelerUsername) != null) {
				testDA.removeTraveler(travelerUsername);
			}
			traveler = testDA.createTraveler(travelerUsername, travelerPassword);
			testDA.close();
			
			// Set the money from traveler
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.close();
			
			System.out.println(rides);
			
			// Make the bookings for the same user BOOKINGS IS WITH 100 SEATS
			testDA.open();
			Booking expectedToFail = testDA.bookRideBlackBox(driver1, rides.get(0), traveler, 100, 50, 10);
			assertNull(expectedToFail);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 10);
			
			// Create the Alert
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate1);
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate2);

			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.getBookingsByTraveler(newTrav);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));
			Booking afterDelBooking2 = testDA.getBookingByRide(driver1, afterDel.get(1));
			
			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			assertEquals(5, afterDel.get(1).getnPlaces(), 0.00001);

			assertNull(testDA.getTraveler(travelerUsername));
			assertEquals("Rejected", afterDelBooking.getStatus().toString());
			assertEquals("Rejected", afterDelBooking2.getStatus().toString());
			assertEquals(0, testDA.getAlertsByUser(newTrav).size(), 0.000001);
			testDA.close();

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(true);
		} finally {
			try {
				testDA.open();
				if (testDA.getTraveler(travelerUsername) != null)
					testDA.removeTraveler(travelerUsername);
				if (testDA.existDriver(driverUsername)) {
					testDA.removeDriver(driverUsername);
				}
				testDA.eraseBookingsAfterDel();
				testDA.eraseRidesAfterDelDriver();
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseCarsAfterDelTrav();
				testDA.close();
			} catch (Exception e) {
				e.printStackTrace();
				sut.close();
				testDA.close();
				fail();
			}
		}
	}
}