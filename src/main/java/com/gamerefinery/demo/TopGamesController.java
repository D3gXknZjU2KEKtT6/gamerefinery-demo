package com.gamerefinery.demo;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;

@Path("/topgames")
@RequestScoped
public class TopGamesController {

    @Inject
    private MongoClient mongoClient;

    @GET
    @Path("/{market:(fi)|(it)|(es)}/{top:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Document> getMarketTop(
            @PathParam("market") String market,
            @PathParam("top") int top,
            @QueryParam("fields") List<String> fields
    ) {
        FindIterable<Document> iterable = getDocuments()
                .find(and(eq("market", market), lte("rank", top)))
                .sort(orderBy(ascending("rank")));

        if (fields != null && !fields.isEmpty()) {
            iterable = iterable.projection(include(fields));
        }

        return iterable.into(new ArrayList<>());
    }

    private MongoCollection<Document> getDocuments() {
        return mongoClient.getDatabase("tg").getCollection("games");
    }

}
