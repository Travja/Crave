package me.travja.crave.common.models.geo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Location {
    private double lat, lon;

    public static double distance(Location loc1, Location loc2) {
        double lat1 = loc1.getLat();
        double lon1 = loc1.getLon();
        double lat2 = loc2.getLat();
        double lon2 = loc2.getLon();

        double earthRadius = 3958.75; // Earth radius in miles
        double dLat        = Math.toRadians(lat2 - lat1); //Get difference in Latitudes
        double dLng        = Math.toRadians(lon2 - lon1); //Get difference in Longitudes
        double sindLat     = Math.sin(dLat / 2); //Calculate the rotation of lat/long
        double sindLng     = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c    = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        return dist;
    }

    public double distance(Location loc) {
        return distance(this, loc);
    }
}