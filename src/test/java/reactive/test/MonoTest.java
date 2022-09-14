package reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;

@Slf4j
/*
  Reactive Stream
  1. Assincrono
  2. non-blocking
  3. Backpressure
  Publisher <- (subscribe) Subscriber
  Subscription é criado quando há Subscriber
  Publisher (onSubscribe e enviará o subscription) -> Subscriber
  Subscription <- (request N)  responsável por gerenciar o Backpressure (Subscribe)
  Publisher -> (onNext) Subscriber
  until:
  1. Publisher envia todos objetos requisitado
  2. Publisher envia todos objetos. (onComplete) subscriber e subscription serão cancelado
  3. Se há erro. (onError) -> subscriber e subscription serão cancelados
 */
public class MonoTest {

    @Test
    public void monoSubscriber(){
        var name = "Bruno Gil";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("----------------------");
        StepVerifier.create(mono)
            .expectNext(name)
            .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer(){
        var name = "Bruno Gil";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Value {}", s));
        log.info("----------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError(){
        var name = "Bruno Gil";
        Mono<String> mono = Mono.just(name)
                .map(s -> {throw new RuntimeException("Testing mono with error");});

        mono.subscribe(s -> log.info("Name {}", s), s -> log.error("Something bad happened"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);

        log.info("----------------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete(){
        var name = "Bruno Gil";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"));

        log.info("----------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription(){
        var name = "Bruno Gil";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"),
                subscription -> subscription.request(5));

        log.info("----------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods(){
        var name = "Bruno Gil";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, starting doing something... "))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .doOnSuccess(s -> log.info("doOnSucess executed {}", s));

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"));
    }

    @Test
    public void monoDoOnError(){
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception error"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .doOnNext(s -> log.info("Executing this onDoNext"))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume(){
        var name = "Bruno Gil";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception error"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn(){
        var name = "Bruno Gil";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception error"))
                .onErrorReturn("EMPTY")
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();
    }
}
