# jkob
[![Build Status](https://travis-ci.org/soundvibe/jkob.svg)](https://travis-ci.org/soundvibe/jkob)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.soundvibe/jkob/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.soundvibe/jkob)

### Kotlin DSL for type-safe JSON building

##### An example of building JSON:
```kotlin
val json = json {
    "id" to "value" //key->value pair
    "name" to "foo"
    "items" ["value1", "value2", "value3"] //array
    for (i in 1..2) "item$i" to i //you can even use regular loops when building JSON
    "numbers" [1, 2, 3]
    "child" { //child object
        "age" to 19
        "isValid" to false
        "isNull" to null
    }
}
```
This JSON object representation can be converted to JSON string:
```kotlin
val jsonString = json.toString()
```
Output:
```javascript
{"id": "value", "name": "foo", "items": ["value1", "value2", "value3"], "item1": 1, "item2": 2, "numbers": [1, 2, 3], "child": {"age": 19, "isValid": false, "isNull": null}}
```

##### An example to retrieve elements from JSON:
```kotlin
val numbers = json["numbers"]?.toList<Int>()
println(numbers) //prints [1, 2, 3]
```


## Binaries


Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cnet.soundvibe.reacto).

Example for Gradle:

```groovy
compile 'net.soundvibe:jkob:1.0.0'
```

and for Maven:

```xml
<dependency>
    <groupId>net.soundvibe</groupId>
    <artifactId>jkob</artifactId>
    <version>1.0.0</version>
</dependency>
```


## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/soundvibe/jkob/issues).

## LICENSE

Copyright 2017 Linas Naginionis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy to the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
