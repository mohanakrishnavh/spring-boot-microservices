package io.emkae.moviecatalogservice.resources;

import io.emkae.moviecatalogservice.model.CatalogItem;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/catalog")
@Log4j2
public class MovieCatalogResource {

    @RequestMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {
        log.info("Request catalog information for user : {}", userId);

        return Collections.singletonList(
                new CatalogItem("Transformers", "Test", 4)
        );
    }
}
