package me.travja.crave.common.models.store;

import lombok.*;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.conf.Variables;
import me.travja.crave.common.models.AzureResponse;
import org.hibernate.Hibernate;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Objects;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private static RestTemplate restTemplate;
    @Transient
    @Setter(AccessLevel.PRIVATE)
    private static Variables    variables;

    static {
        restTemplate = AppContext.getBean(RestTemplate.class);
        variables = AppContext.getBean(Variables.class);
    }

    @Id
    @GeneratedValue
    private long id;

    private double lat = 0, lon = 0;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

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

    public static Location fromAddress(String address) {
        String url = "https://atlas.microsoft.com/search/fuzzy/json" +
                "?api-version=1.0" +
                "&query=" + address +
                "&subscription-key=" + variables.AZURE_KEY +
                "&lat=41" +
                "&lon=-111";

        AzureResponse res = restTemplate.getForObject(url, AzureResponse.class);

        if (res.getResults().isEmpty())
            return new Location(0, 0);
        else
            return res.getResults().get(0).getPosition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id);
    }
}