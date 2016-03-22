package pl.lodz.p.michalsosn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class LampekApplication {

    private final Logger log = LoggerFactory.getLogger(LampekApplication.class);

    @RequestMapping("/echo")
    public String echo(@RequestParam(value="name", defaultValue="World") String name) {
        log.info("echo called");
        return "Hello " + name;
    }

	public static void main(String[] args) {
		SpringApplication.run(LampekApplication.class, args);
	}

}
