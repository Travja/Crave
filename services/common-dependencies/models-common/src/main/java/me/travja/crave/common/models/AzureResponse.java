package me.travja.crave.common.models;

import lombok.Data;
import me.travja.crave.common.models.store.Location;

import java.util.ArrayList;
import java.util.List;

@Data
public class AzureResponse {

    public List<AzureResult> results = new ArrayList<>();


    @Data
    public static class AzureResult {

        public Location     position;
        public AzureAddress address;

        @Data
        public static class AzureAddress {
            public String streetNumber,
                    streetName,
                    municipality, //City
                    municipalitySubdivision,
                    localName, //City
                    freeformAddress,
                    countrySubdivision, //State
                    postalCode;

        }

    }

    /*
{
    "summary": {
        "query": "350 hope ave salt lake city ut",
        "queryType": "NON_NEAR",
        "queryTime": 144,
        "numResults": 1,
        "offset": 0,
        "totalResults": 1,
        "fuzzyLevel": 1,
        "geoBias": {
            "lat": 40.773416,
            "lon": -111.914383
        }
    },
    "results": [
        {
            "type": "Point Address",
            "id": "US/PAD/p1/4698621",
            "score": 16.6965045929,
            "dist": 3834.7211669544686,
            "address": {
                "streetNumber": "350",
                "streetName": "Hope Avenue",
                "municipalitySubdivision": "Ball Park",
                "municipality": "Salt Lake City",
                "countrySecondarySubdivision": "Salt Lake",
                "countrySubdivision": "UT",
                "countrySubdivisionName": "Utah",
                "postalCode": "84115",
                "extendedPostalCode": "84115-5116",
                "countryCode": "US",
                "country": "United States",
                "countryCodeISO3": "USA",
                "freeformAddress": "350 Hope Avenue, Salt Lake City, UT 84115",
                "localName": "Salt Lake City"
            },
            "position": {
                "lat": 40.74043,
                "lon": -111.9011
            },
            "viewport": {
                "topLeftPoint": {
                    "lat": 40.74193,
                    "lon": -111.90308
                },
                "btmRightPoint": {
                    "lat": 40.73893,
                    "lon": -111.89912
                }
            },
            "entryPoints": [
                {
                    "type": "main",
                    "position": {
                        "lat": 40.73893,
                        "lon": -111.90112
                    }
                },
                {
                    "type": "minor",
                    "position": {
                        "lat": 40.739,
                        "lon": -111.90129
                    }
                },
                {
                    "type": "minor",
                    "position": {
                        "lat": 40.73975,
                        "lon": -111.90105
                    }
                },
                {
                    "type": "minor",
                    "position": {
                        "lat": 40.74086,
                        "lon": -111.89971
                    }
                }
            ]
        }
    ]
}
     */
}
