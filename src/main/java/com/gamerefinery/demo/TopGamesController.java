package com.gamerefinery.demo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.lt;

@Path("/topgames")
@RequestScoped
public class TopGamesController {

    @Inject
    private MongoClient mongoClient;

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Game> test() {
        List<Game> results = new ArrayList<>();
        for (Game game : getGameCollection().find(lt("rank", 50))) {
            results.add(game);
        }

        return results;
    }

    private MongoCollection<Game> getGameCollection() {
        return mongoClient.getDatabase("tg").getCollection("games", Game.class);
    }

}
