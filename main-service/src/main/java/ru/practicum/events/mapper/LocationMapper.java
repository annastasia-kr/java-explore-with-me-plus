package ru.practicum.events.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.events.dto.LocationDto;
import ru.practicum.events.model.Location;

@UtilityClass
public class LocationMapper {
    public static LocationDto toLocationDto(Location location) {

        LocationDto locationDto = new LocationDto(location.getLat(), location.getLon());
        return locationDto;
    }

    public static Location toLocation(LocationDto locationDto, Long id) {
        Location location = new Location(id, locationDto.getLat(), locationDto.getLon());
        return location;
    }
}
