package net.fender.springboot.aws.cloudsearch;

import static com.amazonaws.services.cloudsearchdomain.model.QueryParser.Lucene;
import static net.fender.springboot.aws.cloudsearch.CloudSearchClient.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

import net.fender.springboot.aws.cloudsearch.docs.AddDocument;
import net.fender.springboot.aws.cloudsearch.docs.DeleteDocument;
import net.fender.springboot.aws.cloudsearch.docs.Document;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.services.cloudsearchdomain.model.BucketInfo;
import com.amazonaws.services.cloudsearchdomain.model.Hit;
import com.amazonaws.services.cloudsearchdomain.model.SearchRequest;
import com.amazonaws.services.cloudsearchdomain.model.SearchResult;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsResult;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsResult;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestBootstrap.class })
public class CloudSearchClientTest {

	private static final Logger log = LoggerFactory.getLogger(CloudSearchClientTest.class);

	@Autowired
	private CloudSearchClient cloudSearchClient;

	private static String uuid() {
		return UUID.randomUUID().toString();
	}

	@Test
	public void test() throws Exception {
		List<Document> docs = new ArrayList<>();

		DescribeDomainsResult describeDomainsResult = cloudSearchClient.describeDomain("devtest-2");
		log.debug(describeDomainsResult.toString());

		Random random = new Random();
		Integer[] ids = new Integer[] { 1234, 2345, 3456, 4567, 5678, 6789, 7890 };
		Set<Integer> intSet = new HashSet<>(1000);
		// create random 8 digit integers
		for (int i = 0; i < 1000; i++) {
			String string = "";
			for (int j = 0; j < 8; j++) {
				string += random.nextInt(10);
			}
			intSet.add(Integer.valueOf(string));
		}
		List<Integer> ints = new ArrayList<>(intSet);

		// create test docs
		for (int j = 0; j < 50_000; j++) {
			ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Z"));
			List<Integer> docInts = new ArrayList<>();
			int numInts = ints.size();
			for (int i = 0; i < 10 + random.nextInt(10); i++) {
				docInts.add(ints.get(random.nextInt(numInts)));
			}
			List<String> literals = new ArrayList<>();
			int numToAdd = random.nextInt(4);
			for (int i = 0; i < numToAdd; i++) {
				literals.add(uuid());
			}
			TestDocument testDoc = new TestDocument(). //
					withLiteral(uuid()). //
					withListOfIntegers(docInts). //
					withInteger(ids[random.nextInt(ids.length)]). //
					withListOfLiterals(literals). //
					withDate(now);
			AddDocument doc = AddDocument.withRandomId().withPojo(testDoc);
			docs.add(doc);
		}

		log.info("upload start");
		List<UploadDocumentsResult> uploadDocumentsResults = cloudSearchClient.uploadDocuments("devtest-2", docs);
		for (UploadDocumentsResult uploadDocumentsResult : uploadDocumentsResults) {
			log.info("uploadDocumentsResult: {}", uploadDocumentsResult);
		}

		SearchRequest searchRequest = new SearchRequest(). //
				// withQuery("date:[1970-01-01T00:00:00Z TO " +
				// tomorrow.toString() + "]"). //
				// withQuery("integer_i: 1234"). //
				// withSort("date asc"). //
				withQuery("*:*"). //
				// withFacet(Facet.toJson("integer_i")). //
				// withFacet(Facet.toJson("list_of_integers_is")). //
				withCursor(INITIAL_CURSOR). //
				withSize(10000L). //
				withReturn(NO_FIELDS). //
				withQueryParser(Lucene);
		log.info("search start");
		List<SearchResult> searchResults = cloudSearchClient.search("devtest-2", searchRequest);
		log.info("search finish");
		List<Document> docsToDelete = new ArrayList<>();
		for (SearchResult searchResult : searchResults) {
			for (Entry<String, BucketInfo> facet : searchResult.getFacets().entrySet()) {
				log.debug("facet: {} {}", facet.getKey(), facet.getValue());
			}
			for (Hit hit : searchResult.getHits().getHit()) {
				// if (random.nextBoolean()) {
				DeleteDocument docToDelete = new DeleteDocument(hit.getId());
				docsToDelete.add(docToDelete);
				// }
			}
		}
		log.debug("" + docsToDelete.size());

		log.info("delete start");
		List<Future<UploadDocumentsResult>> deleteDocumentsResults = cloudSearchClient.uploadDocumentsAsync(
				"devtest-2", docsToDelete);
		for (Future<UploadDocumentsResult> deleteDocumentsResult : deleteDocumentsResults) {
			log.info("deleteDocumentsResult: {}", deleteDocumentsResult.get());
		}
	}
}
