package com.github.leeonky.javabuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FactorySet {
    private final PropertyBuilder propertyBuilder = PropertyBuilder.createDefaultPropertyBuilder();
    private final DataRepository dataRepository;
    private final Map<Class, Factory> factories = new HashMap<>();
    private final Map<Class, Factory> factoryDefinitions = new HashMap<>();
    private final Map<Class, Map<String, Builder>> cacheBuilders = new HashMap<>();
    private final Map<String, Factory> aliases = new HashMap<>();

    public FactorySet(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public FactorySet() {
        this(new DefaultDataRepository());
    }

    public PropertyBuilder getPropertyBuilder() {
        return propertyBuilder;
    }

    public DataRepository getDataRepository() {
        return dataRepository;
    }

    public <T> Factory<T> onBuild(Class<T> type, Consumer<T> consumer) {
        return onBuild(type, (obj, buildContext) -> consumer.accept(obj));
    }

    public <T> Factory<T> onBuild(Class<T> type, BiConsumer<T, BuildContext<T>> consumer) {
        BeanFactory<T> beanFactory = new BeanFactory<>(this, type, consumer);
        factories.put(type, beanFactory);
        return beanFactory;
    }

    public <T> Factory<T> register(Class<T> type, Supplier<T> supplier) {
        return register(type, (buildContext) -> supplier.get());
    }

    public <T> Factory<T> register(Class<T> type, Function<BuildContext<T>, T> supplier) {
        ObjectFactory<T> objectFactory = new ObjectFactory<>(this, type, supplier);
        factories.put(type, objectFactory);
        return objectFactory;
    }

    public <T> Factory<T> factory(Class<T> type, String extend) {
        Factory<T> factory = factory(type);
        if (extend != null)
            factory = factory.query(extend);
        return factory;
    }

    @SuppressWarnings("unchecked")
    public <T> Factory<T> factory(Class<T> type) {
        return factories.computeIfAbsent(type, k -> new DefaultBeanFactory<>(type, this));
    }

    @SuppressWarnings("unchecked")
    public <T> Factory<T> factory(String alias) {
        Factory factory = aliases.get(alias);
        if (factory == null)
            throw new IllegalArgumentException(String.format("There is no factory for alias [%s]", alias));
        return factory;
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> type(Class<T> type) {
        return cacheBuilders.computeIfAbsent(type, t -> new HashMap<>())
                .computeIfAbsent(null, s -> new DefaultBuilder<>(this, factory(type)));
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> type(Class<T> type, String extend) {
        return cacheBuilders.computeIfAbsent(type, t -> new HashMap<>())
                .computeIfAbsent(extend, s -> new DefaultBuilder<>(this, factory(type, extend)));
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> toBuild(String alias) {
        Factory factory = aliases.get(alias);
        if (factory == null)
            throw new IllegalArgumentException("Factory alias '" + alias + "' does not exist");
        return cacheBuilders.computeIfAbsent(factory.getBeanClass().getType(), t -> new HashMap<>())
                .computeIfAbsent(null, s -> new DefaultBuilder<>(this, factory));
    }

    public boolean hasAlias(String alias) {
        return aliases.containsKey(alias);
    }

    public <T> void aliasFactory(String alias, Factory<T> factory) {
        if (aliases.containsKey(alias))
            throw new IllegalArgumentException("Factory alias '" + alias + "' already exists");
        aliases.put(alias, factory);
    }

    public <T> Factory<T> onBuild(FactoryDefinition<T> factoryDefinition) {
        Factory<T> definitionFactory = new DefinitionFactory<>(this, factoryDefinition);
        factoryDefinitions.put(factoryDefinition.getClass(), definitionFactory);
        return definitionFactory;
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> toBuild(Class<? extends FactoryDefinition<T>> factoryDefinitionClass) {
        Factory factory = factoryDefinitions.get(factoryDefinitionClass);
        if (factory == null)
            throw new IllegalArgumentException("FactoryDefinition '" + factoryDefinitionClass.getName() + "' does not exist");
        return new DefaultBuilder<>(this, factory);
    }
}
