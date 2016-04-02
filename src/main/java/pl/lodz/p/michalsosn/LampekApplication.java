package pl.lodz.p.michalsosn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@EnableAsync
@SpringBootApplication
public class LampekApplication {

    public static void main(String[] args) {
        SpringApplication.run(LampekApplication.class, args);
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver() {
            @Override
            public boolean isMultipart(HttpServletRequest request) {
                String method = request.getMethod().toLowerCase();
                if (!Arrays.asList("put", "post").contains(method)) {
                    return false;
                }
                String contentType = request.getContentType();
                return contentType != null
                    && contentType.toLowerCase().startsWith("multipart/");
            }
        };
    }

}
