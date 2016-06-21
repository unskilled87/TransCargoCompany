package services.Order;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import model.DAO.Impl.OrderDAOImpl;
import model.Entities.CargoStatus;
import model.Entities.Order;
import model.Entities.Waypoint;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.*;


public class SetWeightAndVolume {

    private static final String API_KEY = "AIzaSyDIkAgFpUkSMtaQMqI3yOaA4dYh4PFlL2A";
    public static final int METERS_TO_KM = 1000;
    public static final int SEC_TO_HOURS = 3600;

    private float maxWeight;
    private float maxVolume;
    private float currentWeight;
    private float currentVolume;
    private float distance;
    private float duration;

    private static Logger logger = Logger.getLogger(SetWeightAndVolume.class);
    private static DirectionsResult directionsResult;

    public void setMaxWeightAndVolume(String orderId, SessionFactory sessionFactory) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            OrderDAOImpl orderDAO = new OrderDAOImpl(session);
            Transaction transaction = session.beginTransaction();
            Order order = (Order) orderDAO.read(Integer.parseInt(orderId));

            List<Waypoint> orderWaypoints = GetOrderWaypoints.getOrderWaypoints(orderId, sessionFactory);
            String[] waypointsCities = getWaypointsCities(orderWaypoints);
            String[] strings = removeDuplicateCities(waypointsCities);
            String[] trueOrder = getTrueOrder(strings);
            List<Waypoint> trueWaypointOrder = getTrueWaypointsOrder(trueOrder, orderWaypoints);
            order.setWaypointList(trueWaypointOrder);
            order.setMaxVolume(maxVolume);
            order.setMaxWeight(maxWeight);
            System.out.println(distance);
            System.out.println(duration);
            order.setRouteDistance(distance / METERS_TO_KM);
            order.setRouteDuration(duration / SEC_TO_HOURS);
            transaction.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private String[] getWaypointsCities(List<Waypoint> cargoesWaypoints) {
        String[] result = new String[cargoesWaypoints.size()];
        for (int i = 0; i < cargoesWaypoints.size(); i++) {
            result[i] = cargoesWaypoints.get(i).getWaypointCity().getCityName();
        }
        return result;
    }

    private String[] removeDuplicateCities(String[] citiesName) {
        Set<String> citiesNames = new HashSet<>(Arrays.asList(citiesName));
        String[] strings = citiesNames.toArray(new String[citiesNames.size()]);
        return strings;
    }

    private String[] getTrueOrder(String[] citiesName) {
        String[] trueOrder = new String[citiesName.length + 1];
        GeoApiContext context = new GeoApiContext();
        context.setApiKey(API_KEY);
        try {
            directionsResult = DirectionsApi.newRequest(context)
                    .origin("Saint Petersburg")
                    .destination("Saint Petersburg")
                    .optimizeWaypoints(true)
                    .waypoints(citiesName)
                    .await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (DirectionsLeg leg : directionsResult.routes[0].legs) {
            distance += leg.distance.inMeters;
            duration += leg.duration.inSeconds;
        }
        int[] waypointOrder = directionsResult.routes[0].waypointOrder;
        trueOrder[0] = directionsResult.routes[0].legs[0].startAddress;
        for (int i = 1; i < waypointOrder.length; i++) {
            trueOrder[i] = citiesName[waypointOrder[i]];
        }
        return trueOrder;
    }

    private List<Waypoint> getTrueWaypointsOrder(String[] trueOrder, List<Waypoint> cargoesWaypoints) {
        List<Waypoint> result = new ArrayList<>();
        for (int i = 0; i < trueOrder.length; i++) {
            String cityName = trueOrder[i];
            for (Iterator<Waypoint> iterator = cargoesWaypoints.iterator(); iterator.hasNext(); ) {
                Waypoint waypoint = iterator.next();
                if (waypoint.getWaypointType().getWaypointStatusName().equals("Unloading")
                        && waypoint.getWaypointCargo().getCargoStatus().getStatusName().equals("Shipped")
                        && waypoint.getWaypointCity().getCityName().equals(cityName)) {
                    currentWeight -= waypoint.getWaypointCargo().getWeight();
                    currentVolume -= waypoint.getWaypointCargo().getVolume();
                    checkMaxWeightAndVolume(currentWeight, currentVolume);
                    result.add(waypoint);
                    iterator.remove();
                    continue;
                }

                if (waypoint.getWaypointType().getWaypointStatusName().equals("Loading")
                        && waypoint.getWaypointCargo().getCargoStatus().getStatusName().equals("Ready")
                        && waypoint.getWaypointCity().getCityName().equals(cityName)) {
                    CargoStatus cargoStatus = new CargoStatus();
                    cargoStatus.setStatusName("Shipped");
                    waypoint.getWaypointCargo().setCargoStatus(cargoStatus);
                    currentWeight += waypoint.getWaypointCargo().getWeight();
                    currentVolume += waypoint.getWaypointCargo().getVolume();
                    checkMaxWeightAndVolume(currentWeight, currentVolume);
                    result.add(waypoint);
                    iterator.remove();
                    continue;
                }
            }
        }

        for (int i = trueOrder.length - 1; i >= 0; i--) {
            String cityName = trueOrder[i];
            for (Iterator<Waypoint> iterator = cargoesWaypoints.iterator(); iterator.hasNext(); ) {
                Waypoint waypoint = iterator.next();
                if (waypoint.getWaypointType().getWaypointStatusName().equals("Unloading")
                        && waypoint.getWaypointCargo().getCargoStatus().getStatusName().equals("Shipped")
                        && waypoint.getWaypointCity().getCityName().equals(cityName)) {
                    currentWeight -= waypoint.getWaypointCargo().getWeight();
                    currentVolume -= waypoint.getWaypointCargo().getVolume();
                    checkMaxWeightAndVolume(currentWeight, currentVolume);
                    result.add(waypoint);
                    iterator.remove();
                }
            }
        }
        for (Waypoint waypoint : result) {
            System.out.println(waypoint.getWaypointCity() + " " + waypoint.getWaypointType() + " " + waypoint.getWaypointCargo().getName());
        }
        return result;
    }

    private void checkMaxWeightAndVolume(float weight, float volume) {
        if (weight > maxWeight) {
            maxWeight = weight;
        }
        if (volume > maxVolume) {
            maxVolume = volume;
        }
    }
}
