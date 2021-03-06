package com.folio3.blog.dynamodbt.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicCompositeKey implements Serializable {

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String artist;

    @DynamoDBRangeKey
    private String songTitle;


}
