package sk.stuba.pks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PksApplication {

    public static void main(String[] args) {
        SpringApplication.run(PksApplication.class, args);
    }

}
