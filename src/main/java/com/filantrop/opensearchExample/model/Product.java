package com.filantrop.opensearchExample.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "goods")
public class Product {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Float)
    private BigDecimal price;

    @Field(type = FieldType.Integer)
    private Integer quantity;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String vendor;

    @Field(type = FieldType.Text)
    private String anotherDescription;

    @Field(type = FieldType.Text)
    private String text;

    @Field(type = FieldType.Date)
    private OffsetDateTime modificationDateTime;

    @Field(type = FieldType.Date)
    private OffsetDateTime createdDateTime;
}
