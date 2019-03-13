package org.infinispan.tutorial.simple.remote.query;

import java.util.Objects;

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
      return "InstaPost{" +
            "id='" + id + '\'' +
            ", user='" + user + '\'' +
            ", hashtag='" + hashtag + '\'' +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      InstaPost instaPost = (InstaPost) o;
      return Objects.equals(id, instaPost.id) &&
            Objects.equals(user, instaPost.user) &&
            Objects.equals(hashtag, instaPost.hashtag);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, user, hashtag);
   }
}
