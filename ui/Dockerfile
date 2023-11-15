# build stage
FROM node:lts-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install

COPY tsconfig.json .

# copy files into the build-stage container
COPY public /app/public
COPY src /app/src

RUN npm run build

# production stage
FROM nginx:stable-alpine as production-stage
COPY docker-resources/nginx.conf /etc/nginx/nginx.conf
COPY docker-resources/env.json /
COPY docker-resources/run.sh /
RUN chmod a+x /run.sh

# copy from npm production build container
COPY --from=build-stage /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["/bin/sh", "-c", "/run.sh"]
