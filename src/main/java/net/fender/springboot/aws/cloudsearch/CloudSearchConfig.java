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
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainAsyncClient;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchAsync;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchAsyncClient;
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
	private String searchEndpoint;
	private Region region;
	private ClientConfiguration client = new ClientConfiguration();

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

	public ClientConfiguration getClient() {
		return client;
	}

	public void setClient(ClientConfiguration client) {
		this.client = client;
	}

	@Bean
	public AWSCredentialsProvider awsCloudSearchCredentialsProvider() {
		AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
			private AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

			@Override
			public AWSCredentials getCredentials() {
				return awsCredentials;
			}

			@Override
			public void refresh() {
				awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
			}
		};
		return awsCredentialsProvider;
	}

	@Bean
	public AmazonCloudSearchAsync awsCloudSearchAsyncClient(AWSCredentialsProvider awsCredentialsProvider) {
		return new AmazonCloudSearchAsyncClient(awsCredentialsProvider, client). //
				withEndpoint(searchEndpoint). //
				withRegion(region);
	}

	@Bean
	public Map<String, AmazonCloudSearchDomainAsyncClient> cloudSearchDomainAsyncClients(
			AmazonCloudSearchClient cloudSearchClient, AWSCredentialsProvider awsCredentialsProvider) {
		DescribeDomainsResult describeDomainsResult = cloudSearchClient.describeDomains();
		List<DomainStatus> domainStatusList = describeDomainsResult.getDomainStatusList();
		Map<String, AmazonCloudSearchDomainAsyncClient> domainClients = new HashMap<>(domainStatusList.size());
		for (DomainStatus domainStatus : domainStatusList) {
			log.debug("domainStatus: {}", domainStatus);
			String domainName = domainStatus.getDomainName();
			if (domainStatus.isCreated() && !domainStatus.isDeleted()) {
				log.info("creating AmazonCloudSearchDomainClient for {} domain", domainName);
				ServiceEndpoint serviceEndpoint = domainStatus.getDocService();
				AmazonCloudSearchDomainAsyncClient domainClient = new AmazonCloudSearchDomainAsyncClient(
						awsCredentialsProvider, client)
				.withEndpoint(serviceEndpoint.getEndpoint());
				domainClients.put(domainName, domainClient);
			} else {
				log.info("skipping domain {}: created = {}, deleted = {}", domainName, domainStatus.isCreated(),
						domainStatus.isDeleted());
			}
		}
		return domainClients;
	}

}
