package com.example.demo;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ExtendWith(SpringExtension.class)
public class WebClientContentTypeTest {

    @TempDir
    Path dir;
    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    @BeforeEach
    void init() {
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
        WireMock.stubFor(WireMock.post("/").willReturn(aResponse()));
    }

    @AfterEach
    void cleanup() {
        wireMockServer.stop();
    }

    @Test
    void webClientContentTypeApplicationOctetStream() throws IOException {
        Path f = Files.writeString(dir.resolve("file.notpdf"), "CONTENT");
        WebClient.create().post().uri("http://localhost:" + wireMockServer.port())
                .contentType(MediaType.APPLICATION_PDF)
                .body(BodyInserters.fromResource(new FileSystemResource(f)))
                .exchange().block();

        WireMock.verify(WireMock.postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/zip")));
    }

    @Test
    void webClientContentTypePdf() throws IOException {
        Path f = Files.writeString(dir.resolve("file.pdf"), "CONTENT");
        WebClient.create().post().uri("http://localhost:" + wireMockServer.port())
                .contentType(MediaType.APPLICATION_PDF)
                .body(BodyInserters.fromResource(new FileSystemResource(f)))
                .exchange().block();

        WireMock.verify(WireMock.postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo(MediaType.APPLICATION_PDF_VALUE)));
    }
}
