package com.filantrop.opensearchExample.service;

import com.filantrop.opensearchExample.model.AdditionalInfo;
import com.filantrop.opensearchExample.model.Product;
import com.filantrop.opensearchExample.repository.MarketplaceRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class MarketplaceInitializer implements InitializingBean {
    private final MarketplaceRepository repository;

    public MarketplaceInitializer(MarketplaceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void afterPropertiesSet() {
        repository.save(new Product(
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
                new AdditionalInfo("Key", 1)));
    }
}
