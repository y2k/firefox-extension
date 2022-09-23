FROM clojure:temurin-8-lein-2.9.10-focal

ARG EXT_VERSION

RUN apt-get update && apt-get install -y zip

WORKDIR /app

COPY project.clj .

RUN lein cljsbuild once

COPY Makefile .
COPY resources/updates.template.json resources/
COPY resources/manifest.json resources/
COPY resources/options.html resources/
COPY src/extension/*.cljs src/extension/
COPY src/extension/*.cljc src/extension/

RUN make publish

# Application #

FROM scratch
COPY --from=0 /app/resources/my-extension.zip /build_result/
COPY --from=0 /app/resources/updates.json /build_result/
CMD [""]
