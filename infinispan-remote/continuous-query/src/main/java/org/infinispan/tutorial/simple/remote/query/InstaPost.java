package org.infinispan.tutorial.simple.remote.query;

import java.util.Objects;

import org.infinispan.protostream.annotations.ProtoField;

/**
 * This class is annotated with the infinispan Protostream support annotations. With this method, you don't need to
 * define a protobuf file and a marshaller for the object. See this tutorial for additional explanations:
 * <p>
 * https://blog.infinispan.org/2018/06/making-java-objects-queryable-by.html
 */
public final class InstaPost {

   @ProtoField(number = 1)
   String id;

   @ProtoField(number = 2)
   String user;

   @ProtoField(number = 3)
   String hashtag;

   public InstaPost() {
      // An accessible no-arg constructor is required for Protostream to be able to instantiate this
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
