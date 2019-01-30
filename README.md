# Java-builder
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

Create BuilderSet for registering and building:

```java
BuilderSet builderSet = new BuilderSet();
```

Define builder and build

```java

// Register class
builderSet.register(Bean.class, bean -> {
    bean.setStrValue("hello world");
});

// Build one object
Bean bean = builderSet.type(Bean.class).build();

// Output is "hello world"
println(bean.getStrValue());
```
