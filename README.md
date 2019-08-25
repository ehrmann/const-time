# Constant-time Java Comparators
[![CircleCI](https://circleci.com/gh/ehrmann/const-time.svg?style=svg)](https://circleci.com/gh/ehrmann/const-time)



## Motivation

Timing attacks against things like HMAC are [well documented](https://security.stackexchange.com/a/74552/54844), but
it's also possible to attack caches backed by Java `HashMaps`. Internally, hash tables use a hash code to look up
entries, then do an equality comparison to handle collisions. Because Java `hashCode()` implementations are rarely
cryptographically secure, a timing attack can be used to enumerate keys in a `HashMap`.

First, an attacker probes with string keys with known hash codes to identify hash codes with collisions (lookups with
collisions will be slower). Once hash codes have been identified, collisions can be generated for `Strings` with a
chosen prefix. Because `String.equals()` is not constant time, it can be "picked" the same way as an HMAC.

## Usage

```java
Map<String, V> cache = new TreeMap<>(ConstTimeStringComparator.INSTANCE);
```

Or for byte arrays,

```java
Map<String, V> cache = new TreeMap<>(ConstTimeByteArrayComparator.INSTANCE);
```

If you're especially paranoid (attacks depend on knowing the hash code algorithm for keys) and use composite keys,

```java
Map<Pojo, V> cache = new TreeMap<>(new ConstTimeComparatorBuilder<Pojo>()
    .comparingInt(Pojo::getField1)
    .comparingString(Pojo::getField2)
    .build());
```

