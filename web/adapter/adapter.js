import esbuild from 'esbuild';
import {copyFileSync, createReadStream, createWriteStream, existsSync, readFileSync, statSync, writeFileSync} from 'fs';
import {dirname, join} from 'path';
import {pipeline} from 'stream';
import glob from 'tiny-glob';
import {fileURLToPath} from 'url';
import {promisify} from 'util';
import zlib from 'zlib';

const __dirname = dirname(fileURLToPath(import.meta.url));

const pipe = promisify(pipeline);

const files = fileURLToPath(new URL('./files', import.meta.url));

/**
 * @typedef {import('esbuild').BuildOptions} BuildOptions
 */

/** @type {import('.')} */
export default function ({
                             entryPoint = '.svelte-kit/node/index.js',
                             out = 'build',
                             precompress,
                             env: {
                                 path: path_env = 'SOCKET_PATH',
                                 host: host_env = 'HOST',
                                 port: port_env = 'PORT'
                             } = {},
                             serverFile = `${__dirname}/files/index.js`,
                             esbuild: esbuild_config
                         } = {}) {
    return {
        name: 'travja-node',

        async adapt(builder) {
            builder.rimraf(out);

            builder.log.minor('Copying assets');
            builder.writeClient(`${out}/client`);
            builder.writeServer(`${out}/server`);
            builder.writeStatic(`${out}/static`);

            builder.log.minor('Prerendering static pages');
            await builder.prerender({
                dest: `${out}/prerendered`
            });

            writeFileSync(
                `${out}/manifest.js`,
                `export const manifest = ${builder.generateManifest({
                    relativePath: './server'
                })};\n`
            );

            builder.copy(files, out, {
                replace: {
                    APP     : './server/app.js',
                    MANIFEST: './manifest.js',
                    PATH_ENV: JSON.stringify(path_env),
                    HOST_ENV: JSON.stringify(host_env),
                    PORT_ENV: JSON.stringify(port_env)
                }
            });

            if (precompress) {
                builder.log.minor('Compressing assets');
                await compress(`${out}/client`);
                await compress(`${out}/static`);
                await compress(`${out}/prerendered`);
            }

            copyFileSync(serverFile, join(`${out}`, `index.js`));
            copyFileSync(`${__dirname}/files/package.json`, join(`${out}`, `package.json`));
        }
    };
}

/**
 * @param {string} directory
 */
async function compress(directory) {
    if (!existsSync(directory)) {
        return;
    }

    const files = await glob('**/*.{html,js,json,css,svg,xml}', {
        cwd      : directory,
        dot      : true,
        absolute : true,
        filesOnly: true
    });

    await Promise.all(
        files.map((file) => Promise.all([compress_file(file, 'gz'), compress_file(file, 'br')]))
    );
}

/**
 * @param {string} file
 * @param {'gz' | 'br'} format
 */
async function compress_file(file, format = 'gz') {
    const compress =
              format == 'br'
              ? zlib.createBrotliCompress({
                  params: {
                      [zlib.constants.BROTLI_PARAM_MODE]     : zlib.constants.BROTLI_MODE_TEXT,
                      [zlib.constants.BROTLI_PARAM_QUALITY]  : zlib.constants.BROTLI_MAX_QUALITY,
                      [zlib.constants.BROTLI_PARAM_SIZE_HINT]: statSync(file).size
                  }
              })
              : zlib.createGzip({ level: zlib.constants.Z_BEST_COMPRESSION });

    const source      = createReadStream(file);
    const destination = createWriteStream(`${file}.${format}`);

    await pipe(source, compress, destination);
}
