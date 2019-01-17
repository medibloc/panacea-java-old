# Panacea

Official client-side Java library for the [medibloc blockchain](https://github.com/medibloc/go-medibloc).

## Install
You can download org.medibloc.panacea.core library from jcenter.   

### Maven
```bash
<repositories>
    <repository>
      <id>jcenter</id>
      <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>

<dependency>
  <groupId>org.medibloc.panacea</groupId>
  <artifactId>core</artifactId>
  <version>0.1.2</version>
  <type>pom</type>
</dependency>
```

### Gradle
```bash
repositories {
    jcenter()
}

dependencies {
    implementation "org.medibloc.panacea:core:0.1.2"
}
```

## Usage
The sample_en/sample_ko projects describe how to use panacea library.

Please see the
[Main Class](https://github.com/medibloc/panacea-java/blob/master/sample_en/src/main/java/Main.java)
[(Korean)](https://github.com/medibloc/panacea-java/blob/master/sample_ko/src/main/java/Main.java).

## License
```
Copyright 2018 MediBloc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
