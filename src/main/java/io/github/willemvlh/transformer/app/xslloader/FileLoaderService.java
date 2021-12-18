package io.github.willemvlh.transformer.app.xslloader;

import io.github.willemvlh.transformer.app.ServerOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileLoaderService {

    private final ServerOptions options;

    @Autowired
    public FileLoaderService(ServerOptions options) {
        this.options = options;
    }

    File getXslFile(String xslServerPath) {
        return new File(this.options.getXslRootPath(), xslServerPath);
    }
}
