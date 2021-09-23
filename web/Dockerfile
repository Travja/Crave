FROM node:16

WORKDIR /usr/node

COPY build/ ./

#COPY package.json ./

RUN npm i

#COPY . .

#CMD ["npm", "run", "preview", "--host", "0.0.0.0"]

CMD ["node", "index.js"]