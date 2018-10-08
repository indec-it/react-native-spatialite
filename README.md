
# react-native-spatialite

React Native Spatialite Plugin for Android

## Getting started

`$ npm install react-native-spatialite --save`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import ar.gov.indec.react.spatialite.SpatialiteReactPackage;` to the imports at the top of the file
  - Add `new SpatialiteReactPackage()` to the list returned by the `getPackages()` method

2. Append the following lines to `android/settings.gradle`:

  	```
  	include ':react-native-spatialite'

  	project(':react-native-spatialite').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-spatialite/android')
  	```

3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:

  	```
    compile project(':react-native-spatialite')
  	```

4. Add JitPack in `android/build.gradle` as repository
    ```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
            ...
        }
    }
    ```

## Usage
```javascript
import db from 'react-native-spatialite';

db.createConnection('test.db').then(connected => {
    console.log('Database is connected', connected);
    return db.getVersion();
}).then(
    array => {
        console.log({array});
        return db.executeQuery('SELECT * FROM MyTable');
    }
).then(
    rows => {
        console.log({rows});
    }
).catch(
    err => {
        throw err;
    }
);
```
  
