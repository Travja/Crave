// import adapter from '@sveltejs/adapter-static';
// import node from '@sveltejs/adapter-node';
import node from './adapter/adapter.js';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	kit: {
		// hydrate the <div id="svelte"> element in src/app.html
		target: '#svelte',
		adapter: node({})
		// adapter: adapter({
		// 	pages: "build",
		// 	assets: "build",
		// 	fallback: null
		// })
	}
};

export default config;
