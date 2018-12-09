package com.gamerefinery.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Updates.*;

@Path("/topgames")
@RequestScoped
public class TopGamesController {

    @Inject
    private MongoClient mongoClient;

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Document> findGame(
            @PathParam("id") Long id, // appId seems like a reasonable id to use?
            @QueryParam("fields") List<String> fields
    ) {
        FindIterable<Document> iterable = getDocuments()
                .find(eq("appId", id));

        if (fields != null && !fields.isEmpty()) {
            iterable = iterable.projection(include(fields));
        }

        return iterable.into(new ArrayList<>());
    }

    @GET
    @Path("/{id}/{market:(fi)|(it)|(es)}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Document> findGameInMarket(
            @PathParam("id") Long id,
            @PathParam("market") String market,
            @QueryParam("fields") List<String> fields
    ) {
        FindIterable<Document> iterable = getDocuments()
                .find(and(eq("appId", id), eq("market", market)));

        if (fields != null && !fields.isEmpty()) {
            iterable = iterable.projection(include(fields));
        }

        return iterable.into(new ArrayList<>());
    }

    @POST
    @Path("/{id}/favorite")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setFavorite(
            @PathParam("id") Long id,
            Boolean favorite
    ) {
        if (favorite == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Bson operation = favorite ? set("favorite", true) : unset("favorite");

        UpdateResult result = getDocuments().updateMany(
                eq("appId", id),
                operation
        );

        if (!result.wasAcknowledged()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } else if (result.getMatchedCount() < 1) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok().build();
        }
    }

    @GET
    @Path("/top/{top:[0-9]+}/{market:(fi)|(it)|(es)}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Document> getMarketTop(
            @PathParam("top") int top,
            @PathParam("market") String market,
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
    @Path("/top/{top:(10|20|50)}/{market:(fi)|(it)|(es)}/{days:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Document> getMarketEntered(
            @PathParam("top") int top,
            @PathParam("market") String market,
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
    @Path("/history/{id}/{market:(fi)|(it)|(es)}/{interval:(day)|(week)|(month)}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<HistoryData> getTitleHistory(
            @PathParam("id") Long id,
            @PathParam("market") String market,
            @PathParam("interval") String interval
    ) {
        List<Document> history = getDocuments()
                .find(and(eq("appId", id), eq("market", market)))
                .first()
                .get("history", List.class);

        // group the history entries by timestamps to time intervals
        Map<LocalDate, List<Integer>> ranks = history.stream()
                .collect(Collectors.groupingBy(
                        doc -> toInterval(interval, doc.getLong("ts")),
                        Collectors.mapping(
                                doc -> doc.getInteger("rank"),
                                Collectors.toList()
                        )
                ));

        // combine the grouped ranks within the time intervals to history data entries with some rudimentary
        // statistics about the game rank within the time interval
        return ranks.entrySet().stream()
                .map(entry -> new HistoryData(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(HistoryData::getLocalDate))
                .collect(Collectors.toList());
    }

    private MongoCollection<Document> getDocuments() {
        return mongoClient.getDatabase("tg").getCollection("games");
    }

    private static class HistoryData {
        LocalDate date;
        Double avg;
        Integer min;
        Integer max;

        HistoryData(LocalDate date, List<Integer> ranks) {
            this.date = date;
            this.avg = ranks.stream().mapToInt(Integer::intValue).average().getAsDouble();
            this.min = ranks.stream().mapToInt(Integer::intValue).min().getAsInt();
            this.max = ranks.stream().mapToInt(Integer::intValue).max().getAsInt();
        }

        // only return the serialized date string to the client
        @JsonIgnore
        public LocalDate getLocalDate() {
            return date;
        }

        public String getTime() {
            return date.toString();
        }

        public Double getAvg() {
            return avg;
        }

        public Integer getMin() {
            return min;
        }

        public Integer getMax() {
            return max;
        }
    }

    private static LocalDate toInterval(String param, long ts) {
        switch (param) {
            case "day": return toDay(toDateTime(ts));
            case "week": return toWeek(toDateTime(ts));
            case "month": return toMonth(toDateTime(ts));
            default: throw new UnsupportedOperationException("unsupported time interval: " + param);
        }
    }

    private static LocalDateTime toDateTime(long ts) {
        // converts the time from utc to system time zone, not sure if this is wanted or not
        return Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static LocalDate toDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate();
    }

    private static LocalDate toWeek(LocalDateTime dateTime) {
        return dateTime.with(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()).toLocalDate();
    }

    private static LocalDate toMonth(LocalDateTime dateTime) {
        return dateTime.withDayOfMonth(1).toLocalDate();
    }

}
