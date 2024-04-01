package io.streams.midikafkastreams.controller;

import io.streams.midikafkastreams.service.ChordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChordsController {

    private final ChordsService chordsService;

    @Autowired
    public ChordsController(ChordsService chordsService) {
        this.chordsService = chordsService;
    }

    @RequestMapping(value = "/produce/chords", method = RequestMethod.GET)
    public void chordsProduce() {
        chordsService.produceChordsJSON();
    }
}
