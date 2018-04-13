package me.escoffier.chaos.backend;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.util.Collections;

public class BackendVerticle extends AbstractVerticle {


    private CircuitBreaker circuit;
    private MongoClient client;

    @Override
    public void start(Future<Void> future) {
        Router router = Router.router(vertx);
        router.get("/health").handler(rc -> rc.response().end("OK"));
        router.get("/random").handler(this::getRandomMovie);

        circuit = CircuitBreaker.create("mongo-circuit-breaker", vertx,
            new CircuitBreakerOptions()
                .setFallbackOnFailure(true)
                .setMaxFailures(3)
                .setResetTimeout(5000)
                .setTimeout(1000)
        );

        client = MongoClient.createNonShared(vertx, new JsonObject()
            .put("connection_string", "mongodb://my-mongo.vertx-chaos-demo.svc:27017")
            .put("db_name", "chaos")
            .put("username", "user")
            .put("password", "password"));

        Completable prepareDatabase = prepare();

        prepareDatabase
            .andThen(
                vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .rxListen(8080)
                    .toCompletable()
            ).subscribe(CompletableHelper.toObserver(future));
    }

    private final static String COLLECTION_NAME = "movies";

    private Completable prepare() {
        return client.rxGetCollections()
            .flatMap(list -> {
                if (list.contains(COLLECTION_NAME)) {
                    return Single.just(COLLECTION_NAME);
                } else {
                    return client.rxCreateCollection(COLLECTION_NAME).toSingleDefault(COLLECTION_NAME);
                }
            })
            .flatMap(collection -> client.rxCount(collection, new JsonObject()))
            .flatMapCompletable(count -> {
                if (count == 0) {
                    return client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Shawshank Redemption"))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Godfather")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Dark Knight")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Godfather Part II")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Pulp Fiction")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Schindler's List")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Lord of the Rings: The Return of the King")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "12 Angry Men")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Good, the Bad and the Ugly")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Inception")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Lord of the Rings: The Fellowship of the Ring")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Forrest Gump")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Fight Club")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Star Wars: Episode V - The Empire Strikes Back")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Goodfellas")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Matrix")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Lord of the Rings: The Two Towers")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "One Flew Over the Cuckoo's Nest")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Seven Samurai")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Interstellar")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Se7en")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Star Wars: Episode IV - A New Hope")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Léon: The Professional")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Silence of the Lambs ")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Saving Private Ryan")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Spirited Away")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "The Usual Suspects")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Life Is Beautiful")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "City of God")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "Dangal")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "It's a Wonderful Life")))
                        .flatMap(x -> client.rxInsert(COLLECTION_NAME, new JsonObject().put("title", "City Lights")))
                        .toCompletable();
                } else {
                    return Completable.complete();
                }
            });
    }

    private void getRandomMovie(RoutingContext rc) {
        circuit.rxExecuteCommandWithFallback(
            future ->
                client.rxFind(COLLECTION_NAME, new JsonObject())
                    .map(list -> {
                        Collections.shuffle(list);
                        return list.get(0);
                    })
                    .subscribe((r, e) -> {
                        if (e == null) {
                            future.complete(r);
                        } else {
                            future.fail(e);
                        }
                    }),
            err -> new JsonObject().put("title", "No movie available")
        )
            .subscribe(json -> rc.response().end(json.encodePrettily()), rc::fail);

    }
}
