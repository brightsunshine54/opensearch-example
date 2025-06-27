package com.filantrop.opensearchExample;

import com.filantrop.opensearchExample.model.Product;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.opensearch.client.opensearch.core.UpdateResponse;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ProductUpdater {

    /*public static void main(String[] args) {
        // Initialize your OpenSearchClient
        OpenSearchClient client = OpenSearchClientFactory.createClient();

        String indexName = "product-index"; // Your index name
        String productId = "prod-123";     // ID of the product to update

        // Create the updated product data
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("New Product Name");
        updatedProduct.setPrice(new BigDecimal("29.99"));
        updatedProduct.setQuantity(100);
        updatedProduct.setDescription("Updated description");
        updatedProduct.setVendor("New Vendor");
        updatedProduct.setAnotherDescription("Another updated description");
        updatedProduct.setText("Updated text content");

        // Set the new modification date (typically you'd use OffsetDateTime.now())
        OffsetDateTime newModificationDateTime = OffsetDateTime.parse("2023-11-15T12:00:00Z");

        try {
            // Create and execute the update request
            UpdateResponse<Product> response = updateProductIfNewer(
                    client,
                    indexName,
                    productId,
                    updatedProduct,
                    newModificationDateTime
            );

            System.out.println("Update result: " + response.result());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UpdateResponse<Product> updateProductIfNewer(
            OpenSearchClient client,
            String indexName,
            String productId,
            Product updatedProduct,
            OffsetDateTime newModificationDateTime
    ) throws IOException {
        // Ensure the updated product has the same ID
        updatedProduct.setId(productId);

        UpdateRequest<Product, Product> request =
                new UpdateRequest.Builder<Product, Product>()
                        .index(indexName)
                        .id(productId)
                        .script(sc -> sc.inline(in -> in.lang("painless")
                                        .source("""
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
                                                """)
                                        .params("updatedProduct", JsonData.of(updatedProduct))
                                        .params("newModificationDateTime", JsonData.of(newModificationDateTime.toString()))
                                )
                        )
                        .build();

        return client.update(request, Product.class);
    }*/
}
