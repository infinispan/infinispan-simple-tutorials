package org.infinispan.tutorial.model;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Text;
import org.infinispan.protostream.annotations.Proto;

import java.util.UUID;

@Proto
@Indexed
public record Character(UUID id, @Text String name, @Text String bio, Archetype archetype) {

}
