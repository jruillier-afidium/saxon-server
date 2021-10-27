package io.github.willemvlh.transformer.app;
import org.apache.commons.cli.ParseException;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableCaching
class TransformerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TransformerApplication.class);
        try {
            ServerOptions options = ServerOptions.fromArgs(args);
            Map<String, Object> optionsMap = new HashMap<>();
            optionsMap.put("server.port", options.getPort());
            optionsMap.put("logging.file.name", options.getLogFilePath());
            app.setDefaultProperties(optionsMap);
            app.setLogStartupInfo(false);
            app.setBannerMode(Banner.Mode.OFF);
            app.run(args);

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
