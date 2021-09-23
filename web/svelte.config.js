import staticAdapter from '@sveltejs/adapter-static';
import nodeAdapter from '@sveltejs/adapter-node';
import myAdapter from './adapter/adapter.js';

const hasAdapter = process.env.ADAPTER;
const adapt = hasAdapter ? hasAdapter : 'node';
const options = {};

const getAdapters = (adapt) => {
	console.log("Adapter: " + adapt);
	switch (adapt) {
		case 'node':
			console.log("Found node");
			return nodeAdapter;
		case 'static':
			return staticAdapter;
		case 'custom':
			return myAdapter;
		default:
			return myAdapter;
	}
};

const adapter = getAdapters(adapt);
console.log(adapter);

/** @type {import('@sveltejs/kit').Config} */
const config = {
	kit: {
		// hydrate the <div id="svelte"> element in src/app.html
		target: '#svelte',
		// adapter: myadapter({})
		// adapter: adapter({
		// 	pages: "build",
		// 	assets: "build",
		// 	fallback: null
		// })
	}
};

if (hasAdapter) {
	console.log("We have an adapter");
	config.kit.adapter = adapter(options);
}

export default config;
