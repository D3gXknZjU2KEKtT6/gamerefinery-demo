package com.gamerefinery.demo;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.ZoneId;
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

    @GET
    @Path("/{market:(fi)|(it)|(es)}/{top:(10|20|50)}/{days:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Document> getMarketEntered(
            @PathParam("market") String market,
            @PathParam("top") int top,
            @PathParam("days") int days,
            @QueryParam("fields") List<String> fields
    ) {
        String timeKey = "top" + top + "Entry";
        // apparently the time stamps in the database are epoch milliseconds?
        long minTime = LocalDate.now().minusDays(days)
                .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        FindIterable<Document> iterable = getDocuments()
                .find(and(eq("market", market), gte(timeKey, minTime)))
                .sort(orderBy(descending(timeKey)));

        if (fields != null && !fields.isEmpty()) {
            iterable = iterable.projection(include(fields));
        }

        return iterable.into(new ArrayList<>());
    }

    @GET
    @Path("/history/{id}/{market:(fi)|(it)|(es)}/{interval:d|w|m}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Document> getTitleHistory(
            @PathParam("id") String id,
            @PathParam("market") String market,
            @PathParam("interval") String interval
    ) {
        // TODO: implementation
        return new ArrayList<>();
    }

    private MongoCollection<Document> getDocuments() {
        return mongoClient.getDatabase("tg").getCollection("games");
    }

}
