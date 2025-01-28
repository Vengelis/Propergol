package fr.vengelis.propergol.monitor.controller;

import fr.vengelis.propergol.monitor.RepoConfig;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;

@Controller("/")
public class HomeController {

    private final RepoConfig config;

    @Value("${micronaut.views.enabled}")
    private final boolean views;

    @Value("${micronaut.views.thymeleaf.encoding}")
    private final String thymeleafEncoding;

    @Value("${micronaut.views.thymeleaf.cache}")
    private final boolean thymeleafCache;

    public HomeController(RepoConfig config) {
        this.config = config;
        this.views = true;
        this.thymeleafEncoding = "UTF-8";
        this.thymeleafCache = false;

    }

    @Get("/")
    @View("base.html")
    public String index() {
        return null;
    }
}
