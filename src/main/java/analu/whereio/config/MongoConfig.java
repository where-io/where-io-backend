package analu.whereio.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017}")
    private String mongoUri;

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            @Autowired MappingMongoConverter converter) {

        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }

    @Override
    protected String getDatabaseName() {
        return "whereio_db";
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }
}
