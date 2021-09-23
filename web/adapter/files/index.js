import {assetsMiddleware, kitMiddleware, prerenderedMiddleware} from './middlewares.js';
import express from 'express';
import Eureka from 'eureka-js-client';
import {networkInterfaces} from 'os';

let appName = process.env["APP_NAME"] || "web-server";
let path = process.env["SOCKET_PATH"] || false;
let host = process.env["HOST"] || "0.0.0.0";
let hostname = process.env["HOSTNAME"] || "localhost";
let registryHost = process.env["REGISTRY_HOST"] || "localhost";
let port = process.env["SERVER_PORT"] || !path && 3e3;

let gateway = process.env["GATEWAY"] || "localhost";

console.log(host + " -- " + hostname);

let ipAddr = '127.0.0.1';

const app = express();

app.use(assetsMiddleware, prerenderedMiddleware, kitMiddleware);

const nets = networkInterfaces();
const results = Object.create(null); // Or just '{}', an empty object

for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
        // Skip over non-IPv4 and internal (i.e. 127.0.0.1) addresses
        if (net.family === 'IPv4' && !net.internal) {
            if (!results[name]) {
                results[name] = [];
            }
            results[name].push(net.address);
        }
    }
}

for (const net in results) {
    console.log(net);
    console.log(results[net]);
    if (net == "Wi-Fi" || net == "eth0") {
        ipAddr = results[net][0];
        console.log("IpAddr is now " + ipAddr);
    }
}

// ------------------ Eureka Config --------------------------------------------

const eureka = new Eureka.Eureka({
    instance: {
        app: appName,
        hostName: hostname,
        ipAddr: ipAddr,
        statusPageUrl: `http://${hostname}:${port}`,
        port: {
            '$': port,
            '@enabled': 'true',
        },
        vipAddress: appName,
        dataCenterInfo: {
            '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
            name: 'MyOwn',
        }
    },
    eureka: {
        host: registryHost,
        port: 8761,
        servicePath: '/eureka/apps/',
        maxRetries: 50,
        requestRetryDelay: 2000,
    }
});
eureka.logger.level('debug');
eureka.start(function (error) {
    console.log(error || 'Registered with Eureka');
});

// ------------------ Server Config --------------------------------------------
let server = app.listen(port, function () {

    let host = server.address().address;
    let port = server.address().port;
    console.log("Hostname: " + hostname);
    console.log('Listening at http://%s:%s', host, port);
});