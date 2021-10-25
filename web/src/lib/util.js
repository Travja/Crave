import {tweened} from "svelte/motion";
import {writable} from "svelte/store";

let setFetch = false;

export let scrollDistance = writable(0);

export const overrideFetch = () => {
    if (setFetch) return;

    const ff = window.fetch;
    window.fetch = function () {
        let jwt = loadJWT();
        if (!jwt) return ff.apply(this, arguments);

        let args = [...arguments];
        if (args[0].includes("crave") || args[0].includes("localhost")) {
            console.log(args);
            if (args.length >= 2) {
                if (!args[1])
                    args[1] = [];
                let details = args[1];
                let headers = details["headers"] ? details["headers"] : {};

                if (!headers.Authorization) {
                    headers.Authorization = "Bearer " + jwt;
                    // console.log("Added Authorization header", jwt);
                }

                details.headers = headers;
            } else {
                let headers = {headers: {Authorization: "Bearer " + jwt}};
                args.push(headers);
            }
        }
        // console.log(args);
        return ff.apply(this, args);
    };
    setFetch = true;
};

export const overrideXMLSend = () => {
    const send = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function (data) {
        let jwt = loadJWT();
        if (jwt) {
            this.setRequestHeader("Authorization", "Bearer " + jwt);
            // console.log("Added Authorization");
        }
        send.apply(this, data);
    };
};

export const verifyJWT = () => {
    let jwt = parseJWT();

    return !!jwt && new Date(jwt.exp * 1000) > Date.now();
};

export const parseJWT = () => {
    let token = loadJWT();
    if (!token)
        return undefined;

    let payload = atob(token.split('.')[1]);
    return JSON.parse(payload);
};

export const loadJWT = () => {
    let jwt = localStorage.getItem('jwt') ? localStorage.getItem('jwt') : undefined;

    return jwt;
};


export const formSubmit = (form, callback) => {
    fetch(form.action, {
        method: form.method.toUpperCase(),
        body: new FormData(form)
    })
        .then(res => res.json())
        .then(data => {
            if (callback)
                callback(data);
        })
        .catch(error => callback({error: true, message: error}));
};

export const findCheapest = details => {
    let lowestDetails;
    details.forEach(det => {
        if (!lowestDetails)
            lowestDetails = det;
        else {
            if (lowestDetails.price > det.price)
                lowestDetails = det;
        }
    });

    return lowestDetails;
};

export const formatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',

    // These options are needed to round to whole numbers if that's what you want.
    //minimumFractionDigits: 0, // (this suffices for whole numbers, but will print 2500.10 as $2,500.1)
    //maximumFractionDigits: 0, // (causes 2500.99 to be printed as $2,501)
});

let scrollEnabled = false;
let scrollPos = 0;
export const initScroll = () => {
    if (scrollEnabled) return;
    const updateScroll = (e) => {
        // scrollDistance.set(getScrollHeight());
        let pos = (document.documentElement || document.body).scrollTop / 100;
        scrollDistance.set(pos);
        // let pos = (document.documentElement || document.body).scrollTop;
        // scrollDistance.set(pos-scrollPos);
        // scrollPos = pos;
    };

    document.addEventListener("scroll", updateScroll);
    // scrollDistance.set(getScrollHeight());
    let pos = (document.documentElement || document.body).scrollTop / 100;
    scrollDistance.set(pos);
    // scrollDistance.set(pos - scrollPos);
    // scrollPos = pos;
    scrollEnabled = true;
};

export const getScrollHeight = (element) => {
    if (!element || element == document.body) {
        let h = document.documentElement,
            b = document.body,
            st = 'scrollTop',
            sh = 'scrollHeight';
        return (h[st] || b[st]) / ((h[sh] || b[sh]) - h.clientHeight);
    }
};

export const setupButtons = () => {
    overrideXMLSend();
    overrideFetch();
    for (let btn of document.getElementsByClassName("button")) {
        console.log(btn);
        btn.setAttribute("tabindex", 0);
        btn.onkeypress = (e) => {
            if (e.keyCode == 32 || e.keyCode == 13)
                e.target.click();
        }
    }
};