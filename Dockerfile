FROM nastyabirillo/plugin-utilities:1.0

WORKDIR /

COPY . lupa
RUN cd lupa && ./gradlew build

CMD ["bin/bash"]