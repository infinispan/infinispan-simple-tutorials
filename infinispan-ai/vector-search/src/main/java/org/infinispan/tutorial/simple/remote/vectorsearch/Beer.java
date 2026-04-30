package org.infinispan.tutorial.simple.remote.vectorsearch;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Keyword;
import org.infinispan.api.annotations.indexing.Text;
import org.infinispan.api.annotations.indexing.Vector;
import org.infinispan.api.annotations.indexing.option.VectorSimilarity;
import org.infinispan.protostream.annotations.Proto;

@Proto
@Indexed
public record Beer(
      @Keyword(projectable = true, sortable = true)
      String name,

      @Keyword(projectable = true, normalizer = "lowercase")
      String style,

      @Keyword(projectable = true, sortable = true, normalizer = "lowercase")
      String brewery,

      @Keyword(projectable = true, normalizer = "lowercase")
      String country,

      @Basic(projectable = true, sortable = true)
      Double abv,

      @Text
      String description,

      @Vector(dimension = 3, similarity = VectorSimilarity.COSINE)
      float[] descriptionEmbedding
) {
}
