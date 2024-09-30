import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import dataAccess.DataAccess;
import domain.Alert;
import testOperations.TestDataAccess;

public class UpdateAlertaAurkituakBDBlackTest {

    private static DataAccess sut = new DataAccess();
    private static TestDataAccess testDA = new TestDataAccess();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); 

    @Before
    public void setUp() {
        testDA.open(); 
        testDA.removeAllAlerts();
        testDA.removeAllRides(); 
        testDA.close();
    }
 
    @Test
    public void testAlertMatchesRide() {
        String username = "Lucas";
        String from = "Barcelona";
        String to = "Madrid";
        Date date = null;
        try {
            date = sdf.parse("15/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }

        try {
            testDA.open();
            testDA.createTraveler(username, "password");
            testDA.createAlert(username, from, to, date, true);
            testDA.createRide(from, to, date, 2);
            testDA.close();

            sut.open();
            boolean result = sut.updateAlertaAurkituak(username);
            sut.close();

            assertTrue(result);

            testDA.open();
            Alert alert = testDA.getAlert(username);
            assertTrue(alert.isFound());
            testDA.close();
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        } finally {
            testDA.open();
            testDA.removeTraveler(username);
            testDA.removeAllAlerts();
            testDA.removeAllRides();
            testDA.close();
        }
    }

    @Test
    public void testNoMatchingRide() {
        String username = "Pedro";
        String from = "Donostia";
        String to = "Madrid";
        Date date = null;
        Date date2 = null;
        try {
            date = sdf.parse("15/10/2024");
            date2 = sdf.parse("16/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }

        try {
            testDA.open();
            testDA.createTraveler(username, "password");
            testDA.createAlert(username, from, to, date, false);
            testDA.createRide("Valencia", "Sevilla", date2, 0);
            testDA.close();

            sut.open();
            boolean result = sut.updateAlertaAurkituak(username);
            sut.close();

            assertFalse(result);

            testDA.open();
            Alert alert = testDA.getAlert(username);
            assertFalse(alert.isFound());
            testDA.close();
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        } finally {
            testDA.open();
            testDA.removeTraveler(username);
            testDA.removeAllAlerts();
            testDA.removeAllRides();
            testDA.close();
        }
    }

 

    @Test
    public void OnlyOneAtributeMatchWithOther() {
        String username = "Alfonso";
        String from = "Donosita";
        String to = "Vitoria";
        Date date = null;
        try {
            date = sdf.parse("15/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }

        try {
            testDA.open();
            testDA.createTraveler(username, "password");
            testDA.createAlert(username, from, to, date, true);
            testDA.createRide("Barcelona", "Valencia", date, 2);
            testDA.close();

            sut.open(); 
            boolean result = sut.updateAlertaAurkituak(username);
            sut.close();

            assertFalse(result);
            
            testDA.open();
            Alert alert = testDA.getAlert(username);
            assertFalse(alert.isFound());
            testDA.close();
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        } finally {
            testDA.open();
            testDA.removeTraveler(username);
            testDA.removeAllAlerts();
            testDA.removeAllRides();
            testDA.close();
        }
    }

    
    @Test
    public void AlertEqualAsNullAndRideEqualAsNull() {
        String username = "Alfredo";
        String from = null;
        String to = null;
        Date date = null;
        try {
            date = sdf.parse("15/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }

        try {
            testDA.open();
            testDA.createTraveler(username, "password");
            testDA.createAlert(username, from, to, date, false);
            testDA.createRide(from, to, date, 0);
            testDA.close();

            sut.open();
            boolean result = sut.updateAlertaAurkituak(username);
            sut.close();

            assertFalse(result);

            testDA.open();
            Alert alert = testDA.getAlert(username);
            assertFalse(alert.isFound());
            testDA.close();
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        } finally {
            testDA.open();
            testDA.removeTraveler(username);
            testDA.removeAllAlerts();
            testDA.removeAllRides();
            testDA.close();
        }
    }

    @Test(expected = Exception.class)
    public void testNonExistentUser() {
        String username = "usuarioError";

        sut.open();
        sut.updateAlertaAurkituak(username);
        sut.close();
    }
}