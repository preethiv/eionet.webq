FROM tomcat:7.0.56-jre7
RUN rm -rf /usr/local/tomcat/conf/logging.properties /usr/local/tomcat/webapps/*
COPY target/webq2.war /usr/local/tomcat/webapps/ROOT.war
