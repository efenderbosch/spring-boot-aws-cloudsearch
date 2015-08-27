package net.fender.springboot.aws.cloudsearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fender.springboot.aws.cloudsearch.jackson.CloudSearchModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchClient;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsResult;
import com.amazonaws.services.cloudsearchv2.model.DomainStatus;
import com.amazonaws.services.cloudsearchv2.model.ServiceEndpoint;
import com.amazonaws.util.json.Jackson;

@Configuration
@ConfigurationProperties(prefix = "cloudSearch")
public class CloudSearchConfig {

	private static final Logger log = LoggerFactory.getLogger(CloudSearchConfig.class);

	static {
		Jackson.getObjectMapper().registerModule(new CloudSearchModule());
	}

	private String accessKey;
	private String secretKey;
	private boolean gzip;
	private String searchEndpoint;
	private Region region;
	private int documentUploadMaxSizeBytes;

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public boolean isGzip() {
		return gzip;
	}

	public void setGzip(boolean gzip) {
		this.gzip = gzip;
	}

	public String getSearchEndpoint() {
		return searchEndpoint;
	}

	public void setSearchEndpoint(String searchEndpoint) {
		this.searchEndpoint = searchEndpoint;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Regions region) {
		this.region = Region.getRegion(region);
	}

	public int getDocumentUploadMaxSizeBytes() {
		return documentUploadMaxSizeBytes;
	}

	public void setDocumentUploadMaxSizeBytes(int documentUploadMaxSizeBytes) {
		this.documentUploadMaxSizeBytes = documentUploadMaxSizeBytes;
	}

	@Bean
	public AWSCredentials awsCredentials() {
		return new BasicAWSCredentials(accessKey, secretKey);
	}

	@Bean
	public ClientConfiguration clientConfig() {
		return new ClientConfiguration().withGzip(gzip);
	}

	@Bean
	public AmazonCloudSearchClient awsCloudSearchClient(AWSCredentials awsCredentials, ClientConfiguration clientConfig) {
		return new AmazonCloudSearchClient(awsCredentials, clientConfig). //
				withEndpoint(searchEndpoint). //
				withRegion(region);
	}

	@Bean
	public AmazonCloudSearchDomainClients cloudSearchDomainClients(
			AmazonCloudSearchClient cloudSearchClient, AWSCredentials awsCredentials, ClientConfiguration clientConfig) {
		DescribeDomainsResult describeDomainsResult = cloudSearchClient.describeDomains();
		List<DomainStatus> domainStatusList = describeDomainsResult.getDomainStatusList();
		Map<String, AmazonCloudSearchDomainClient> domainClients = new HashMap<>(domainStatusList.size());
		for (DomainStatus domainStatus : domainStatusList) {
			log.debug("domainStatus: {}", domainStatus);
			String domainName = domainStatus.getDomainName();
			if (domainStatus.isCreated() && !domainStatus.isDeleted()) {
				log.info("creating AmazonCloudSearchDomainClient for {} domain", domainName);
				ServiceEndpoint serviceEndpoint = domainStatus.getDocService();
				AmazonCloudSearchDomainClient domainClient = new AmazonCloudSearchDomainClient(awsCredentials,
						clientConfig).withEndpoint(serviceEndpoint.getEndpoint());
				domainClients.put(domainName, domainClient);
			} else {
				log.info("skipping domain {}: created = {}, deleted = {}", domainName, domainStatus.isCreated(),
						domainStatus.isDeleted());
			}
		}
		return new AmazonCloudSearchDomainClients(domainClients);
	}

	public static class AmazonCloudSearchDomainClients {
		private final Map<String, AmazonCloudSearchDomainClient> cloudSearchDomainClients;

		public AmazonCloudSearchDomainClients(Map<String, AmazonCloudSearchDomainClient> cloudSearchDomainClients) {
			this.cloudSearchDomainClients = cloudSearchDomainClients;
		}

		public Map<String, AmazonCloudSearchDomainClient> getCloudSearchDomainClients() {
			return cloudSearchDomainClients;
		}

	}

}
