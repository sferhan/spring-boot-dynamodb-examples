/**
 * Copyright © 2018 spring-data-dynamodb-example (https://github.com/derjust/spring-data-dynamodb-examples)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.folio3.blog.dynamodbt.demo;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

// Enable scanning the database table in case the search column is neither a key attribute nor has an Index on it
@EnableScan
public interface MusicRespository extends CrudRepository<Music, MusicCompositeKey> {

    List<Music> findByArtist(String artist);

	List<Music> findBySongTitle(String songTitle);

    @Query(fields = "artist, songTitle")
    // Note : If projections are used on Global Secondary Indexes, the index must contain the desired fields in the first place
	List<Music> findByYear(Integer year);

	List<Music> findByQuality(String quality);

	List<Music> findByGenre(String genre);

	// Note : Order by can be done on one of the attributes of the same index. For example we wouldn't be able to order by 'year' when finding by artist
    // because our index only contains 'artist' and 'songTitle'
	List<Music> findByArtistOrderBySongTitleDesc(String artist);

}