package net.fender.springboot.aws.cloudsearch;

import static com.amazonaws.services.cloudsearchdomain.model.QueryParser.Lucene;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
import org.springframework.test.context.web.WebAppConfiguration;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.cloudsearchdomain.model.Hit;
import com.amazonaws.services.cloudsearchdomain.model.SearchRequest;
import com.amazonaws.services.cloudsearchdomain.model.SearchResult;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsResult;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@SpringApplicationConfiguration(classes = { TestBootstrap.class })
public class CloudSearchClientTest {

	private static final Logger log = LoggerFactory.getLogger(CloudSearchClientTest.class);

	@Autowired
	private CloudSearchClient cloudSearchClient;

	@Test
	public void test() throws Exception {
		List<Document> docs = new ArrayList<>();
		for (int i = 0; i < 2000; i++) {
			ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Z"));
			AddDocument doc = AddDocument.withRandomId();
			doc.put("date", now);
			docs.add(doc);
		}

		// lambda!
		ProgressListener progressListener = (ProgressEvent progressEvent) -> {
			log.trace("progress {}", progressEvent);
		};

		log.info("upload start");
		List<UploadDocumentsResult> uploadDocumentsResults = cloudSearchClient.uploadDocuments("dev-test", docs);
		for (UploadDocumentsResult uploadDocumentsResult : uploadDocumentsResults) {
			log.info("uploadDocumentsResult: {}", uploadDocumentsResult);
		}

		log.info("search start");

		ZonedDateTime tomorrow = ZonedDateTime.now(ZoneId.of("Z")).plusDays(1);
		SearchRequest searchRequest = new SearchRequest(). //
				withQuery("date:[1970-01-01T00:00:00Z TO " + tomorrow.toString() + "]"). //
				withSort("date asc"). //
				withCursor("initial"). //
				withSize(1000L). //
				withQueryParser(Lucene);
		List<SearchResult> searchResults = cloudSearchClient.search("dev-test", searchRequest);

		String minDate = "9999-99-99T99:99:99Z";
		String maxDate = "0000-00-00T00:00:00Z";
		int found = 0;
		Set<String> idsToDelete = new HashSet<>();
		Random random = new Random();
		for (SearchResult searchResult : searchResults) {
			for (Hit hit : searchResult.getHits().getHit()) {
				String date = hit.getFields().get("date").get(0);
				if (date.compareTo(minDate) < 0) {
					minDate = date;
				}
				if (date.compareTo(maxDate) > 0) {
					maxDate = date;
				}
				if (log.isTraceEnabled()) {
					log.trace("hit: {} {}", hit.getId(), date);
				}
				if (random.nextBoolean()) {
					idsToDelete.add(hit.getId());
				}
				found++;
			}
		}
		log.info("search found {} between {} and {}", found, minDate, maxDate);

		List<Document> docsToDelete = new ArrayList<>();
		for (String id : idsToDelete) {
			DeleteDocument docToDelete = new DeleteDocument(id);
			docsToDelete.add(docToDelete);
		}
		log.info("delete start");
		List<UploadDocumentsResult> deleteDocumentsResults = cloudSearchClient
				.uploadDocuments("dev-test", docsToDelete);
		for (UploadDocumentsResult deleteDocumentsResult : deleteDocumentsResults) {
			log.info("deleteDocumentsResult: {}", deleteDocumentsResult);
		}
	}
}
