# autopipes2
## history
imported from SF/SVN
upgrade to spring 3
adding rest subproject (war)
adding mock subproject (jar)
## build
mvn install -P [jdbc=default|mock]
These profiles affect the build of rest war only.
jdbc-profile produces rest-jdbc.war with standard jdbc storage service
mock-profile produces rest-mock.war with mocked storage service
