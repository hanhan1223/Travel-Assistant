package org.example.travel.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TravelDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public TravelDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }
}
