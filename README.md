# vpm
Partial implementation of npm-like package manager

Provided for testing:
- package.json file without the "dependency" section
- Empty "node_modules" directory
- Executable target/vpm.jar file

To run the project:
- Simply use precompiled JAR file: java -jar ./target/vpm.jar
- Or recompile it first
    * mvn install
    * mvn clean package
  
Only two npm commands are implemented: **add** and **install**.

**NOTE 1:** Version conflict resolution is very minimalistic, see comments in the code.

**NOTE 2:** Package tar archives with newer versions are downloaded to node_modules 
directory, replacing existing ones, but not extracted. 
That part is just a placeholder.



