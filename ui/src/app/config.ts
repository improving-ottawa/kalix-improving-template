import _ from "lodash";

interface Config {
    exampleUiBaseUri: string
    exampleApiBaseUrl: string
}

const getConfigDev = _.memoize(() => {
    return Promise.resolve(
        {
            exampleUiBaseUri: process.env.EXAMPLE_UI_BASE_URI ?? 'http://localhost:3000',
            exampleApiBaseUrl: process.env.EXAMPLE_API_BASE_URL ?? 'http://localhost:8010'
        } as Config
    )
});

const getConfigProd = _.memoize(async () => {
    const res = await fetch("/env.json");
    const json = await res.json();
    return {
        exampleUiBaseUri: json.EXAMPLE_UI_BASE_URI,
        exampleApiBaseUrl: json.EXAMPLE_API_BASE_URL
    } as Config;
});

const getConfig = async () => {
    const cachedConfig = (process.env.NODE_ENV === 'production') ?
        await getConfigProd() : await getConfigDev()

    console.log(`API Url: ${cachedConfig.exampleApiBaseUrl}`)

    return cachedConfig
};

export type {Config}
export {getConfig}
