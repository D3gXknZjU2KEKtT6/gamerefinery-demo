package com.gamerefinery.demo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class MongoClientProducer {
    @Produces
    public MongoClient mongoClient() {
        MongoClientURI uri = new MongoClientURI(
                "mongodb+srv://test:9Q3J96UK4prE9FGW@training-dbhj9.mongodb.net/test"
        );
        return new MongoClient(uri);
    }

}
