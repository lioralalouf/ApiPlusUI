package models.request.inhalation;

import models.database.Inhalation;
import models.request.PlatformRequestBase;

import java.util.ArrayList;
import java.util.List;

public class UploadInhalationsRequest {

    public List<InhalationDto> inhalations;

    public UploadInhalationsRequest(List<Inhalation> inhalations) {

        this.inhalations = new ArrayList<>();

        for (Inhalation inhalation : inhalations) {

            InhalationDto inhalationDto = new InhalationDto();
            inhalationDto.inhalation.device.serialNumber = inhalation.device.serialNumber;
            inhalationDto.inhalation.event.category = inhalation.event.category;
            inhalationDto.inhalation.event.duration = inhalation.event.duration;
            inhalationDto.inhalation.event.id = inhalation.event.id;
            inhalationDto.inhalation.event.peakFlow = inhalation.event.peakFlow;
            inhalationDto.inhalation.event.peakOffset = inhalation.event.peakOffset;
            inhalationDto.inhalation.event.startOffset = inhalation.event.startOffset;
            inhalationDto.inhalation.event.status = inhalation.event.status;
            inhalationDto.inhalation.event.time = inhalation.event.time;
            inhalationDto.inhalation.event.volume = inhalation.event.volume;

            inhalationDto.inhalation.geolocation.latitude = inhalation.geoLocation.latitude;
            inhalationDto.inhalation.geolocation.longitude = inhalation.geoLocation.longitude;

            this.inhalations.add(inhalationDto);

        }

    }
}


