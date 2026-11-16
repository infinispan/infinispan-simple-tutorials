package org.infinispan.tutorial.simple.remote.spatial;

import org.infinispan.api.annotations.indexing.GeoField;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Keyword;
import org.infinispan.api.annotations.indexing.model.LatLng;
import org.infinispan.protostream.annotations.Proto;

// tag::entities[]
@Proto
@Indexed
public record Hiking(@Keyword String name, @GeoField LatLng start, @GeoField LatLng end) {

}
// end::entities[]
