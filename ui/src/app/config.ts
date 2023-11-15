interface Config {
    exampleUiBaseUri: string
    exampleApiBaseUrl: string
}

const getConfig = async () => {
    if (process.env.NODE_ENV === 'production') {
        const res = await fetch("/env.json");
        const json = await res.json();
        return {
            exampleUiBaseUri: json.OTT_UI_BASE_URI,
            exampleApiBaseUrl: json.OTT_API_BASE_URL
        } as Config;
    }
    return Promise.resolve(
      {
          //TODO: Change api.example.io based on project hostname
          exampleUiBaseUri: process.env.OTT_UI_BASE_URI ?? 'https://api.example.io:443',
          exampleApiBaseUrl: process.env.OTT_API_BASE_URL ?? 'https://api.example.io:443'
      } as Config
    );
};

export type { Config }
export { getConfig }
