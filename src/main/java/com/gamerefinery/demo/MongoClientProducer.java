package com.gamerefinery.demo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class MongoClientProducer {

    @Produces
    public MongoClient mongoClient() {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoClientURI uri = new MongoClientURI(
                "mongodb+srv://test:9Q3J96UK4prE9FGW@training-dbhj9.mongodb.net/test",
                MongoClientOptions.builder().codecRegistry(pojoCodecRegistry)
        );

        return new MongoClient(uri);
    }

}
