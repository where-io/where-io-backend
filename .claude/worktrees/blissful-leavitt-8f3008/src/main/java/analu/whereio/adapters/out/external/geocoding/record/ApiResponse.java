package analu.whereio.adapters.out.external.geocoding.record;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ApiResponse {

    private List<Result> results;
    private String status;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // =========================

    public static class Result {

        @JsonProperty("navigation_points")
        private List<NavigationPoint> navigationPoints;

        public List<NavigationPoint> getNavigationPoints() {
            return navigationPoints;
        }

        public void setNavigationPoints(List<NavigationPoint> navigationPoints) {
            this.navigationPoints = navigationPoints;
        }
    }

    // =========================

    public static class NavigationPoint {

        private Location location;

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

    // =========================

    public static class Location {

        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
