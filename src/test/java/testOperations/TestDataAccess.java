package testOperations;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import configuration.ConfigXML;
import domain.Alert;
import domain.Booking;
import domain.Car;
import domain.Driver;
import domain.Movement;
import domain.Ride;
import domain.Traveler;
import domain.User;


public class TestDataAccess {
	protected  EntityManager  db;
	protected  EntityManagerFactory emf;

	ConfigXML  c=ConfigXML.getInstance();


	public TestDataAccess()  {
		
		System.out.println("TestDataAccess created");

		//open();
		
	}

	
	public void open(){
		

		String fileName=c.getDbFilename();
		
		if (c.isDatabaseLocal()) {
			  emf = Persistence.createEntityManagerFactory("objectdb:"+fileName);
			  db = emf.createEntityManager();
		} else {
			Map<String, String> properties = new HashMap<String, String>();
			  properties.put("javax.persistence.jdbc.user", c.getUser());
			  properties.put("javax.persistence.jdbc.password", c.getPassword());

			  emf = Persistence.createEntityManagerFactory("objectdb://"+c.getDatabaseNode()+":"+c.getDatabasePort()+"/"+fileName, properties);

			  db = emf.createEntityManager();
    	   }
		System.out.println("TestDataAccess opened");

		
	}
	public void close(){
		db.close();
		System.out.println("TestDataAccess closed");
	}

