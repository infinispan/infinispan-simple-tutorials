package org.infinispan.tutorial.simple.encoding;

import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.UTF8StringMarshaller;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.net.URI;

public class InfinispanEncodingCaches {

   static RemoteCacheManager cacheManager;
   static RemoteCache<String, String> textCache;
   static RemoteCache<String, String> jsonCache;
   static RemoteCache<String, String> xmlCache;

   public static void main(String[] args) throws Exception {
      connectToInfinispan();
      manipulateCachesAndPrint();
      disconnect(false);
   }

   static void manipulateCachesAndPrint() {
      System.out.println("== Cache with text encoding and string marshaller.");
      textCache.put("text", "诶, 你好.");
      System.out.println("Get key from text cache: " + textCache.get("text"));

      System.out.println("== Cache with json encoding and string marshaller.");
      jsonCache.put("\"json\"", "{\"name\": \"infinispan\"}");
      System.out.println("Get key from json cache: " + jsonCache.get("json"));

      System.out.println("== Cache with xml encoding and string marshaller.");
      xmlCache.put("xml", "<name>infinispan</name>");
      System.out.println("Get key from xml cache: " + xmlCache.get("xml"));
   }

   public static void connectToInfinispan() throws Exception {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      URI textCacheURI = InfinispanEncodingCaches.class.getClassLoader().getResource("textCache.xml").toURI();
      URI jsonCacheURI = InfinispanEncodingCaches.class.getClassLoader().getResource("jsonCache.xml").toURI();
      URI xmlCacheURI = InfinispanEncodingCaches.class.getClassLoader().getResource("xmlCache.xml").toURI();

      builder.remoteCache("textCache").configurationURI(textCacheURI);
      builder.remoteCache("jsonCache").configurationURI(jsonCacheURI);
      builder.remoteCache("xmlCache").configurationURI(xmlCacheURI);

      cacheManager = TutorialsConnectorHelper.connect(builder);
      textCache = cacheManager.getCache("textCache");
      jsonCache = cacheManager.getCache("jsonCache")
              .withDataFormat(DataFormat.builder()
                      .keyMarshaller(new UTF8StringMarshaller())
                      .valueMarshaller(new UTF8StringMarshaller())
                      .keyType(MediaType.APPLICATION_JSON)
                      .valueType(MediaType.APPLICATION_JSON).build());
      xmlCache = cacheManager.getCache("xmlCache")
              .withDataFormat(DataFormat.builder()
                      .keyMarshaller(new UTF8StringMarshaller())
                      .valueMarshaller(new UTF8StringMarshaller())
                      .keyType(MediaType.APPLICATION_XML)
                      .valueType(MediaType.APPLICATION_XML).build());
   }

   public static void disconnect(boolean removeCaches) {
      if (removeCaches) {
         cacheManager.administration().removeCache(textCache.getName());
         cacheManager.administration().removeCache(jsonCache.getName());
         cacheManager.administration().removeCache(xmlCache.getName());
      }
      TutorialsConnectorHelper.stop(cacheManager);
   }
}
