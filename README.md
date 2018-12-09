# gamerefinery-demo

**Get game**
----
  Returns JSON data about a single game in the db.
  
* **URL**

  /topgames/:appId
  
* **METHOD**

  `GET`
  
* **URL PARAMETERS**

  **Required:**
  
  `appId=[Integer]`: the application id of the game
  
  **Optional:**
  
  `fields=[String]`: if provided, the returned JSON representation will only contain the given fields
  
* **RESPONSE CONTENT**
  
  A list of JSON documents that match the given criteria (if the same game is present in the database for multiple 
  markets, they all are returned).

* **EXAMPLE**

  /topgames/1234?fields=name?fields=rank
  
**Get game in market**
----
  Returns JSON data about a single game in the db for the given market.
  
* **URL**

  /topgames/:appId/:market
  
* **METHOD**

  `GET`
  
* **URL PARAMETERS**

  **Required:**
  
  `appId=[Integer]`: the application id of the game
  
  `market=fi|es|it`: the market
  
  **Optional:**
  
  `fields=[String]`: if provided, the returned JSON representation will only contain the given fields
  
* **RESPONSE CONTENT**
  
  A list of JSON documents that match the given criteria (only contains one document unless several games have the same 
  appId for some reason).

* **EXAMPLE**

  /topgames/1234/fi?fields=name?fields=rank
  
  
**Get top games in market**
----
  Returns JSON data about the top x games for the given market.
  
* **URL**

  /topgames/top/:rank/:market
  
* **METHOD**

  `GET`
  
* **URL PARAMETERS**

  **Required:**
  
  `rank=[Integer]`: minimum rank to display
  
  `market=fi|es|it`: the market
  
  **Optional:**
  
  `fields=[String]`: if provided, the returned JSON representation will only contain the given fields
  
* **RESPONSE CONTENT**
  
  A list of JSON documents representing the current top x games in the given market, ordered by rank.

* **EXAMPLE**

  /topgames/top/10/fi?fields=name?fields=rank
  
**Get recently entered top games in market**
----
  Returns JSON data about the games that have entered top x for the given market at most y days in the past.
  
* **URL**

  /topgames/top/:rank/:market/:days
  
* **METHOD**

  `GET`
  
* **URL PARAMETERS**

  **Required:**
  
  `rank=10|20|50`: minimum rank for the games
  
  `market=fi|es|it`: the market
  
  `days=[Integer]`: number of days in the past (from current date)
  
  **Optional:**
  
  `fields=[String]`: if provided, the returned JSON representation will only contain the given fields
  
* **RESPONSE CONTENT**
  
  A list of JSON documents representing the games that have entered top x for the given market in at most y days in the 
  past, sorted by the time they entered top x staring with the most recent entry.

* **EXAMPLE**

  /topgames/top/10/fi/200?fields=name?fields=rank  
  
**Get history data for game in market**
----
  Returns JSON data about the rank history of the given game in the given market.
  
* **URL**

  /topgames/history/:appId/:market/:period
  
* **METHOD**

  `GET`
  
* **URL PARAMETERS**

  **Required:**
  
  `appId=[Integer]`: the application id of the game
  
  `market=fi|es|it`: the market
  
  `period=day|week|month`: the time period with which the returned data is grouped by
  
* **RESPONSE CONTENT**
  
  A list of history data items with rudimentary statistics about the rank history of the given game. Each data item is
  of the form `{ avg: 1.5, min: 1, max: 2, time: "2018-1-1"}`, with values calculated based on the available rank 
  history data grouped into time periods as requested. I.e. if the data period is `day`, the returned data contains an
  item for each day that had data available. If the data period is `week`, the returned data contains a data item for
  each week that had available data, identifier by the `time` field (which is the start of the time period). The other
  fields in the data item are the average, minimum and maximum of all values within the time period.

* **EXAMPLE**

  /topgames/history/1234/fi/month  

**Set favorite status of game**
----
  Set or unset the given game as favorite.
  
* **URL**

  /topgames/:appId/favorite
  
* **METHOD**

  `POST`
  
* **URL PARAMETERS**

  **Required:**
  
  `appId=[Integer]`: the application id of the game
  
* **DATA PARAMETERS**

  `[Boolean]`: the data should contain a single boolean value that represent the desired favorite status of the given game
  
* **RESPONSE STATUS**
  
  * **Code: 200 OK** if operation was successful
  * **Code: 400 BAD REQUEST** if the provided data is null
  * **Code: 404 NOT FOUND** if the requested game was not found in the db
  * **Code: 500 INTERNAL SERVER ERROR** if the db did not acknowledge the request

* **EXAMPLE**

  `curl --header "Content-Type: application/json" --request POST --data "true" http://host/topgames/1234/favorite`

