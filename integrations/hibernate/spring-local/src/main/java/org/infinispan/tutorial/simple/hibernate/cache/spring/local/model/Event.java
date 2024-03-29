/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.infinispan.tutorial.simple.hibernate.cache.spring.local.model;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Cacheable
public class Event {

   @Id
   @GeneratedValue
   private Long id;

   private String name;

   private LocalDateTime timestamp = LocalDateTime.now();

   public Event(String name) {
      this.name = name;
   }

   public Event() {
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public LocalDateTime getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(LocalDateTime timestamp) {
      this.timestamp = timestamp;
   }

   @Override
   public String toString() {
      return "Event{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", time=" + timestamp +
            '}';
   }

}
