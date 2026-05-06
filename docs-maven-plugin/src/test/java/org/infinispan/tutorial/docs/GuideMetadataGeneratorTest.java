package org.infinispan.tutorial.docs;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GuideMetadataGeneratorTest {

   @TempDir
   Path outputDir;

   @Test
   void generatesIndexFromGuideFiles() throws Exception {
      Path srcDir = Path.of(getClass().getClassLoader()
            .getResource("test-tutorials").toURI());

      GuideMetadataGenerator generator = new GuideMetadataGenerator(srcDir, outputDir);
      generator.generate();

      Path indexFile = outputDir.resolve("index.yaml");
      assertTrue(Files.exists(indexFile), "index.yaml should be created");

      String yaml = Files.readString(indexFile);
      assertTrue(yaml.contains("Remote Cache Tutorial"), "should contain tutorial-a title");
      assertTrue(yaml.contains("Distributed Cache Tutorial"), "should contain tutorial-b title");
      assertTrue(yaml.contains("mode: remote"), "should contain mode attribute");
      assertTrue(yaml.contains("mode: embedded"), "should contain mode attribute");
      assertTrue(yaml.contains("caching"), "should contain topic");
   }

   @Test
   void copiesGuideFilesToOutput() throws Exception {
      Path srcDir = Path.of(getClass().getClassLoader()
            .getResource("test-tutorials").toURI());

      GuideMetadataGenerator generator = new GuideMetadataGenerator(srcDir, outputDir);
      generator.generate();

      Path guidesDir = outputDir.resolve("guides");
      assertTrue(Files.exists(guidesDir), "guides/ directory should be created");
      assertTrue(Files.list(guidesDir).count() >= 2, "should copy at least 2 guide files");
   }

   @Test
   void derivesIdFromDirectoryPath() throws Exception {
      Path srcDir = Path.of(getClass().getClassLoader()
            .getResource("test-tutorials").toURI());

      GuideMetadataGenerator generator = new GuideMetadataGenerator(srcDir, outputDir);
      generator.generate();

      String yaml = Files.readString(outputDir.resolve("index.yaml"));
      assertTrue(yaml.contains("id: remote-cache"), "should derive id: infinispan-remote/cache -> remote-cache");
      assertTrue(yaml.contains("id: embedded-cache-distributed"), "should derive id: infinispan-embedded/cache-distributed -> embedded-cache-distributed");
   }

   @Test
   void skipsDirectoriesWithoutGuide() throws Exception {
      Path srcDir = Path.of(getClass().getClassLoader()
            .getResource("test-tutorials").toURI());
      Path noGuideDir = srcDir.resolve("no-guide-here");
      Files.createDirectories(noGuideDir);

      GuideMetadataGenerator generator = new GuideMetadataGenerator(srcDir, outputDir);
      generator.generate();

      String yaml = Files.readString(outputDir.resolve("index.yaml"));
      assertFalse(yaml.contains("no-guide-here"), "should skip dirs without guide.adoc");

      Files.delete(noGuideDir);
   }
}
