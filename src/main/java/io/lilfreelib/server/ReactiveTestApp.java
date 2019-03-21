package io.lilfreelib.server;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.lilfreelib.reactive.CollectedDataStore;

@ApplicationPath("reactive")
public class ReactiveTestApp extends Application {
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(ReactiveTestResource.class);
        classes.add(CollectedDataStore.class);
        return classes;
    }
}
