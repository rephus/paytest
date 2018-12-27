#FROM  frolvlad/alpine-scala:2.11
FROM bigtruedata/sbt:0.13.15-2.11.8


# ENV SCALA_VERSION 2.11.8
# ENV SBT_VERSION 0.13.15

EXPOSE 8655

RUN mkdir /code
WORKDIR /codeº

ADD . /code/

RUN ./sbt clean update

CMD ./sbt run
