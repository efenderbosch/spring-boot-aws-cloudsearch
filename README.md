# spring-boot-aws-cloudsearch
AWS CloudSearch client w/ Spring Boot integration

Add a cloudSeach block in your application.yml like this:

```yaml
cloudSearch:
  accessKey: ####################
  secretKey: ########################################
  gzip: true
  searchEndpoint: search-my-domain-##########################.us-east-1.cloudsearch.amazonaws.com
  region: us-east-1
  documentUploadMaxSizeBytes: 5_000_000
```

Make sure ``net.fender.springboot.aws.cloudsearch.CloudSearchConfig`` is added via ``@EnableAutoConfiguration`` or manually.

TODO
- there's a bug in splitting JSON in to chunks if the chunk is smaller than 1 doc's JSON
