package tn.esprit.utils;

import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;

public class FrenchAnalysisConfigurer implements LuceneAnalysisConfigurer {
    @Override
    public void configure(LuceneAnalysisConfigurationContext context) {
        context.analyzer("french").custom()
                .tokenizer("standard")
                .tokenFilter("lowercase")
                .tokenFilter("asciifolding")
                .tokenFilter("frenchElision")
                .tokenFilter("frenchLightStem");
    }
}