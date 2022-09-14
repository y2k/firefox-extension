FROM clojure:temurin-11-lein-2.9.10-alpine

WORKDIR /app

COPY project.clj .

# RUN lein deps

RUN lein cljsbuild once

COPY src/extension/*.cljs src/extension/
COPY src/extension/*.cljc src/extension/

RUN lein cljsbuild once min

# RUN ls -la resources/public/js/compiled
