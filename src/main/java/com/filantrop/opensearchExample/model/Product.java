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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "goods")
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

    @Field(type = FieldType.Text, name = "vendor")
    private String vendor;

    @Field(type = FieldType.Text, name = "anotherDescription")
    private String anotherDescription;

    @Field(type = FieldType.Object, index = false)
    private AdditionalInfo additionalInfo;

    @Field(type = FieldType.Text, name = "text")
    private String text;

    @Field(type = FieldType.Text, name = "anotherText")
    private String anotherText;
}
