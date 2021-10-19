import {writable} from "svelte/store";

export const variables = {
    gateway: import.meta.env.VITE_GATEWAY,
    jwt: writable()
};

export let title = writable("Untitled");

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