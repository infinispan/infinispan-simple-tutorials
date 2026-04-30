package org.infinispan.tutorial.model;

import java.util.UUID;

import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Text;
import org.infinispan.protostream.annotations.Proto;

@Proto
@Indexed
public record Character(UUID id, @Text String name, @Text String bio, Archetype archetype) {

}
