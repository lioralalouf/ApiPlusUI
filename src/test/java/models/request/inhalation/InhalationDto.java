package models.request.inhalation;


import models.request.shared.GeoLocationDto;

public class InhalationDto {

    public String timestamp = "2020-01-01T00:00:00+05:00";
    public Inhalation inhalation = new Inhalation();

    public class Inhalation {
        public EventDto event = new EventDto();
        public DeviceDto device = new DeviceDto();
        public GeoLocationDto geolocation = new GeoLocationDto();
    }
}
