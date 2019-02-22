# Java-builder
[![travis-ci](https://travis-ci.org/leeonky/java-builder.svg?branch=master)](https://travis-ci.org/leeonky/java-builder)
[![coveralls](https://img.shields.io/coveralls/github/leeonky/java-builder/master.svg)](https://coveralls.io/github/leeonky/java-builder)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.leeonky/java-builder.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.leeonky/java-builder)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Lost commit](https://img.shields.io/github/last-commit/leeonky/java-builder.svg)](https://github.com/leeonky/java-builder)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b6f87097011245d8b3fcfa4324292640)](https://www.codacy.com/app/leeonky/java-builder?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=leeonky/java-builder&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/69d279626d49cac2bdc8/maintainability)](https://codeclimate.com/github/leeonky/java-builder/maintainability)

A library for building objects in test.

## Getting Started

Given a bean class:

```java
@Getter
@Setter
public class Bean {
    private String strValue;
    private int intValue;
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

### Register

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

###Build with property

```java
factorySet.onBuild(Bean.class, (bean) -> {
});

// Output is:
// hello world
println(factorySet.type(Bean.class).properties(new HashMap<String, Object>{{
    put("strValue", "hello world");
}}).build().getStrValue());
```

- guess and convert to right type

```java
factorySet.onBuild(Bean.class, (bean) -> {
});

// Output is:
// 100
println(factorySet.type(Bean.class).properties(new HashMap<String, Object>{{
    put("intValue", "100");
}}).build().getIntValue());
```
- register customer converter

```java
factorySet.onBuild(Bean.class, (bean) -> {
});

factorySet.registerConverter(converter ->
    converter.addTypeConverter(Long.class, int.class, Long::intValue));

// Output is:
// 100
println(factorySet.type(Bean.class).properties(new HashMap<String, Object>{{
    put("intValue", 100L);
}}).build().getIntValue());
```
