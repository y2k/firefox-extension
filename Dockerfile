FROM clojure:temurin-8-lein-2.9.10-focal

WORKDIR /app

COPY project.clj .

RUN lein cljsbuild once

COPY src/extension/*.cljs src/extension/
COPY src/extension/*.cljc src/extension/

RUN lein cljsbuild once min
