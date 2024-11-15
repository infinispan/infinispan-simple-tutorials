package org.infinispan.tutorial.simple.remote.spatial;

import org.infinispan.commons.api.query.geo.LatLng;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;
import org.infinispan.protostream.annotations.ProtoSyntax;

@ProtoSchema(schemaPackageName = "tutorial",
      includeClasses = { Restaurant.class, TrainRoute.class, Hiking.class },
      dependsOn = LatLng.LatLngSchema.class, // this is required to use LatLng
      syntax = ProtoSyntax.PROTO3)
public interface TutorialSchema extends GeneratedSchema {
}
