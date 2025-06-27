package com.filantrop.opensearchExample.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filantrop.opensearchExample.model.Product;
import jakarta.json.stream.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Script;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.core.ReindexRequest;
import org.opensearch.client.opensearch.core.UpdateRequest;
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
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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


    //private final String indexName = "marketplace";

    @Override
    public void afterPropertiesSet() throws IOException {
        log.info("Initializing MarketplaceInitializer");
        log.info("Cluster health status: {}", elasticsearchOperations.cluster().health());

        List<IndexInformation> information = elasticsearchOperations.indexOps(Product.class).getInformation();
        String sourceIndex = information.get(0).getName();

        boolean needReIndex = false;
        try {
            elasticsearchOperations.indexOps(IndexCoordinates.of(sourceIndex)).putMapping(Product.class);
        } catch (RuntimeException exception) {
            log.info("On update mapping error occurs!", exception);
            needReIndex = true;
        }

        if (needReIndex) {
            log.info("Start reindex!");
            if (!information.isEmpty()) {
                String alias = information.get(0).getAliases().get(0).getAlias();
                String destIndex = alias + "-" + Instant.now().getEpochSecond();

                boolean exists = openSearchClient.indices().exists(builder -> builder.index(destIndex)).value();
                if (!exists) {
                    CreateIndexRequest request = CreateIndexRequest.of(
                            builder -> {
                                try {
                                    Document mappingFromClass = elasticsearchOperations.indexOps(Product.class).createMapping();
                                    return builder.index(destIndex)
                                            .mappings(convert(mappingFromClass));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    openSearchClient.indices().create(request);
                    log.info("New index created!");
                }

                log.info("Start reindex at: {}", DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now()));
                ReindexRequest reindexRequest = new ReindexRequest.Builder()
                        .waitForCompletion(true)
                        .source(Source.of(builder -> builder.index(sourceIndex)))
                        .dest(Destination.of(builder -> builder.index(destIndex)))
                        .build();
                openSearchClient.reindex(reindexRequest);
                log.info("End reindex at: {}", DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now()));

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
                log.info("Aliases updated at: {}", DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now()));
                openSearchClient.indices().delete(builder -> builder.index(sourceIndex));
            }
        }

        // Create a sample product to update
        String id = "product_" + Instant.now();
        OffsetDateTime createdAt = OffsetDateTime.now();

        Product initialProduct = new Product();
        initialProduct.setId(id);
        initialProduct.setName("Initial Product");
        initialProduct.setPrice(BigDecimal.valueOf(10.0));
        initialProduct.setModificationDateTime(OffsetDateTime.now().minusMinutes(5));
        initialProduct.setCreatedDateTime(createdAt);

        createOrUpdateProduct(initialProduct);

        // Simulate an update with a newer modification date
        Product updatedProduct = new Product();
        updatedProduct.setId(id);
        updatedProduct.setName("Updated Product Name");
        updatedProduct.setPrice(BigDecimal.valueOf(12.0));
        updatedProduct.setModificationDateTime(OffsetDateTime.now());
        updatedProduct.setCreatedDateTime(createdAt);

        createOrUpdateProduct(updatedProduct);


        //Attempt to update with older date
        Product outdatedProduct = new Product();
        outdatedProduct.setId(id);
        outdatedProduct.setName("Outdated Update");
        outdatedProduct.setPrice(BigDecimal.valueOf(15.0));
        outdatedProduct.setModificationDateTime(OffsetDateTime.now().minusMinutes(10));
        outdatedProduct.setCreatedDateTime(createdAt);

        createOrUpdateProduct(outdatedProduct);
    }

    private void createOrUpdateProduct(Product product) throws IOException {
        Map<String, JsonData> paramsMap = new HashMap<>();
        // paramsMap.put("product", JsonData.of(product, jsonpMapper));
        paramsMap.put("updatedProduct", JsonData.of(product, jsonpMapper));
        paramsMap.put("newModificationDateTime", JsonData.of(product.getModificationDateTime().toString()));

        UpdateRequest<JsonData, JsonData> request = UpdateRequest.of(ur -> ur
                        .index("goods")
                        .id(product.getId())
                        //.doc(JsonData.of(product, jsonpMapper))
                        .script(s ->
                                        s.inline(in ->
                                                        in.lang("painless")
                                                                .source(
                                                                        """
                                                                                if (ctx._source.modificationDateTime == null ||
                                                                                    params.newModificationDateTime.isAfter(ctx._source.modificationDateTime)) {
                                                                                    ctx._source.name = params.updatedProduct.name;
                                                                                    ctx._source.price = params.updatedProduct.price;
                                                                                    ctx._source.quantity = params.updatedProduct.quantity;
                                                                                    ctx._source.description = params.updatedProduct.description;
                                                                                    ctx._source.vendor = params.updatedProduct.vendor;
                                                                                    ctx._source.anotherDescription = params.updatedProduct.anotherDescription;
                                                                                    ctx._source.text = params.updatedProduct.text;
                                                                                    ctx._source.modificationDateTime = params.newModificationDateTime;
                                                                                }
                                                                                """
/*                                                """
                                                        if (ctx._source.modificationDateTime < params.product.modificationDateTime) {
                                                            ctx._source = params.product;
                                                        }
                                                        """*/
                                                                )
                                                                .params(paramsMap)
                                        )

                        )
                        .upsert(JsonData.of(product, jsonpMapper))
        );


        openSearchClient.update(request, JsonData.class);
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
