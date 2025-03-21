package org.infinispan.tutorial.simple.remote.spatial;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.GeoPoint;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Keyword;
import org.infinispan.api.annotations.indexing.Latitude;
import org.infinispan.api.annotations.indexing.Longitude;
import org.infinispan.api.annotations.indexing.Text;
import org.infinispan.protostream.annotations.Proto;

@Proto
@Indexed
@GeoPoint(fieldName = "location", projectable = true, sortable = true)
public record Restaurant(
      @Keyword(normalizer = "lowercase", projectable = true, sortable = true) String name,
      @Text String description,
      @Text String address,
      @Latitude(fieldName = "location") Double latitude,
      @Longitude(fieldName = "location") Double longitude,
      @Basic Float score
) {
}
