import {writable} from "svelte/store";

export const variables = {
    gateway: import.meta.env.VITE_GATEWAY
};

export let title = writable("Untitled");