package clt.controller;

import clt.model.hub.main.HubEvent;
import clt.model.sensor.main.SensorEvent;
import clt.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @PostMapping("/hubs")
    public void addHubEvent(@Valid @RequestBody HubEvent hubEvent) {
        service.addHubEvent(hubEvent);
    }

    @PostMapping("/sensors")
    public void addSensorEvent(@Valid @RequestBody SensorEvent sensorEvent) {
        service.addSensorEvent(sensorEvent);
    }

}
