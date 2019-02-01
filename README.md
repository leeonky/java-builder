# Java-builder
[![travis-ci](https://travis-ci.org/leeonky/java-builder.svg?branch=master)](https://travis-ci.org/leeonky/java-builder)
[![coveralls](https://coveralls.io/repos/github/leeonky/java-builder/badge.svg?branch=master&kill_cache=1)](https://coveralls.io/github/leeonky/java-builder)

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

Create FactorySet

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

// Output is
// hello world
println(bean.getStrValue());
```

###Reigister class

- With sequence

```java
factorySet.register(Bean.class, (bean, sequence) -> {
    bean.setStrValue("hello " + sequence);
});
Builder<Bean> builder = factorySet.type(Bean.class);

// Output is:
// hello 1
// hello 2
println(builder.build().getStrValue());
println(builder.build().getStrValue());
```

- With sequence and params

```java
factorySet.register(Bean.class, (bean, sequence, params) -> {
    bean.setStrValue("hello " + params.get("message"));
});

// Output is:
// hello world
println(factorySet.type(Bean.class).params(new HashMap<String, Object>{{
    put("message", "world");
}}).build().getStrValue());
```
