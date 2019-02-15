# Java-builder
[![travis-ci](https://travis-ci.org/leeonky/java-builder.svg?branch=master)](https://travis-ci.org/leeonky/java-builder)
[![coveralls](https://coveralls.io/repos/github/leeonky/java-builder/badge.svg?branch=master&service=github&kill_cache=1)](https://coveralls.io/github/leeonky/java-builder)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.leeonky/java-builder/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.leeonky/java-builder)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

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
factorySet.onBuild(Bean.class, bean -> {
    bean.setStrValue("hello world");
});

// Build one object
Bean bean = factorySet.type(Bean.class).build();

// Output is
// hello world
println(bean.getStrValue());
```

### Register class

- With sequence

```java
factorySet.onBuild(Bean.class, (bean, sequence) -> {
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
factorySet.onBuild(Bean.class, (bean, sequence, params) -> {
    bean.setStrValue("hello " + params.get("message"));
});

// Output is:
// hello world
println(factorySet.type(Bean.class).params(new HashMap<String, Object>{{
    put("message", "world");
}}).build().getStrValue());
```

- with no default constructor
```java
factorySet.register(Bean.class, (sequence) -> {
    Bean bean = new Bean();
    bean.setStrValue("hello " + sequence);
    return bean;
});
Builder<Bean> builder = factorySet.type(Bean.class);

// Output is:
// hello 1
println(builder.build().getStrValue());
```
