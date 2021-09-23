FROM node:16

#ENV ADAPTER=custom

WORKDIR /usr/node

COPY build/ ./

RUN npm i

CMD ["node", "index.js"]