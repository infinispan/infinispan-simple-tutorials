package org.infinispan.tutorial.simple.remote.marshalling;

import java.time.YearMonth;
import java.util.List;

// tag::model[]
public record Magazine(String name, YearMonth publicationDate, List<String> stories) {
}
// end::model[]
