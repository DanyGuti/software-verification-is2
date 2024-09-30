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

public class UpdateAlertaAurkituakMockWhiteTest {

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
        try {
            date = sdf.parse("15/10/2024");
        } catch (ParseException e) {
            fail("Error parsing date");
        }

        Traveler traveler = new Traveler(username, "password");
        Alert alert = new Alert(traveler, from, to, date);
        Ride ride = new Ride(from, to, date, 2);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        List<Ride> rides = new ArrayList<>();
        rides.add(ride);

        setupUpdateAlerta(username, alerts, rides);

        boolean result = sut.updateAlertaAurkituak(username);

        assertTrue(result);
        assertTrue(alert.isFound());

        Mockito.verify(db).persist(alert);
        Mockito.verify(et).begin();
        Mockito.verify(et).commit();
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
        Alert alert = new Alert(traveler, from, to, date);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        List<Ride> rides = new ArrayList<>();

        setupUpdateAlerta(username, alerts, rides);

        boolean result = sut.updateAlertaAurkituak(username);

        assertFalse(result);
        assertFalse(alert.isFound());

        Mockito.verify(db, Mockito.never()).persist(alert);
        Mockito.verify(et).begin();
        Mockito.verify(et).commit();
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

        Traveler traveler = new Traveler(username, "password");
        Alert alert1 = new Alert(traveler, from1, to1, date);
        Alert alert2 = new Alert(traveler, from2, to2, date);
        Ride ride = new Ride(from1, to1, date, 2);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert1);
        alerts.add(alert2);
        List<Ride> rides = new ArrayList<>();
        rides.add(ride);

        setupUpdateAlerta(username, alerts, rides);

        boolean result = sut.updateAlertaAurkituak(username);

        assertTrue(result);
        assertTrue(alert1.isFound());
        assertFalse(alert2.isFound());

        Mockito.verify(db).persist(alert1);
        Mockito.verify(db, Mockito.never()).persist(alert2);
        Mockito.verify(et).begin();
        Mockito.verify(et).commit();
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