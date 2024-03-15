package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.protostream.annotations.Proto;

@Proto
public record BasqueName(String id, String name) {}