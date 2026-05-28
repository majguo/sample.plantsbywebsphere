package com.ibm.websphere.samples.daytrader.web.mvc;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ibm.websphere.samples.daytrader.streaming.StreamingHub;

@RestController
@RequestMapping("/rest/broadcastevents")
public class BroadcastEventsController {

    private final StreamingHub streamingHub;

    public BroadcastEventsController(StreamingHub streamingHub) {
        this.streamingHub = streamingHub;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter register() {
        return streamingHub.registerBroadcastEmitter();
    }
}