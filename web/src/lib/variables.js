import {writable} from "svelte/store";

export const variables = {
    gateway: import.meta.env.VITE_GATEWAY,
    jwt: writable()
};

export let title = writable("Untitled");