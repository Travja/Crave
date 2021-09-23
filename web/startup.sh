npm i && npm run build

cd build
npm i

cd ../
cp -r build ../tmp

rm -r *
cp -r ../tmp/* .
rm -r ../tmp

node index.js