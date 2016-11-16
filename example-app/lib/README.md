This is a local repository holding IOLITE APIs so that, if needed, maven can build it without access to IOLITE repositories.

To update the contents of this repository execute the following command in the project directory:
`mvn clean dependency:copy-dependencies -Dmdep.useRepositoryLayout=true -DincludeGroupIds=de.iolite -Dmdep.copyPom=true -DoutputDirectory=lib`