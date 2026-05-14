package org.infinispan.tutorial.docs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

public class GuideMetadataGenerator {

   private static final Set<String> SKIP_DIRS = Set.of(
         "target", "docs", "documentation", "docs-maven-plugin", ".git", ".github");

   private final Path srcDir;
   private final Path outputDir;

   public GuideMetadataGenerator(Path srcDir, Path outputDir) {
      this.srcDir = srcDir;
      this.outputDir = outputDir;
   }

   public static void main(String[] args) throws Exception {
      if (args.length < 2) {
         System.err.println("Usage: GuideMetadataGenerator <srcDir> <outputDir>");
         System.exit(1);
      }
      Path src = Path.of(args[0]);
      Path out = Path.of(args[1]);
      System.out.println("[INFO] Scanning for guides in: " + src);
      GuideMetadataGenerator generator = new GuideMetadataGenerator(src, out);
      generator.generate();
      System.out.println("[INFO] Guide metadata written to: " + out);
   }

   public void generate() throws IOException {
      List<Map<String, Object>> guides = new ArrayList<>();
      Path guidesOutputDir = outputDir.resolve("guides");
      if (Files.exists(guidesOutputDir)) {
         try (Stream<Path> files = Files.list(guidesOutputDir)) {
            files.forEach(f -> {
               try { Files.delete(f); } catch (IOException ignored) {}
            });
         }
      }
      Files.createDirectories(guidesOutputDir);

      try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
         collectGuides(srcDir, "", asciidoctor, guides, guidesOutputDir);
      }

      guides.sort(Comparator.comparing(g -> (String) g.get("id")));

      Map<String, Object> index = new LinkedHashMap<>();
      index.put("guides", guides);

      ObjectMapper om = new ObjectMapper(
            new YAMLFactory()
                  .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                  .disable(YAMLGenerator.Feature.SPLIT_LINES)
                  .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
      om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

      Files.createDirectories(outputDir);
      om.writeValue(outputDir.resolve("index.yaml").toFile(), index);
   }

   private void collectGuides(Path dir, String prefix, Asciidoctor asciidoctor,
         List<Map<String, Object>> guides, Path guidesOutputDir)
         throws IOException {
      try (Stream<Path> entries = Files.list(dir)) {
         List<Path> sorted = entries.sorted().toList();
         for (Path entry : sorted) {
            if (!Files.isDirectory(entry)) {
               continue;
            }
            String name = entry.getFileName().toString();
            if (name.startsWith(".") || SKIP_DIRS.contains(name)) {
               continue;
            }
            Path guideFile = entry.resolve("guide.adoc");
            String dirName = stripPrefix(entry.getFileName().toString());
            if (Files.exists(guideFile)) {
               String id = prefix.isEmpty() ? dirName : prefix + "-" + dirName;
               String language = deriveLanguage(guideFile);
               processGuide(guideFile, id, language, asciidoctor, guides, guidesOutputDir);
            }
            String subPrefix = prefix.isEmpty() ? dirName : prefix + "-" + dirName;
            collectGuides(entry, subPrefix, asciidoctor, guides, guidesOutputDir);
         }
      }
   }

   private String stripPrefix(String dirName) {
      if (dirName.startsWith("infinispan-")) {
         return dirName.substring("infinispan-".length());
      }
      return dirName;
   }

   private static final String NON_JAVA_CLIENTS_DIR = "non-java-clients";

   private String deriveLanguage(Path guideFile) {
      Path relative = srcDir.relativize(guideFile.getParent());
      if (relative.getNameCount() >= 2
            && relative.getName(0).toString().equals(NON_JAVA_CLIENTS_DIR)) {
         return relative.getName(1).toString();
      }
      return "java";
   }

   private void processGuide(Path guideFile, String id, String language, Asciidoctor asciidoctor,
         List<Map<String, Object>> guides, Path guidesOutputDir)
         throws IOException {
      String content = Files.readString(guideFile);
      Options perGuideOptions = Options.builder()
            .safe(SafeMode.UNSAFE)
            .baseDir(guideFile.getParent().toFile())
            .build();
      Document doc = asciidoctor.load(content, perGuideOptions);

      Map<String, Object> guide = new LinkedHashMap<>();
      guide.put("id", id);
      guide.put("title", doc.getDoctitle());
      guide.put("summary", attrString(doc, "summary"));
      guide.put("mode", attrString(doc, "mode"));
      guide.put("language", language);
      guide.put("topics", splitComma(attrString(doc, "topics")));
      guide.put("keywords", splitComma(attrString(doc, "keywords")));
      guide.put("source-dir", attrString(doc, "source-dir"));
      guide.put("file", id + ".adoc");

      String duration = attrString(doc, "duration");
      if (duration != null) {
         try {
            guide.put("duration", Integer.parseInt(duration));
         } catch (NumberFormatException e) {
            guide.put("duration", duration);
         }
      }

      guides.add(guide);

      String resolved = resolveIncludes(content, guideFile.getParent());
      Files.writeString(guidesOutputDir.resolve(id + ".adoc"), resolved);
   }

   private static final Pattern INCLUDE_PATTERN =
         Pattern.compile("^include::(.+?)\\[tag=(\\w[\\w-]*)\\]\\s*$", Pattern.MULTILINE);

   private final Map<Path, List<String>> fileCache = new HashMap<>();

   private String resolveIncludes(String content, Path baseDir) {
      Matcher matcher = INCLUDE_PATTERN.matcher(content);
      StringBuilder sb = new StringBuilder();
      while (matcher.find()) {
         String filePath = matcher.group(1);
         String tagName = matcher.group(2);
         String extracted = extractTag(baseDir.resolve(filePath), tagName);
         matcher.appendReplacement(sb, Matcher.quoteReplacement(extracted));
      }
      matcher.appendTail(sb);
      return sb.toString();
   }

   private String extractTag(Path file, String tag) {
      List<String> lines = fileCache.computeIfAbsent(file, f -> {
         try {
            return Files.readAllLines(f);
         } catch (IOException e) {
            System.err.println("WARNING: Cannot read include file: " + f);
            return List.of();
         }
      });
      if (lines.isEmpty()) {
         return "// Include not found: " + file.getFileName() + "[tag=" + tag + "]";
      }

      String startMarker = "tag::" + tag + "[]";
      String endMarker = "end::" + tag + "[]";
      StringBuilder sb = new StringBuilder();
      boolean capturing = false;
      for (String line : lines) {
         if (line.contains(startMarker)) {
            capturing = true;
            continue;
         }
         if (line.contains(endMarker)) {
            capturing = false;
            continue;
         }
         if (capturing) {
            sb.append(line).append('\n');
         }
      }
      String result = sb.toString();
      if (result.isEmpty()) {
         return "// Tag not found: " + tag + " in " + file.getFileName();
      }
      if (result.endsWith("\n")) {
         result = result.substring(0, result.length() - 1);
      }
      return result;
   }

   private String attrString(Document doc, String name) {
      Object val = doc.getAttribute(name);
      return val != null ? val.toString() : null;
   }

   private List<String> splitComma(String value) {
      if (value == null || value.isBlank()) {
         return List.of();
      }
      return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
   }
}
