package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.Objects;

public class BasqueName {
   private Integer id;
   private String name;

   @ProtoFactory
   public BasqueName(Integer id, String name) {
      this.id = id;
      this.name = name;
   }

   @ProtoField(number = 1)
   public Integer getId() {
      return id;
   }

   @ProtoField(number = 2)
   public String getName() {
      return this.name;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      BasqueName that = (BasqueName) o;
      return Objects.equals(id, that.id) &&
            Objects.equals(name, that.name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, name);
   }

   @Override
   public String toString() {
      return "BasqueName{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
   }
}
