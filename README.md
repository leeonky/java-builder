# Java-builder
[![travis-ci](https://travis-ci.org/leeonky/java-builder.svg?branch=master)](https://travis-ci.org/leeonky/java-builder)
[![coveralls](https://coveralls.io/repos/github/leeonky/java-builder/badge.svg?branch=master)](https://coveralls.io/github/leeonky/java-builder)

A library for building objects in test.

## Getting Started

Given a bean class:

```java
@Getter
@Setter
public class Bean {
    private String strValue;
};
```

Create FactorySet for registering and building:

```java
FactorySet factorySet = new FactorySet();
```

Register and build

```java

// Register class
factorySet.register(Bean.class, bean -> {
    bean.setStrValue("hello world");
});

// Build one object
Bean bean = factorySet.type(Bean.class).build();

// Output is "hello world"
println(bean.getStrValue());
```
