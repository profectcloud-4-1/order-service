package profect.group1.goormdotcom.common.presigned.config;


import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3ClientConfig {
    private final S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {

        return S3Client.builder()
                .region(Region.of(s3Properties.getS3().getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

    }

    @Bean
    public S3Presigner s3Presigner() {

        return S3Presigner.builder()
                .region(Region.of(s3Properties.getS3().getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        /*
        S3Presigner.Builder builder = S3Presigner.builder()

                .region(Region.of(s3Properties.getS3().getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create());
        String endpoint = s3Properties.getS3().getEndpoint();
        if (!StringUtils.isBlank(endpoint)) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
        */
    }
}
