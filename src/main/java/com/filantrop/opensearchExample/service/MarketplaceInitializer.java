package com.filantrop.opensearchExample.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filantrop.opensearchExample.model.Product;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import jakarta.json.stream.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.core.ReindexRequest;
import org.opensearch.client.opensearch.core.reindex.Destination;
import org.opensearch.client.opensearch.core.reindex.Source;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.update_aliases.Action;
import org.opensearch.client.opensearch.indices.update_aliases.AddAction;
import org.opensearch.client.opensearch.indices.update_aliases.RemoveAction;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexInformation;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceInitializer implements InitializingBean {
    private final ElasticsearchOperations elasticsearchOperations;
    private final OpenSearchClient openSearchClient;
    private final JsonpMapper jsonpMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() throws IOException {
        log.info("Initializing MarketplaceInitializer");
        log.info("Cluster health status: {}", elasticsearchOperations.cluster().health());

        Map<String, Object> mappingFromCluster = elasticsearchOperations.indexOps(Product.class).getMapping();
        log.info("Mapping found in elastic: {}", mappingFromCluster);

        // todo: json is not equal need another method
        Document mappingFromClass = elasticsearchOperations.indexOps(Product.class).createMapping();
        log.info("Mapping found in class: {}", mappingFromClass);


        if (!isMappingsAreEquals(mappingFromCluster, mappingFromClass)) {
            List<IndexInformation> information = elasticsearchOperations.indexOps(Product.class).getInformation();
            if (!information.isEmpty()) {
                String sourceIndex = information.get(0).getName();
                String alias = information.get(0).getAliases().get(0).getAlias();
                String destIndex = alias + "-" + Instant.now().getEpochSecond();

                boolean exists = openSearchClient.indices().exists(builder -> builder.index(destIndex)).value();
                if (!exists) {
                    CreateIndexRequest request = CreateIndexRequest.of(
                            builder -> {
                                try {
                                    return builder.index(destIndex)
                                            .mappings(convert(mappingFromClass));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );

                    openSearchClient.indices().create(request);
                }

                ReindexRequest reindexRequest = new ReindexRequest.Builder()
                        .waitForCompletion(true)
                        .source(Source.of(builder -> builder.index(sourceIndex)))
                        .dest(Destination.of(builder -> builder.index(destIndex)))
                        .build();
                openSearchClient.reindex(reindexRequest);

                openSearchClient.indices().updateAliases(
                        builder -> builder.actions(
                                Action.of(builder1 -> builder1.add(
                                        AddAction.of(builder2 -> builder2.alias(alias).index(destIndex)))
                                ),
                                Action.of(builder1 -> builder1.remove(
                                        RemoveAction.of(builder2 -> builder2.alias(alias).index(sourceIndex)))
                                )
                        )
                );

                openSearchClient.indices().delete(builder -> builder.index(sourceIndex));
            }
        }

        Map<String, Object> newActualMapping = elasticsearchOperations.indexOps(Product.class).getMapping();
        log.info("Mapping found in elastic: {}", mappingFromCluster);
        MapDifference<String, Object> diff2 = Maps.difference(mappingFromClass, newActualMapping);
        if (!diff2.areEqual()) {
            log.info("Not equal found in elastic");
        }
    }

    private boolean isMappingsAreEquals(Map<String, Object> mappingFromCluster, Document mappingFromClass) throws JsonProcessingException {
        //todo: create method to compare two mappings
/*        JsonNode mappingFromClassJson = objectMapper.readTree(objectMapper.writeValueAsString(mappingFromClass));
        JsonNode mappingFromClusterJson = objectMapper.readTree(objectMapper.writeValueAsString(mappingFromCluster));
        return mappingFromClassJson.equals(mappingFromClusterJson);*/
        return true;
    }

    private TypeMapping convert(Document mapping) throws IOException {
        String type = objectMapper.writeValueAsString(mapping);

        try (JsonParser mappingsParser =
                     jsonpMapper
                             .jsonProvider()
                             .createParser(new StringReader(type))) {

            return TypeMapping._DESERIALIZER.deserialize(mappingsParser, jsonpMapper);
        }
    }
}
