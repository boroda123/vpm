package com.manager.vpm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class TarExtractor {
    private InputStream tarStream;
    private boolean gzip;
    private Path destination;

    TarExtractor(InputStream in, boolean gzip, Path destination) throws IOException {
        this.tarStream = in;
        this.gzip = gzip;
        this.destination = destination;

        if (!destination.toFile().exists()) {
            Files.createDirectories(destination);
        }
    }

    InputStream getTarStream() {
        return tarStream;
    }

    boolean isGzip() {
        return gzip;
    }

    Path getDestination() {
        return destination;
    }

    abstract void untar() throws IOException;
}
