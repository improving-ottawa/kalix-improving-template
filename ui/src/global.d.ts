declare global {
    declare interface Window {
        BEARER_TOKEN: string | undefined;
    }
}

export let BEARER_TOKEN = window.BEARER_TOKEN = undefined;
