package sk.stuba.pks;

import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.function.Supplier;

//@EnableAsync
@SpringBootApplication
public class PksApplication {

    public static void main(String[] args) {
        SpringApplication.run(PksApplication.class, args);
    }

}
