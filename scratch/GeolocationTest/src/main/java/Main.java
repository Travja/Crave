import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.DecimalFormat;

public class Main {

    public static void main(String[] args) {

        Location home = new Location(40.773416, -111.914383);
        Location walmart = new Location(40.74043, -111.9011);
        DecimalFormat format = new DecimalFormat("#.#");

        System.out.println("Walmart is " + format.format(distFrom(home, walmart)) + " miles from home.");

    }

    public static double distFrom(Location loc1, Location loc2) {
        double lat1 = loc1.getLat();
        double lon1 = loc1.getLon();
        double lat2 = loc2.getLat();
        double lon2 = loc2.getLon();

        double earthRadius = 3958.75; // Earth radius in miles
        double dLat = Math.toRadians(lat2-lat1); //Get difference in Latitudes
        double dLng = Math.toRadians(lon2-lon1); //Get difference in Longitudes
        double sindLat = Math.sin(dLat / 2); //Calculate the rotation of lat/long
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }

    @Data
    @AllArgsConstructor
    public static class Location {
        private double lat, lon;
    }

}
