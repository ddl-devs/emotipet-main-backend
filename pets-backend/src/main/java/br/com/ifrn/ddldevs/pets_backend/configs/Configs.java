package br.com.ifrn.ddldevs.pets_backend.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationArgumentResolver;
import java.util.List;

@Configuration
public class Configs implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new SpecificationArgumentResolver());
    }
}
