import {writable} from "svelte/store";

export const variables = {
    gateway: import.meta.env.VITE_GATEWAY,
    jwt: writable()
};

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