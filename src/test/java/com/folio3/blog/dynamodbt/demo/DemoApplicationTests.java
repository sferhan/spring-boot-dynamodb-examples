package com.folio3.blog.dynamodbt.demo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PropertyPlaceholderAutoConfiguration.class, DemoApplication.class})
public class DemoApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(DemoApplicationTests.class);

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    private DynamoDBMapper mapper;

    @Autowired
    private MusicRespository musicRespository;

    @Before
    public void init() throws Exception {
        CreateTableRequest ctr = mapper.generateCreateTableRequest(Music.class);
        final ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(5L, 5L);
        ctr.setProvisionedThroughput(provisionedThroughput);
        ctr.getGlobalSecondaryIndexes().forEach(v -> v.setProvisionedThroughput(provisionedThroughput));
        Boolean tableWasCreatedForTest = TableUtils.createTableIfNotExists(amazonDynamoDB, ctr);
        if (tableWasCreatedForTest) {
            log.info("Created table {}", ctr.getTableName());
        }
        TableUtils.waitUntilActive(amazonDynamoDB, ctr.getTableName());
        log.info("Table {} is active", ctr.getTableName());
    }

    @After
    public void destroy() throws Exception {
        if(amazonDynamoDB.listTables(new ListTablesRequest()).getTableNames().indexOf("Music") <= 0) {
            DeleteTableRequest dtr = mapper.generateDeleteTableRequest(Music.class);
            TableUtils.deleteTableIfExists(amazonDynamoDB, dtr);
            log.info("Deleted table {}", dtr.getTableName());
        }
    }

    @Test
    public void test1_contextLoads() {
    }

    @Test
    public void test2_tableExists() {
        if(amazonDynamoDB.listTables(new ListTablesRequest()).getTableNames().indexOf("Music") < 0) {
            fail();
        }
    }

    @Test
    public void test3_CRUD() {

        // Insertion test: Inserting two records and checking count in the database
        List<Review> reviews = new ArrayList<Review>();
        reviews.add(new Review("Really good song", 4.5f));
        reviews.add(new Review("Excellent", 4.7f));

        Music song1 = new Music("No one you know", "My Dog Spot", "Country", "Hey Now", 1984, reviews);
        musicRespository.save(song1);
        Music song2 = new Music("No one you know", "Somewhere Down The Road", "Country", "Somewhat Famous", 1985, reviews);
        musicRespository.save(song2);
        Assert.assertEquals(musicRespository.count(), 2);
        log.info("Insertion test successful");

        // Get All
        Iterable<Music> result = musicRespository.findAll();
        Assert.assertEquals(2, Lists.newArrayList(result).size());
        Assert.assertThat(result, hasItem(song1));
        Assert.assertThat(result, hasItem(song2));
        log.info("Get All test successful");

        //Query by hash key
        List<Music> hashKeySearchResult = musicRespository.findByArtist("No one you know");
        Assert.assertEquals(2, hashKeySearchResult.size());
        log.info("Query by by HashKey test successful");

        // Query by range key
        List<Music> rangeKeySearchResult = musicRespository.findBySongTitle("My Dog Spot");
        Assert.assertEquals(1, rangeKeySearchResult.size());
        Assert.assertThat(rangeKeySearchResult, hasItem(song1));
        log.info("Query by range key test successful");

        // Query by Id
        Optional<Music> queryByIdResult = musicRespository.findById(new MusicCompositeKey("No one you know", "Somewhere Down The Road"));
        Assert.assertNotEquals(false, queryByIdResult.isPresent());
        Assert.assertNotEquals(null, queryByIdResult.get());
        Assert.assertEquals(queryByIdResult.get(), song2);
        log.info("Query by ID test successful");

        // Query by a non key attribute by making an index on that attribute
        List<Music> queryUsingIndexResult = musicRespository.findByYear(1984);
        Assert.assertEquals(1, queryUsingIndexResult.size());
        Assert.assertEquals(queryUsingIndexResult.get(0).id, song1.id);
        log.info("Query using Index test successful");

        // Scan : Searching using an attribute that is neither a partition key nor range key and also does not have any index on it
        List<Music> scanResult = musicRespository.findByYear(1984);
        Assert.assertEquals(1, scanResult.size());
        Assert.assertEquals(scanResult.get(0).id, song1.id);
        log.info("Scan test successful");

        // Update: Get an existing user and update one of his attributes
        Music songMyDogSpot = scanResult.get(0);
        songMyDogSpot.setAlbumTitle("Different Title");
        musicRespository.save(songMyDogSpot);
        Assert.assertNotEquals(false, musicRespository.findById(songMyDogSpot.id).isPresent());
        Assert.assertEquals("Different Title", musicRespository.findById(songMyDogSpot.id).get().getAlbumTitle());
        log.info("Update test successful");

        // Delete: delete an existing user
        musicRespository.delete(songMyDogSpot);
        Assert.assertEquals(musicRespository.count(), 1);
        log.info("Delete test successful");

        log.info("All CRUD tests successful");

    }

}
