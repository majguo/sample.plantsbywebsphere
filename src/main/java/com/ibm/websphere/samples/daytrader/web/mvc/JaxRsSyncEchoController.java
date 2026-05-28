package com.ibm.websphere.samples.daytrader.web.mvc;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.websphere.samples.daytrader.web.prims.jaxrs.TestJSONObject;

@RestController
@RequestMapping("/jaxrs/sync")
public class JaxRsSyncEchoController {

    @GetMapping(value = "/echoText", produces = MediaType.TEXT_PLAIN_VALUE)
    public String echoText(@RequestParam("input") String input) {
        return input;
    }

    @PostMapping(value = "/echoJSON", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TestJSONObject echoJson(@RequestBody TestJSONObject jsonObject) {
        return jsonObject;
    }

    @PostMapping(
            value = "/echoXML",
            consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE },
            produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE })
    public ResponseEntity<String> echoXml(
            @RequestBody String body,
            @RequestParam(name = "contentType", required = false) String ignored) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(body);
    }
}