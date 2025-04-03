package com.filantrop.opensearchExample.service;

import com.filantrop.opensearchExample.model.AdditionalInfo;
import com.filantrop.opensearchExample.model.Product;
import com.filantrop.opensearchExample.repository.MarketplaceRepository;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.ReindexRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceInitializer implements InitializingBean {
    //private final MarketplaceRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final OpenSearchClient openSearchClient;

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing MarketplaceInitializer");
        log.info("Cluster health status: {}", elasticsearchOperations.cluster().health());

        Map<String, Object> actualMapping = elasticsearchOperations.indexOps(Product.class).getMapping();
        log.info("Mapping found in elastic: {}", actualMapping);

        Document mapping = elasticsearchOperations.indexOps(Product.class).createMapping();
        log.info("Mapping found in class: {}", mapping);

        MapDifference<String, Object> diff = Maps.difference(actualMapping, mapping);

        openSearchClient.reindex(ReindexRequest.of());

/*        if (diff.areEqual()){
            return;
        }

        boolean delete = elasticsearchOperations.indexOps(IndexCoordinatesProduct.class).delete();
        boolean withMapping = elasticsearchOperations.indexOps(Product.class).createWithMapping();*/



        /*        repository.save(new Product(
                "5",
                "Utopia Bedding Bed Pillowsrtyrty",
                new BigDecimal(39.99),
                2,
                "These professionally finished pillows, with high thread counts, provide great comfort against your skin along with added durability "
                        + "that easily resists wear and tear to ensure a finished look for your bedroom.",
                "Utopia Bedding",
                "fdsgdfgdfgdf",
                null));

        repository.save(new Product(
                "6",
                "Echo Dot Smart speakeryuertyurtyuty",
                new BigDecimal(34.99),
                10,
                "Our most popular smart speaker with a fabric design. It is our most compact smart speaker that fits perfectly into small spaces.",
                "Amazon",
                "dfghfghfghfgyutyuty",
                new AdditionalInfo("Key", 1)));*/
    }
}
