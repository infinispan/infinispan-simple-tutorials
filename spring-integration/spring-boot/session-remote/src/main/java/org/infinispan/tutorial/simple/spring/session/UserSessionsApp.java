package org.infinispan.tutorial.simple.spring.session;

import org.infinispan.spring.remote.session.configuration.EnableInfinispanRemoteHttpSession;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableInfinispanRemoteHttpSession
public class UserSessionsApp {

    public static void main(String... args) {
        new SpringApplicationBuilder().sources(UserSessionsApp.class).run(args);
    }
}
