package org.infinispan.tutorial.simple.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.Subject;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.security.AuthorizationPermission;
import org.infinispan.security.Security;
import org.infinispan.security.mappers.IdentityRoleMapper;

public class InfinispanCacheSecurity {

    public static final String CACHE_NAME = "my-cache";
    public static final String SECRET_CACHE_NAME = "my-secret-cache";
    public static final String READ_ONLY_ROLE = "read-only";
    public static final String WRITE_ONLY_ROLE = "write-only";
    public static final String ADMIN_ROLE = "admin";
    public static final String SECRET_ROLE = "secret";

    // These are the users authenticated by the application.
    // The subjects are provided by the application utilizing the caches.
    static final Subject ADMIN_USER = createSubject(ADMIN_ROLE);
    static final Subject SECRET_USER = createSubject(SECRET_ROLE);
    static final Subject READ_ONLY_USER = createSubject(READ_ONLY_ROLE);
    static final Subject WRITE_ONLY_USER = createSubject(WRITE_ONLY_ROLE);

    public DefaultCacheManager dcm;
    Cache<String, String> cache;
    Cache<String, String> secretCache;

    public static void main(String[] args) {
        InfinispanCacheSecurity security = new InfinispanCacheSecurity();

        // First, create the caches and insert the entries.
       // Each operation is wrapped with the user with the correct permissions.
        security.createDefaultCacheManager();
        security.createAllCachesAndPopulate(3);

        System.out.println("Entries for normal cache:");
        security.showAllEntriesWithUser(READ_ONLY_USER, security.cache);

        System.out.println("\nEntries for secret cache:");
        security.showAllEntriesWithUser(SECRET_USER, security.secretCache);

        // Now, we try to access the secret cache with a user that doesn't have the permission.
       // This operation should throw the SecurityException.
        System.out.println("\nFail to read with incorrect user:");
        try {
            security.showAllEntriesWithUser(WRITE_ONLY_USER, security.secretCache);
            throw new AssertionError("Should have failed to read with incorrect user");
        } catch (SecurityException ignore) {
            System.out.println("The user doesn't have permission to operate the secret cache");
        }

        security.stop();
    }

    public void createDefaultCacheManager() {
        // Setup up a clustered cache manager
        GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();

        // Enable authorization and define the roles and their permissions.
        global.security().authorization().enable()
                // Defines how to map a principal to a role.
                // The identity will simply map the principal name to the role name.
                // For example, user "admin" will map to role "admin", and so on.
                .principalRoleMapper(new IdentityRoleMapper())
                .groupOnlyMapping(false)
                // Define different roles and associate the permissions.
                .role(ADMIN_ROLE).permission(AuthorizationPermission.ALL)
                .role(SECRET_ROLE).permission(AuthorizationPermission.ALL)
                .role(READ_ONLY_ROLE).permission(AuthorizationPermission.ALL_READ)
                .role(WRITE_ONLY_ROLE).permission(AuthorizationPermission.ALL_WRITE);

        // Initialize the cache manager
        // We pass false to not start automatically.
        // Then we call start using the admin user. The user needs LIFECYCLE permission.
        dcm = new DefaultCacheManager(global.build(), false);
        Security.doAs(ADMIN_USER, dcm::start);
    }

    public void createAllCachesAndPopulate(int size) {
        // First, create the first cache.
        // This cache is accessible to any role.
        ConfigurationBuilder builder1 = new ConfigurationBuilder();
        builder1.clustering().cacheMode(CacheMode.DIST_SYNC);
        builder1.security().authorization().enable();

        // We utilize an admin to create the cache.
        cache = Security.doAs(ADMIN_USER, () -> dcm.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache(CACHE_NAME, builder1.build()));


        // Now we create a cache only accessible to users with the secret role.
        ConfigurationBuilder builder2 = new ConfigurationBuilder();
        builder2.clustering().cacheMode(CacheMode.DIST_SYNC);
        // The cache must include both roles so it can be stopped by the admin user as well.
        builder2.security().authorization().enable().roles(ADMIN_ROLE, SECRET_ROLE);

        // We utilize an admin to create the cache.
        secretCache = Security.doAs(ADMIN_USER, () -> dcm.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache(SECRET_CACHE_NAME, builder2.build()));

        // Now populate the caches with the correct user.
        Security.doAs(WRITE_ONLY_USER, () -> {
            for (int i = 0; i < size; i++) {
                cache.put(UUID.randomUUID().toString(), dcm.getNodeAddress());
            }
        });

        Security.doAs(SECRET_USER, () -> {
            for (int i = 0; i < size; i++) {
                secretCache.put(UUID.randomUUID().toString(), dcm.getNodeAddress());
            }
        });
    }

    public void showAllEntriesWithUser(Subject user, Cache<String, String> c) {
        Security.doAs(user, () -> c.entrySet().forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue())));
    }

    public int getCacheSize(Subject user, Cache<String, String> c) {
       return Security.doAs(user, c::size);
    }

    public void stop() {
        if (dcm != null) {
            Security.doAs(ADMIN_USER, dcm::stop);
            dcm = null;
        }
    }

    public static Subject createSubject(String principal) {
        return new Subject(true, Set.of(new ExamplePrincipal(principal)), Collections.emptySet(), Collections.emptySet());
    }

    private record ExamplePrincipal(String name) implements Principal, Serializable {

        @Override
        public String getName() {
            return name;
        }
    }
}
