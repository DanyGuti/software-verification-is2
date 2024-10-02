import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.text.ParseException;
 
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import dataAccess.DataAccess;
import domain.Alert;
import domain.Ride;
import domain.Traveler;

public class UpdateAlertaAurkituakMockBlacTest {

    static DataAccess sut;
    protected MockedStatic<Persistence> persistenceMock;

    @Mock
    protected EntityManagerFactory entityManagerFactory;

    @Mock
    protected EntityManager db;

    @Mock
    protected EntityTransaction et;

    @Mock
    public TypedQuery<Alert> alertQuery;

    @Mock
    public TypedQuery<Ride> rideQuery;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        persistenceMock = Mockito.mockStatic(Persistence.class);
        persistenceMock.when(() -> Persistence.createEntityManagerFactory(Mockito.any()))
                .thenReturn(entityManagerFactory);
        Mockito.doReturn(db).when(entityManagerFactory).createEntityManager();
        Mockito.doReturn(et).when(db).getTransaction();
        sut = new DataAccess(db);
    }
 
    @After
    public void tearDown() {
        persistenceMock.close(); 
    }

    private void setupUpdateAlerta(String username, List<Alert> alerts, List<Ride> rides) {
        // Mock the behavior of db.createQuery for Alert
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(alertQuery);
        Mockito.when(alertQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(alertQuery);
        Mockito.when(alertQuery.getResultList()).thenReturn(alerts);

        // Mock the behavior of db.createQuery for Ride
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Ride.class))).thenReturn(rideQuery);
        Mockito.when(rideQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(rideQuery);
        Mockito.when(rideQuery.getResultList()).thenReturn(rides);
    }

    @Test
    public void testAlertMatchesRide() {
        String username = "Hugo";
        String from = "Barcelona";
        String to = "Madrid";
        Date date = null;
        Integer nPlaces=2;
        try {
            date = sdf.parse("15/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }

        Traveler traveler = new Traveler(username, "password");
        Alert alert = new Alert(from,to,date,traveler);
        Ride ride = new Ride(from, to, date, nPlaces,0,null);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        List<Ride> rides = new ArrayList<>();
        rides.add(ride);

        setupUpdateAlerta(username, alerts, rides);

        boolean result = sut.updateAlertaAurkituak(username);

        assertTrue(result);
        assertTrue(alert.isFound());

        Mockito.when(db.find(Alert.class, alert.getTo())).thenReturn(alert);
        Alert foundAlert = db.find(Alert.class, alert.getTo());
        assertNotNull(foundAlert);
        assertTrue(foundAlert.isFound());
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

        Traveler traveler = new Traveler(username, "password");
        Alert alert = new Alert(from,to,date,traveler);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        List<Ride> rides = new ArrayList<>();

        setupUpdateAlerta(username, alerts, rides);

        boolean result = sut.updateAlertaAurkituak(username);

        assertFalse(result);
        assertFalse(alert.isFound());

        Mockito.when(db.find(Alert.class, alert.getTo())).thenReturn(alert);
        Alert foundAlert = db.find(Alert.class, alert.getTo());
        assertNotNull(foundAlert);
        assertFalse(foundAlert.isFound());
    }
    
    
    @Test
    public void OnlyOneAtributeMatchWithOther() {
        String username = "Alfredo";
        String from = "Barcelona";
        String to = "Madrid";
        Date date = null;
        try {
            date = sdf.parse("15/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }
 
        Traveler traveler = new Traveler(username, "password");
        Alert alert = new Alert(from, to, date, traveler);
        Ride ride = new Ride("Vitoria", "Valencia", date, 2, 0, null);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        List<Ride> rides = new ArrayList<>();
        rides.add(ride);

        setupUpdateAlerta(username, alerts, rides);

        boolean result = sut.updateAlertaAurkituak(username);

        assertFalse(result);
        assertFalse(alert.isFound());

        Mockito.when(db.find(Alert.class, alert.getTo())).thenReturn(alert);
        Alert foundAlert = db.find(Alert.class, alert.getTo());
        assertNotNull(foundAlert);
        assertFalse(foundAlert.isFound());
    }

    @Test
    public void AlertEqualAsNullAndRideEqualAsNull() {
        String username = "Alfredo";
        Date date = null;
        try {
            date = sdf.parse("15/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }

        Traveler traveler = new Traveler(username, "password");
        Alert alert = new Alert(null, null, null, traveler);
        Ride ride = new Ride(null, null, date, 2, 0, null);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        List<Ride> rides = new ArrayList<>();
        rides.add(ride);

        setupUpdateAlerta(username, alerts, rides);

        boolean result = sut.updateAlertaAurkituak(username);

        assertFalse(result); 
        assertFalse(alert.isFound());

        Mockito.when(db.find(Alert.class, alert.getTo())).thenReturn(alert);
        Alert foundAlert = db.find(Alert.class, alert.getTo());
        assertNotNull(foundAlert);
        assertFalse(foundAlert.isFound());
    }
    
    @Test(expected = Exception.class)
    public void testNonExistentUser() {
        String username = "usuarioError";
        
        Mockito.when(db.createQuery(Mockito.anyString(), Mockito.eq(Alert.class))).thenReturn(alertQuery);
        Mockito.when(alertQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(alertQuery);
        Mockito.when(alertQuery.getResultList()).thenReturn(new ArrayList<>());

        sut.updateAlertaAurkituak(username);
    }
}