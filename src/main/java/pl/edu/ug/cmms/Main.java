package pl.edu.ug.cmms;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("classpath:beans.xml")
public class Main {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Main.class)
            .web(WebApplicationType.NONE)
            .run(args);
    }
}