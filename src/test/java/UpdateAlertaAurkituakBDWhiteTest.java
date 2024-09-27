import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import dataAccess.DataAccess;
import domain.Alert;
import testOperations.TestDataAccess;

public class UpdateAlertaAurkituakBDWhiteTest {

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
        String username = "Hugo";
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
        String username = "Alex";
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
            testDA.createRide("Valencia", "Sevilla", date, 2);
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
    public void testMultipleAlertsOneMatch() {
        String username = "Juan";
        String from1 = "Barcelona";
        String to1 = "Madrid";
        String from2 = "Valencia";
        String to2 = "Sevilla";
        Date date = null;
        try {
            date = sdf.parse("15/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }

        try {
            testDA.open();
            testDA.createTraveler(username, "password");
            testDA.createAlert(username, from1, to1, date, true);
            testDA.createAlert(username, from2, to2, date, true);
            testDA.createRide(from1, to1, date, 2);
            testDA.createRide(from2, to2, date, 0);
            testDA.close();

            sut.open();
            boolean result = sut.updateAlertaAurkituak(username);
            sut.close();

            assertTrue(result);

            testDA.open();
            List<Alert> alerts = testDA.getAlerts(username);
            boolean foundMatchingAlert = false;
            for (Alert alert : alerts) {
                if (alert.getFrom().equals(from1) && alert.getTo().equals(to1)) {
                    assertTrue(alert.isFound());
                    foundMatchingAlert = true;
                } else {
                    assertFalse(alert.isFound());
                }
            }
            assertTrue(foundMatchingAlert);
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
    public void testMultipleAlertsNoMatch() {
        String username = "Pepe";
        String from1 = "Barcelona";
        String to1 = "Madrid"; 
        String from2 = "Valencia";
        String to2 = "Sevilla";
        Date date = null;
        try {
            date = sdf.parse("15/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }

        try {
            testDA.open();
            testDA.createTraveler(username, "password");
            testDA.createAlert(username, from1, to1, date, true);
            testDA.createAlert(username, from2, to2, date, true);
            testDA.createRide("Bilbao", "Santander", date, 2);
            testDA.close();

            sut.open();
            boolean result = sut.updateAlertaAurkituak(username);
            sut.close();

            assertFalse(result);

            testDA.open();
            List<Alert> alerts = testDA.getAlerts(username);
            for (Alert alert : alerts) {
                assertFalse(alert.isFound());
            }
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
    public void testPartialMatch() {
        String username = "Alfredo";
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
            testDA.createRide(from, "Valencia", date, 2);
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
    public void testUserWithoutAlerts() {
        String username = "usuarioSinAlertas";

        try {
            testDA.open();
            testDA.createTraveler(username, "password");
            testDA.close();

            sut.open();
            boolean result = sut.updateAlertaAurkituak(username);
            sut.close();

            assertFalse(result);
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        } finally {
            testDA.open();
            testDA.removeTraveler(username);
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