	public boolean removeDriver(String name) {
		System.out.println(">> TestDataAccess: removeDriver");
		Driver d = db.find(Driver.class, name);
		if (d!=null) {
			db.getTransaction().begin();
			db.remove(d);
			db.getTransaction().commit();
			return true;
		} else 
		return false;
    }
	public Driver createDriver(String name, String pass) {
		System.out.println(">> TestDataAccess: addDriver");
		Driver driver=null;
		db.getTransaction().begin();
		try {
		    driver=new Driver(name,pass);
			db.persist(driver);
			db.getTransaction().commit();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return driver;
    }
	public boolean existDriver(String email) {
		try {
	        // Attempt to find the driver by email
	        Driver driver = db.find(Driver.class, email);
			System.out.println(driver);
	        
	        // If driver is found (not null), return true
	        return driver != null;
	    } catch (Exception e) {
	        // Log the exception and return false
	        System.err.println("Error while trying to find driver: " + e.getMessage());
	        return false;
	    }

	}
		
		public Driver addDriverWithRide(String name, String from, String to,  Date date, int nPlaces, float price) {
			System.out.println(">> TestDataAccess: addDriverWithRide");
				Driver driver=null;
				db.getTransaction().begin();
				try {
					 driver = db.find(Driver.class, name);
					if (driver==null) {
						System.out.println("Entra en null");
						driver=new Driver(name,null);
				    	db.persist(driver);
					}
				    driver.addRide(from, to, date, nPlaces, price);
					db.getTransaction().commit();
					System.out.println("Driver created "+driver);
					
					return driver;
					
				}
				catch (Exception e){
					e.printStackTrace();
				}
				return null;
	    }
		
		
		public boolean existRide(String name, String from, String to, Date date) {
			System.out.println(">> TestDataAccess: existRide");
			Driver d = db.find(Driver.class, name);
			if (d!=null) {
				return d.doesRideExists(from, to, date);
			} else 
			return false;
		}
		public Ride removeRide(String name, String from, String to, Date date ) {
			System.out.println(">> TestDataAccess: removeRide");
			Driver d = db.find(Driver.class, name);
			if (d!=null) {
				db.getTransaction().begin();
				Ride r= d.removeRide(from, to, date);
				db.getTransaction().commit();
				System.out.println("created rides" +d.getCreatedRides());
				return r;

			} else 
			return null;

		}
		public Traveler createTraveler(String name, String pass) {
			Traveler traveler = null;
			db.getTransaction().begin();
			try {
				traveler = new Traveler(name, pass);
				db.persist(traveler);
				db.getTransaction().commit();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return traveler;
		}
		
		public boolean removeTraveler(String name) {
			Traveler t = db.find(Traveler.class, name);
			if (t != null) {
				db.getTransaction().begin();
				db.remove(t);
				db.getTransaction().commit();
				return true;
			} else 
			return false;
	    }
		
		public Car createCar(String tuition, String model, int seats, Driver driver) {
			Car car = null;
			db.getTransaction().begin();
			try {
				Driver d = db.find(Driver.class, driver.getUsername());
				Car carFound = db.find(Car.class, model);
				if (carFound != null) {
					d.addCar(carFound);
					return carFound;
				}
				car = new Car(tuition, model, seats);
				d.addCar(car);
				db.getTransaction().commit();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return car;
		}
		
		public boolean removeCar(String model) {
			Car car = db.find(Car.class, model);
			if (car != null) {
				db.getTransaction().begin();
				db.remove(car);
				db.getTransaction().commit();
				return true;
			} else 
			return false;
		}
		
		public Booking createBooking(Driver d, Ride ride, Traveler traveler, int seats) {
			Booking booking = null;
			db.getTransaction().begin();
			try {
				Driver dr = db.find(Driver.class, d.getUsername());
				List<Ride> rideL = dr.getCreatedRides();
				Ride rideF = db.find(Ride.class, ride);
				// Check if rideF is not null before proceeding
				if (rideF != null) {
				    // Iterate through the list of created rides
				    for (Ride r : rideL) {
				        // Check if the ride from the list matches the ride from the database
				        if (r.equals(rideF)) { // Assuming Ride class implements equals() correctly
				            // Perform your desired action when a match is found
				            System.out.println("Matched Ride: " + r);
				            // You can also break if you only need the first match
				            break;
				        }
				    }
				} else {
				    System.out.println("Ride not found in the database.");
				}
				System.out.println(rideF);
				Traveler travF = db.find(Traveler.class, traveler.getUsername());
				booking = new Booking(rideF, travF, seats);
				booking.setTraveler(travF);
				
				booking.setStatus("Accepted");
				
				travF.addBookedRide(booking);
				
				travF.setMoney(100);
				travF.setIzoztatutakoDirua(10);
				rideF.setnPlaces(rideF.getnPlaces() - seats);
				
				db.merge(travF);
				db.merge(rideF);
				db.persist(booking);
				db.getTransaction().commit();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return booking;
		}
		
		public boolean removeBookingsRide(Ride ride) {
			Ride r = db.find(Ride.class, ride);
			if (r != null) {
				List<Booking> bookings = r.getBookings();
				for (Booking book : bookings) {
					if (book != null) {
						db.getTransaction().begin();
						db.remove(book);
						db.getTransaction().commit();
					} 
				}
			}
			return false;
		}
		
		public Movement createMovement(User user, String operation, double idMov) {
			Movement movement = null;
			db.getTransaction().begin();
			try {
				movement = new Movement(user, operation, idMov);
				db.getTransaction().commit();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return movement;
		}
		
		public boolean removeMovement(double idMov) {
			Movement movement = db.find(Movement.class, idMov);
			if (movement != null) {
				db.getTransaction().begin();
				db.remove(movement);
				db.getTransaction().commit();
				return true;
			} else 
			return false;
		}
		
		public List<Movement> findMovementAfterDelUserDriver(User user) {
			try {
				List<Movement> movs = db.createQuery("SELECT m FROM Movement m WHERE m.user = :user AND m.eragiketa = :eragiketa", Movement.class)
                        .setParameter("user", user)
                        .setParameter("eragiketa", "BookDeny")
                        .getResultList();
			 	System.out.println(movs + " found movements");
		 		return movs;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		}
		public void eraseMovementsAfterDel(User user) {
			try {
				List<Movement> movs = db.createQuery("SELECT m FROM Movement m WHERE m.user = :user AND m.eragiketa = :eragiketa", Movement.class)
		                .setParameter("user", user)
		                .setParameter("eragiketa", "BookDeny")
		                .getResultList();

		        System.out.println(movs.size() + " found movements");

		        // Begin the transaction
		        db.getTransaction().begin();

		        // Loop through the list and remove each movement
		        for (Movement mov : movs) {
		            db.remove(mov);
		        }
		        // Commit the transaction
		        db.getTransaction().commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public void eraseBookingsAfterDel() {
			try {
		        List<Booking> bookings = db.createQuery("SELECT b FROM Booking b", Booking.class).getResultList();


		        System.out.println(bookings.size() + " found bookings");

		        // Begin the transaction
		        db.getTransaction().begin();

		        // Loop through the list and remove each movement
		        for (Booking booking: bookings) {
		            db.remove(booking);
		        }
		        // Commit the transaction
		        db.getTransaction().commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public Ride createNewRide (String from, String to, Date date, int nPlaces, double price, Driver driver) {
			Ride ride = null;
			db.getTransaction().begin();
			try {
				ride = new Ride(from, to, date, nPlaces, price, driver);
				db.persist(ride);
				db.getTransaction().commit();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return ride;
		}
		public Driver getDriver (String email) {
			Driver driver = null;
			if (this.existDriver(email)) {
		        driver = db.find(Driver.class, email);
		        return driver;
			}
			return null;
		}
		public Traveler getTraveler (String email) {
			Traveler traveler = null;
			traveler = db.find(Traveler.class, email);
			if (traveler != null) {
		        return traveler;
			}
			return null;
		}
		public boolean removeNewRide(int id) {
			Ride r = db.find(Ride.class, id);
			if (r != null) {
				db.getTransaction().begin();
				db.remove(r);
				db.getTransaction().commit();
				return true;
			} else 
			return false;
		}
		public List<Ride> getRidesByDriver (Driver d) {
			Driver dr = db.find(Driver.class, d);
			List<Ride> dRl = dr.getCreatedRides();
			if (dRl != null) {
				return dRl;
			}
			return null;
		}
		public List<Booking> getBookingsByTraveler(Traveler t) {
		    if (t == null) {
		        System.out.println("Traveler is null");
		        return null; // Handle null traveler case
		    }
		    
		    try {
		        // Execute the query to find all bookings for the specified traveler
		        Traveler tr = db.find(Traveler.class, t);
		        System.out.println(tr.getBookedRides() + "from TRAVELER");
		        return tr.getBookedRides();
		    } catch (Exception e) {
		        e.printStackTrace();
		        return null;
		    }
		}
		public void eraseRidesAfterDelDriver () {
			try {
				// Step 1: Query for all rides
		        List<Ride> rides = db.createQuery("SELECT r FROM Ride r", Ride.class).getResultList();

		        System.out.println(rides.size() + " found rides to delete");

		        // Begin the transaction
		        db.getTransaction().begin();

		        // Step 2: Loop through the list and remove each ride
		        for (Ride ride : rides) {
		            db.remove(ride); // Remove the ride from the database
		        }

		        // Commit the transaction
		        db.getTransaction().commit();

		        System.out.println("Successfully deleted all rides.");

		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		public Car getCarByTuition (String tuition) {
			Car car = null;
			car = db.find(Car.class, tuition);
			if (c != null) {
				return car;
			}
			return car;
		}
		public Booking getBookingByRide(Driver d, Ride rd) {
			Driver dr = db.find(Driver.class, d);
			List<Ride>rides = dr.getCreatedRides();
			System.out.println(rides.get(0).getBookings());
			System.out.println(rides.get(1).getBookings());
			Booking booking = null;
			
			for(Ride r: rides) {
				System.out.println(r.getBookings());
				Ride newR = db.find(Ride.class, r);
				if (rd.getDate() == newR.getDate()) {
					System.out.println(newR.getBookings());
					booking = newR.getBookings().get(0); 
					return booking;
				}
			}
			return booking;
		}
		public Alert createAlertTraveler(Traveler tr, String from, String to, Date date) {
			db.getTransaction().begin();
			Traveler t = db.find(Traveler.class, tr); // Assuming tr is identified by username
	        Alert al = new Alert(from, to, date, t);

	        // Add the alert to the traveler
	        t.addAlert(al);

	        // Persist the new alert
	        db.persist(al);

	        // Merge the updated traveler to ensure the relationship is stored
	        db.merge(t);

	        // Commit the transaction
	        db.getTransaction().commit();
	        return al;
		}
		public void eraseCarsAfterDelTrav () {
			try {
		        List<Car> cars = db.createQuery("SELECT c FROM Car c", Car.class).getResultList();

		        db.getTransaction().begin();

		        for (Car car : cars) {
		            db.remove(car); // Remove the ride from the database
		        }

		        // Commit the transaction
		        db.getTransaction().commit(); 

		        System.out.println("Successfully deleted all rides.");

		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
	    public void createAlert(String username, String from, String to, Date date, boolean active) {
	        db.getTransaction().begin();
	        Traveler traveler = db.find(Traveler.class, username);
	        if (traveler == null) {
	            traveler = new Traveler(username, "password");
	            db.persist(traveler);
	        }
	        Alert alert = new Alert(from, to, date, traveler);
	        alert.setActive(active);
	        db.persist(alert);
	        db.getTransaction().commit();
	    }
	    public void createRide(String from, String to, Date date, int nPlaces) {
	        db.getTransaction().begin();
	        Ride ride = new Ride(from, to, date, nPlaces, 0, null);
	        db.persist(ride);
	        db.getTransaction().commit();
	    }
	    public Alert getAlert(String username) {
	        return db.createQuery("SELECT a FROM Alert a WHERE a.traveler.username = :username", Alert.class)
	                 .setParameter("username", username)
	                 .getSingleResult();
	    }
	    public void removeAlert(String username) {
	        db.getTransaction().begin();
	        Alert alert = getAlert(username);
	        if (alert != null) {
	            db.remove(alert);
	        }
	        db.getTransaction().commit();
	    }
	    public void removeRide(String from, String to, Date date) {
	        db.getTransaction().begin();
	        Ride ride = db.createQuery("SELECT r FROM Ride r WHERE r.from = :from AND r.to = :to AND r.date = :date", Ride.class)
	                      .setParameter("from", from)
	                      .setParameter("to", to)
	                      .setParameter("date", date)
	                      .getSingleResult();
	        if (ride != null) {
	            db.remove(ride);
	        }
	        db.getTransaction().commit();
	    }
	    public void removeAllAlerts() {
	        db.getTransaction().begin();
	        db.createQuery("DELETE FROM Alert").executeUpdate();
	        db.getTransaction().commit();
	    }

	    public void removeAllRides() {
	        db.getTransaction().begin();
	        db.createQuery("DELETE FROM Ride").executeUpdate();
	        db.getTransaction().commit();
	    } 
	    public List<Alert> getAlerts(String username) {
	        Traveler traveler = db.find(Traveler.class, username);
	        if (traveler != null) {
	            return traveler.getAlerts();
	        }
	        return new ArrayList<>();
	    }
}