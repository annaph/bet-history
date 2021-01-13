# Bet History

## Brief description
Microservice that provides handling events related to user placing bets. It also exposes a REST API to retrieve betting history.

Main components of the microservice:
- `org.bet.history.BetHistoryApp`:  
Application entry point. Runs Bet History service as Spring Boot application.
- `org.bet.history.config.BetHistoryConfig`:  
Instantiates all components and resolve dependencies.
- `org.bet.history.controller.BetHistoryController`:  
Contains implementation of the REST endpoint to retrieve betting history.
- `org.bet.history.streaming.EventSources`:  
Component responsible to stream bet events.
- `org.bet.history.streaming.BetEventSubscriber`:  
Component responsible to handle bet events.
- `org.bet.history.cache.BetCache`:  
Contains implementation of the cache responsible to hold and maintain bet data.

## Build

### Compile
```sh
$ sbt compile
```

### Run unit tests
```sh
$ sbt test
```

### Run
```sh
$ sbt "runMain org.bet.history.BetHistoryApp"
```

### Docker
Deploy to local Docker Engine:
```sh
$ cd deploy && ./deploy-bet-history.sh
```

## API Usage
Use following HTTP request to retrieve bet history for an account:
```
GET http://localhost:8080/bets/{accountId}?first={first}&after={after}&before={before}
```

where path variable _accountId_ is mandatory, while query paramters _first_, _after_ and _before_ are optional.

An example of the response:
```json
{
	"nodes": [{
		"betId": "bet-0002",
		"payout": 500.12,
		"status": "Open",
		"fixtureName": "Start vs Brann",
		"outcomeName": "Brann scores 1st goal",
		"cursor": "9dce297c"
	}],
	"navigation": {
		"hasNextPage": false,
		"hasPreviousPage": false,
		"firstCursor": "9dce297c",
		"lastCursor": "9dce297c"
	}
}
```

## License
Free software. Enjoy :)
