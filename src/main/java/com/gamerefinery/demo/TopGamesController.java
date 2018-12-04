package com.gamerefinery.demo;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/topgames")
@RequestScoped
public class TopGamesController {

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> test() {
        return Stream.of("foo", "bar").collect(Collectors.toList());
    }

}
