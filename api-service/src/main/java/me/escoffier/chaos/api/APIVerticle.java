package me.escoffier.chaos.api;

import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Context;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;

public class APIVerticle extends AbstractVerticle {


    private WebClient client;
    private CircuitBreaker circuit;

    @Override
    public void start() {
        circuit = CircuitBreaker.create("backend-circuit-breaker", vertx,
            new CircuitBreakerOptions()
                .setFallbackOnFailure(true)
                .setMaxFailures(3)
                .setResetTimeout(5000)
                .setTimeout(1000)
                .setMaxRetries(3)
        );

        Router router = Router.router(vertx);
        router.get("/").handler(this::invoke);
        router.get("/health").handler(rc -> rc.response().end("ok"));

        client = WebClient.create(vertx, new WebClientOptions()
            .setDefaultHost("backend-service.vertx-chaos-demo.svc").setDefaultPort(8080));

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8080);
    }

    private void invoke(RoutingContext rc) {
        circuit.rxExecuteCommandWithFallback(
            future ->
                client.get("/random").rxSend()
                    .map(HttpResponse::bodyAsJsonObject)
                    .subscribe(
                        future::complete,
                        future::fail
                    ),
            err -> new JsonObject().put("title", "No movie for you tonight, service unavailable " + err.getMessage())
        )
            .map(JsonObject::encodePrettily)
            .subscribe(
                res -> rc.response().end(res),
                rc::fail
            );
    }
}
