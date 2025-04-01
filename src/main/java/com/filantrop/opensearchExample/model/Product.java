package com.filantrop.opensearchExample.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Alias;
import org.springframework.data.elasticsearch.annotations.Aliases;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*Aliases в OpenSearch не работает*/
@Aliases({
        @Alias("myalias1"),
        @Alias("myalias2")
})
@Document(indexName = "marketplace",
        /*и так тоже не работает*/
        aliases = {
                @Alias("myalias1"),
                @Alias("myalias2")
        }
)
public class Product {
    @Id
    private String id;

    @Field(type = FieldType.Text, name = "name")
    private String name;

    @Field(type = FieldType.Double, name = "price")
    private BigDecimal price;

    @Field(type = FieldType.Integer, name = "quantity")
    private Integer quantity;

    @Field(type = FieldType.Text, name = "description")
    private String description;

    @Field(type = FieldType.Keyword, name = "vendor")
    private String vendor;

    @Field(type = FieldType.Keyword, name = "anotherDescription")
    private String anotherDescription;

    @Field(type = FieldType.Object, index = false)
    private AdditionalInfo additionalInfo;
}
