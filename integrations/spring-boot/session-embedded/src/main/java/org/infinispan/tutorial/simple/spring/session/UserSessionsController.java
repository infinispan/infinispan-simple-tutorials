package org.infinispan.tutorial.simple.spring.session;

import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.session.MapSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class UserSessionsController {

    public static final String LATEST_SESSION_VALUE = "latest";
    @Autowired
    private SpringEmbeddedCacheManager cacheManager;

    @GetMapping("/")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
                           HttpSession session) {
        String latest = (String) session.getAttribute(LATEST_SESSION_VALUE);
        session.setAttribute("latest", name);
        if(latest == null) {
            return "Nobody to ciao";
        }
        return "ciao " + latest;
    }

    @GetMapping("/sessions")
    public String sessions() {
        return cacheManager.getCache("sessions").getNativeCache().keySet().toString();
    }

    @GetMapping("/sessions/{id}")
    public String sessionContent(@PathVariable String id) {
        SimpleValueWrapper simpleValueWrapper = (SimpleValueWrapper) cacheManager.getCache("sessions").get(id);
        if (simpleValueWrapper == null) {
            return "Session not found";
        }
        MapSession mapSession = (MapSession) simpleValueWrapper.get();
        return "Latest " + mapSession.getAttribute(LATEST_SESSION_VALUE);
    }
}
