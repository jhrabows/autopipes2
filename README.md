# autopipes2
## history
imported from SF/SVN
upgrade to spring 3
adding rest subproject (war)
adding mock subproject (jar)
## build
mvn install -P [jdbc=default|mock]
These profiles affect the build of rest war only.
* jdbc-profile produces rest-jdbc.war with standard jdbc storage service
* mock-profile produces rest-mock.war with mocked storage service
The build tests depends on Oracle Jdbc Driver. To install run *sql/installdriver.bat*.
The tests run against test schema owner. You can create that owner with *recreateuser-ora.bat*

## Related Links
https://github.com/Tshibek/excel_project

## Env Variables
HYDROSERVER=http://localhost:9090/autorest/xml

Oracle service name from TNSNAMES.ora. Required if different from XE (Oracle 10 value). ORCL is Oracle 11 value.

CATALINA_OPTS=-DDB_SERVICE=ORCL
