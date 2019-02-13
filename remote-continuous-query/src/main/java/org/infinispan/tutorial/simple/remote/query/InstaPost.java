package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;

/**
 * This class is annotated with the infinispan Protostream support annotations. With this method, you don't need to
 * define a protobuf file and a marshaller for the object. See this tutorial for additional explanations:
 * <p>
 * https://blog.infinispan.org/2018/06/making-java-objects-queryable-by.html
 */
@ProtoDoc("@Indexed")
public final class InstaPost {

   @ProtoDoc("@Field")
   @ProtoField(number = 1, required = true)
   String id;

   @ProtoDoc("@Field")
   @ProtoField(number = 2, required = true)
   String user;

   @ProtoDoc("@Field")
   @ProtoField(number = 3, required = true)
   String hashtag;

   public InstaPost() {

   }

   public InstaPost(String id, String user, String hashtag) {
      this.id = id;
      this.user = user;
      this.hashtag = hashtag;
   }

   @Override
   public String toString() {
      return "Tag{" +
            "id='" + id + '\'' +
            ", value='" + user + '\'' +
            ", hashtag='" + hashtag + '\'' +
            '}';
   }
}
