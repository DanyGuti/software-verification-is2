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
import testOperations.TestDataAccess;

// Created by Daniel Gutierrez Gomez
public class DeleteUserBDWhiteTest {

	static DataAccess sut = new DataAccess();
	 
	// Additional operations needed to execute the test 
	static TestDataAccess testDA = new TestDataAccess();
	 
	// Constants
	private static final String FROM = "SAN SEBASTIAN";
	private static final String TO = "MADRID";
	
	private static final String MODEL_1 = "Audi";
	private static final String TUITION_1 = "1234ABC";
	private static final String MODEL_2 = "Citroen";
	private static final String TUITION_2 = "5678DEF";


    @SuppressWarnings("unused")	
	
	@Test
	// Testing deleteUser dataAccess when user.mota == "Driver"
	// user does not have Car nor Rides
	// Should expect to not delete user() correctly
	public void test1() {
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
			
			assertTrue(testDA.existDriver(driverUsername));
		} catch (Exception e) {
			sut.close();
			fail();
		} finally {
			testDA.open();
			if (driverCreated || testDA.existDriver(driverUsername))
				testDA.removeDriver(driverUsername);
          	testDA.close();
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
		Driver driver1 = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date rideDate = null;
		try {
			rideDate = sdf.parse("05/10/2026");
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		boolean driverCreated = false;
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
				driver1 = testDA.createDriver(driverUsername, driverPassword);
			    driverCreated = true;
			}
			testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate, 5, 10);
			
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
			System.out.println("Should have cars if Driver has Rides");
			assertTrue(true);
			sut.close();
		} finally {
			testDA.open();
			if (driverCreated || testDA.existDriver(driverUsername))
				testDA.removeDriver(driverUsername);
			testDA.close();
		}
	}
	
	@Test
	// Testing us.mota == "Driver", there are more than one Ride
	// Driver has no cars at all
	// SHOULD Fail()
	public void test3() {
		// Define parameters
		String driverUsername="Daniel";
		String driverPassword="dsadoaoda";
		Driver driver1 = null;
		
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
		
		boolean driverCreated = false;
		try {
			testDA.open();
			if (!testDA.existDriver(driverUsername)) {
				driver1 = testDA.createDriver(driverUsername, driverPassword);
			    driverCreated = true;
			}
			testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate1, 5, 10);
			testDA.addDriverWithRide(driverUsername, FROM, TO, rideDate2, 5, 10);
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
			System.out.println("Should have cars if Driver has Rides");
			assertTrue(true);
			sut.close();
		} finally {
			testDA.open();
			if (driverCreated || testDA.existDriver(driverUsername))
				testDA.removeDriver(driverUsername);
			testDA.close();
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
			
			
			// 	Assign and create car to the Driver
			testDA.open();
			testDA.createCar(TUITION_1, MODEL_1, 5, driver1);
			testDA.close();
			
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 3);
			testDA.createBooking(driver1, rides.get(1), traveler, 3);
			
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
			System.out.println("dksna" + newTrav.getMoney());
			testDA.close();
			
			assertEquals("Rejected", bks1.get(0).getStatus().toString());
			assertEquals("Rejected", bks1.get(1).getStatus().toString());

			for (Booking b: bks1) {
				Ride rAfterDel = b.getRide();
				assertTrue(!rAfterDel.isActive());
			}
			assertEquals(-20.0, newTrav.getIzoztatutakoDirua(), 0.0001);
			assertEquals(130, newTrav.getMoney(), 0.0001);
			
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
	// Should cover 2 bookings from a user
	// setting the movements and money to the user
	// adding also the status as accepted,
	// setting the frozen money of Traveler
	// Traveler should have initial money plus the price
	// Driver is erased, and also its cars (2 in this case erased)
	// theres is a new movement created
	public void test5() {
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
			testDA.createBooking(driver1, rides.get(0), traveler, 3);
			testDA.createBooking(driver1, rides.get(1), traveler, 3);
			
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
			assertEquals("Rejected", bks1.get(1).getStatus().toString());

			for (Booking b: bks1) {
				Ride rAfterDel = b.getRide();
				assertTrue(!rAfterDel.isActive());
			}
			assertEquals(-20.0, newTrav.getIzoztatutakoDirua(), 0.0001);
			assertEquals(130, newTrav.getMoney(), 0.0001);
			
			testDA.open();
			movements = testDA.findMovementAfterDelUserDriver(newTrav); 
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
	// -----------------------------------------------TRAVELER TESTS------------------------------------------
	@Test
	// Testing deleteUser dataAccess when user.mota == "Traveler"
	// user does not have Car nor Rides
	// Should expect to delete user as () correctly
	public void test6() {
		// Define parameters
		String travUsername ="Daniel";
		String travPassword="dsadoaoda";
		Traveler trav = null;
		
		try {
			testDA.open();
			if (testDA.getTraveler(travUsername) == null) {
				trav = testDA.createTraveler(travUsername, travPassword);
			}
			testDA.close();		
			
			// Invoke SUT
			sut.open();
			sut.deleteUser(trav);
			sut.close();
			
			testDA.open();
			if (testDA.getTraveler(travUsername) == null) {
				assertTrue(true);
			}
			testDA.close();
		} catch (Exception e) {
			sut.close();
			fail();
		} finally {
			testDA.open();
			if (testDA.getTraveler(travUsername) != null)
				testDA.removeTraveler(travUsername);
          	testDA.close();
        }
	}
	
	@Test
	// One Booking and one Movement for the Traveler to erase
	// NO Alert, 2 Rides, one Car, Traveler has 100 in Money, one Driver
	// Should erase Traveler user, Driver in Database, Ride should have extra seats
	public void test7() {
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
			testDA.close();
			
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 3);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			Traveler newTrav = testDA.getTraveler(travelerUsername);
			testDA.close();
			
			sut.open();
			sut.deleteUser(newTrav);
			sut.close();
			
			testDA.open();
			List<Ride> afterDel = testDA.getRidesByDriver(driver1);
			
			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));
			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			assertNull(testDA.getTraveler(travelerUsername));
			System.out.println(afterDelBooking);
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
				testDA.eraseMovementsAfterDel(traveler);
				testDA.eraseRidesAfterDelDriver();
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
//	 ONE Alert, 2 Rides, one Car, Traveler has 100 in Money, one Driver
//	 Should erase Traveler user, Driver in Database, Ride should have extra seats
	public void test8() {
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
			testDA.close();
			
			System.out.println(rides);
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 3);
			testDA.createBooking(driver1, rides.get(1), traveler, 5);
			
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
			System.out.println(afterDel + "después");

			Booking afterDelBooking = testDA.getBookingByRide(driver1, afterDel.get(0));
			Booking afterDelBooking2 = testDA.getBookingByRide(driver1, afterDel.get(1));

			assertEquals(5, afterDel.get(0).getnPlaces(), 0.00001);
			assertEquals(5, afterDel.get(1).getnPlaces(), 0.00001);

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
//	 TWO Booking and TWO Movement for the Traveler to erase
//	 NO Alert, 2 Rides, one Car, Traveler has 100 in Money, one Driver
//	 Should erase Traveler user, Driver in Database, Ride should have extra seats
	public void test9() {
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
			testDA.close();
			
			System.out.println(rides);
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 3);
			testDA.createBooking(driver1, rides.get(1), traveler, 5);
			
			// Make the movements
			testDA.createMovement(traveler, "BookFreeze", 10);
			testDA.createMovement(traveler, "BookFreeze", 10);
			
			// Create the Alert
			testDA.createAlertTraveler(traveler, FROM, TO, rideDate1);

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
	public void test10() {
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
			testDA.close();
			
			System.out.println(rides);
			
			// Make the bookings for the same user
			testDA.open();
			testDA.createBooking(driver1, rides.get(0), traveler, 3);
			testDA.createBooking(driver1, rides.get(1), traveler, 5);
			
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
	// Should throw null exception or not accepting different types of
	// user (admin, traveler, driver)
	public void test11(){
		try {
	        sut.deleteUser(null);
	        fail("Expected NullPointerException to be thrown");
	    } catch (NullPointerException e) {
	        assertTrue(true);
	    } catch (Exception e) {
	        fail("Unexpected exception: " + e);
	    }
	}
	
}
	
