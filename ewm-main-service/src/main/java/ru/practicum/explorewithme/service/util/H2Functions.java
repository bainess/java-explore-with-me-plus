package ru.practicum.explorewithme.service.util;

public class H2Functions {
    public static double distance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 0;
        if (lat1.equals(lat2) && lon1.equals(lon2)) return 0;

        double radLat1 = Math.PI * lat1 / 180;
        double radLat2 = Math.PI * lat2 / 180;
        double theta = lon1 - lon2;
        double radTheta = Math.PI * theta / 180;

        double dist = Math.sin(radLat1) * Math.sin(radLat2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radTheta);
        if (dist > 1) dist = 1;
        dist = Math.acos(dist);
        dist = dist * 180 / Math.PI;
        dist = dist * 60 * 1.8524;

        return dist;
    }
}
