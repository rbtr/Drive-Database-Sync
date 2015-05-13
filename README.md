# Drive-Database-Sync
A Google Drive sync layer for Android SQLite Databases

# Include in your project
## Gradle
Add the noted lines from the following snippet to your project's `build.gradle`
```javascript
repositories {

	... // Your repos

    // Include this line to add mavenCentral
    mavenCentral()

    // Include these lines to add the staging repo
    // maven { 
    //	url "https://oss.sonatype.org/content/repositories/snapshots" 
    //}
}


dependencies {

    ... // Your dependencies

    // Include this line to pull from the release repo
    compile 'com.github.athingunique:ddbs:X.X.X@aar'

    // Include this line to pull from the snapshot repo 
    // (replace X.X.X with the version number)
    // compile 'com.github.athingunique:ddbs:X.X.X-SNAPSHOT@aar'

}
```

# How to use
See Example app and [JavaDoc](https://athingunique.github.io/ddbs-doc/) provided.

# Example
The Example application demos putting some data (the current date) in a database, putting the database in Drive, deleting the local database, pulling the database out of Drive, etc. It should give you a basic idea of how to interface with this Library.

# Documentation
[JavaDoc](https://athingunique.github.io/ddbs-doc/)

# Version notes
Still in active development, v1 not finalized.

# Credits
Thanks to Google for creating something like this for the old Drive API which inspired me, and again to Google for not updating their demo and motivating me.

# Author(s)
Developed by [Evan Baker](http://e13engineering.com)

# License

	Apache v2

	Copyright 2015 Evan Baker

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
