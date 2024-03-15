package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.protostream.annotations.Proto;

/**
 * This class is annotated with the infinispan Protostream support annotations. With this method, you don't need to
 * define a protobuf file and a marshaller for the object. See this tutorial for additional explanations:
 * <p>
 * https://blog.infinispan.org/2018/06/making-java-objects-queryable-by.html
 */
@Proto
public record InstaPost(String id, String user, String hashtag){}
