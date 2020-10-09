FROM tiangolo/node-frontend:10 as build-stage

COPY ./ /app/

FROM nginx:1.15

COPY --from=build-stage /app/ /usr/share/nginx/html
COPY --from=build-stage /nginx.conf /etc/nginx/conf.d/default.conf

WORKDIR ./

COPY ./ /etc/nginx/html/
CMD ["/bin/sh",  "-c",  "exec nginx -g 'daemon off;'"]
