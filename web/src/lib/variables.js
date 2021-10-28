import {writable} from "svelte/store";

const createJWT = () => {
    const {subscribe, set} = writable();

    return {
        subscribe,
        set: (val) => {
            if (val)
                localStorage.setItem("jwt", val);
            else
                localStorage.removeItem("jwt");
            return set(val);
        },
        reset: () => {
            localStorage.removeItem("jwt");
            return set(undefined);
        }
    };
};

export const variables = {
    gateway: import.meta.env.VITE_GATEWAY,
    subKey: import.meta.env.VITE_AZURE_KEY,
    jwt: createJWT()
};

export const gateway = () => variables.gateway ? variables.gateway : window?.location.origin

export let title = writable("Untitled");

export let icons = {
    starFull: "/star-full.svg",
    starEmpty: "/star-empty.svg",
    starFullDark: "/star-full-dark.svg",
    starEmptyDark: "/star-empty-dark.svg"
}

export let loginState = {
    loggedIn: writable(false),
};

export const login = (token) => {
    loginState.loggedIn.set(true);
    variables.jwt.set(token);
};

export const logout = () => {
    loginState.loggedIn.set(false);
    variables.jwt.set(undefined);
};