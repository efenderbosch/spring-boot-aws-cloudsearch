package net.fender.springboot.aws.cloudsearch;

import static com.amazonaws.services.cloudsearchdomain.model.ContentType.Applicationjson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.fender.springboot.aws.cloudsearch.docs.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchdomain.model.Hits;
import com.amazonaws.services.cloudsearchdomain.model.SearchRequest;
import com.amazonaws.services.cloudsearchdomain.model.SearchResult;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsResult;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchClient;
import com.amazonaws.services.cloudsearchv2.model.DefineIndexFieldRequest;
import com.amazonaws.services.cloudsearchv2.model.DefineIndexFieldResult;
import com.amazonaws.services.cloudsearchv2.model.DeleteIndexFieldRequest;
import com.amazonaws.services.cloudsearchv2.model.DeleteIndexFieldResult;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsRequest;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsResult;
import com.amazonaws.services.cloudsearchv2.model.IndexDocumentsRequest;
import com.amazonaws.services.cloudsearchv2.model.IndexDocumentsResult;
import com.amazonaws.util.StringInputStream;

@Component
public class CloudSearchClient {

	private static final Logger log = LoggerFactory.getLogger(CloudSearchClient.class);

	public static final String NO_FIELDS = "_no_fields";
	public static final String INITIAL_CURSOR = "initial";

	@Autowired
	private AmazonCloudSearchClient cloudSearchClient;
	@Resource(lookup = "cloudSearchDomainClients")
	private Map<String, AmazonCloudSearchDomainClient> cloudSearchDomainClients;
	@Value("${cloudSearch.documentUploadMaxSizeBytes}")
	private int documentUploadMaxSizeBytes;
	private ProgressListener progressListener;

	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	public void setDocumentUploadMaxSizeBytes(int documentUploadMaxSizeBytes) {
		this.documentUploadMaxSizeBytes = documentUploadMaxSizeBytes;
	}

	public IndexDocumentsResult indexDocuments(String domainName) {
		return cloudSearchClient.indexDocuments(new IndexDocumentsRequest().withDomainName(domainName));
	}

	public DefineIndexFieldResult defineIndexField(DefineIndexFieldRequest defineIndexFieldRequest) {
		return cloudSearchClient.defineIndexField(defineIndexFieldRequest);
	}

	public DeleteIndexFieldResult deleteIndexField(DeleteIndexFieldRequest deleteIndexFieldRequest) {
		return cloudSearchClient.deleteIndexField(deleteIndexFieldRequest);
	}

	public DescribeDomainsResult describeDomain(String domainName) {
		DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest().withDomainNames(domainName);
		return cloudSearchClient.describeDomains(describeDomainsRequest);
	}

	public List<UploadDocumentsResult> uploadDocuments(String domainName, List<Document> docs) {
		if (docs.size() == 0) {
			return Collections.emptyList();
		}
		AmazonCloudSearchDomainClient domainClient = cloudSearchDomainClients.get(domainName);
		if (domainClient == null) {
			throw new IllegalArgumentException(domainName + " not known");
		}
		List<UploadDocumentsRequest> uploadDocumentsRequests = createUploadDocumentsRequest(docs);
		List<UploadDocumentsResult> uploadDocumentsResults = new ArrayList<>(uploadDocumentsRequests.size());
		for (UploadDocumentsRequest uploadDocumentsRequest : uploadDocumentsRequests) {
			UploadDocumentsResult uploadDocumentsResult = domainClient.uploadDocuments(uploadDocumentsRequest);
			uploadDocumentsResults.add(uploadDocumentsResult);
		}
		return uploadDocumentsResults;
	}

	private List<UploadDocumentsRequest> createUploadDocumentsRequest(List<Document> docs) {
		List<String> parts = chunkedJson(docs);
		List<UploadDocumentsRequest> uploadDocumentRequests = new ArrayList<>(parts.size());
		for (String part : parts) {
			try (StringInputStream documents = new StringInputStream(part)) {
				UploadDocumentsRequest uploadDocumentsRequest = new UploadDocumentsRequest(). //
						withDocuments(documents). //
						withContentLength((long) part.length()). //
						withContentType(Applicationjson);
				if (progressListener != null) {
					uploadDocumentsRequest.setGeneralProgressListener(progressListener);
				}
				uploadDocumentRequests.add(uploadDocumentsRequest);
			} catch (IOException e) {
				log.warn("this should never happen", e);
			}
		}
		return uploadDocumentRequests;
	}

	private List<String> chunkedJson(List<Document> docs) {
		List<String> parts = new ArrayList<>();
		int len = docs.size();
		StringBuffer sb = new StringBuffer("[");
		boolean appendComma = false;
		// TODO there's a bug in here when the chunk is too small
		for (int i = 0; i < len; i += 1) {
			String json = docs.get(i).toJsonString();
			log.trace(json);
			if (json.length() > documentUploadMaxSizeBytes) {
				// if a single object is larger than the desired size, still
				// send an array of size 1
				sb.append(json);
				sb.append("]");
				String part = sb.toString();
				log.debug("part {}", part.length());
				parts.add(part.toString());
				sb = new StringBuffer("[");
				appendComma = false;
			} else if (sb.length() + json.length() > documentUploadMaxSizeBytes) {
				// if this object would case the length to be greater than
				// the max, start a new one
				sb.append("]");
				String part = sb.toString();
				log.debug("part {}", part.length());
				parts.add(part.toString());
				sb = new StringBuffer("[");
				sb.append(json);
				log.trace("partial {}", sb.length());
				appendComma = true;
			} else {
				if (appendComma) {
					sb.append(',');
				}
				sb.append(json);
				log.trace("partial {}", sb.length());
				appendComma = true;
			}
		}
		// add the last chunk
		if (sb.charAt(sb.length() - 1) != ']') {
			sb.append("]");
		}
		String part = sb.toString();
		log.debug("part {}", part.length());
		parts.add(part.toString());
		return parts;
	}

	public List<SearchResult> search(String domainName, SearchRequest searchRequest) {
		AmazonCloudSearchDomainClient domainClient = cloudSearchDomainClients.get(domainName);
		if (domainClient == null) {
			throw new IllegalArgumentException(domainName + " not known");
		}
		if (progressListener != null) {
			searchRequest.setGeneralProgressListener(progressListener);
		}
		List<SearchResult> searchResults = new ArrayList<>();
		int found = 0;
		while (true) {
			SearchResult searchResult = domainClient.search(searchRequest);
			searchResults.add(searchResult);
			Hits hits = searchResult.getHits();
			log.debug("found {} {}", found, hits.getFound());
			int size = hits.getHit().size();
			found += size;
			if (size == 0 || hits.getFound() == found || hits.getCursor() == null) {
				break;
			}
			searchRequest.setCursor(hits.getCursor());
		}
		return searchResults;
	}

}
