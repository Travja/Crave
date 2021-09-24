# Deprecated. The purpose here was to build the svelte application on the container.
# This sometimes generates different files and breaks load balancing.
npm i && npm run build

cd build
npm i

cd ../
cp -r build ../tmp

rm -r *
cp -r ../tmp/* .
rm -r ../tmp

node index.js