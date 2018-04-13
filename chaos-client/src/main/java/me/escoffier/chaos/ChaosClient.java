package me.escoffier.chaos;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;

public class ChaosClient {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("You must pass the url as parameter");
            System.exit(1);
        }

        String url = args[0];
        Vertx vertx = Vertx.vertx();

        WebClient client = WebClient.create(vertx);

        vertx.setPeriodic(1000, l -> invoke(client, url));

    }

    private static void invoke(WebClient client, String url) {
        long begin = System.currentTimeMillis();
        client.getAbs(url)
            .rxSend()
            .map(HttpResponse::bodyAsJsonObject)
            .map(json -> json.getString("title"))
            .subscribe(
                title -> System.out.println("Your movie for tonight is " + title
                    + " (" + (System.currentTimeMillis() - begin) + " ms)"),
                err -> System.out.println("Oh oh... service invocation failed "
                    + "(" + (System.currentTimeMillis() - begin) + " ms)")
            );
    }

}
