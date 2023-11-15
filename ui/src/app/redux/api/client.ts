import { getConfig } from "../../config";
import {GatewayClient} from "../../../generated/com/example/gateway/api/Gateway_actionServiceClientPb";

let cachedClient: undefined | GatewayClient = undefined
export const getClient = async () => {
    if (cachedClient) return cachedClient
    else {
        const config = await getConfig()
        cachedClient = new GatewayClient(config.exampleApiBaseUrl)
        return cachedClient
    }
}
