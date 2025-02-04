package org.infinispan.tutorial.simple.remote.marshalling;

import java.time.YearMonth;
import java.util.List;

public record Magazine(String name, YearMonth publicationDate, List<String> stories) {

}
